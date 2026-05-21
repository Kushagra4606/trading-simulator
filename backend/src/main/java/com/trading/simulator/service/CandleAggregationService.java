package com.trading.simulator.service;

import com.trading.simulator.entity.PriceCandle;
import com.trading.simulator.entity.Trade;
import com.trading.simulator.repository.PriceCandleRepository;
import com.trading.simulator.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleAggregationService {

    private final TradeRepository tradeRepository;
    private final PriceCandleRepository candleRepository;

    // Runs every 60 seconds — builds 1m candles from raw trades
    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void aggregateOneMinuteCandles() {
        // Get all unique symbols that have trades
        List<String> symbols = tradeRepository.findDistinctSymbols();

        for (String symbol : symbols) {
            try {
                buildCandlesForSymbol(symbol);
            } catch (Exception e) {
                log.error("Candle aggregation failed for {}: {}", symbol, e.getMessage());
            }
        }
    }

    private void buildCandlesForSymbol(String symbol) {
        // Find the last candle we built — start from there
        Optional<LocalDateTime> lastCandle = candleRepository.findLatestCandleTime(symbol);

        LocalDateTime from = lastCandle
                .map(t -> t.plusMinutes(1))          // next minute after last candle
                .orElse(LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MINUTES));

        // Truncate current minute — we don't build a candle for the CURRENT
        // in-progress minute because trades are still coming in
        LocalDateTime to = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        if (!from.isBefore(to)) {
            return; // nothing to build yet
        }

        List<Trade> trades = tradeRepository.findBySymbolAndExecutedAtBetween(symbol, from, to);

        if (trades.isEmpty()) {
            return;
        }

        // Group trades by their 1-minute bucket
        Map<LocalDateTime, List<Trade>> grouped = trades.stream()
                .collect(Collectors.groupingBy(trade ->
                        trade.getExecutedAt().truncatedTo(ChronoUnit.MINUTES)));

        List<PriceCandle> candles = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<Trade>> entry : grouped.entrySet()) {
            LocalDateTime bucket = entry.getKey();
            List<Trade> bucketTrades = entry.getValue();

            // Sort by time to get open and close correctly
            bucketTrades.sort(Comparator.comparing(Trade::getExecutedAt));

            BigDecimal open  = bucketTrades.get(0).getPrice();
            BigDecimal close = bucketTrades.get(bucketTrades.size() - 1).getPrice();
            BigDecimal high  = bucketTrades.stream().map(Trade::getPrice).max(BigDecimal::compareTo).orElse(open);
            BigDecimal low   = bucketTrades.stream().map(Trade::getPrice).min(BigDecimal::compareTo).orElse(open);
            long volume = bucketTrades.stream().mapToLong(t -> t.getQuantity().longValue()).sum();

            // Upsert — if candle already exists (re-run scenario), update it
            PriceCandle candle = candleRepository
                    .findBySymbolAndTimeframeAndCandleTime(symbol, "1m", bucket)
                    .orElse(PriceCandle.builder()
                            .symbol(symbol)
                            .timeframe("1m")
                            .candleTime(bucket)
                            .build());

            candle.setOpen(open);
            candle.setHigh(high);
            candle.setLow(low);
            candle.setClose(close);
            candle.setVolume(volume);
            candles.add(candle);
        }

        candleRepository.saveAll(candles);
        log.debug("Built {} candles for {}", candles.size(), symbol);
    }
}