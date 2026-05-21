
package com.trading.simulator.service;

import com.trading.simulator.dto.CandleResponse;
import com.trading.simulator.dto.CandleResponse.CandleDto;
import com.trading.simulator.entity.PriceCandle;
import com.trading.simulator.repository.PriceCandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandleService {

    private final PriceCandleRepository candleRepository;
    private final IndicatorService indicatorService;

    public CandleResponse getCandles(String symbol, String timeframe, int limit,
                                     boolean sma20, boolean sma50, boolean ema20,
                                     boolean rsi, boolean macd, boolean bb, boolean vwap) {

        // Always fetch 1m candles from DB — higher timeframes aggregated here
        List<PriceCandle> raw1m = candleRepository
                .findBySymbolAndTimeframeOrderByCandleTimeAsc(symbol, "1m");

        List<PriceCandle> candles = switch (timeframe) {
            case "5m"  -> aggregate(raw1m, 5);
            case "15m" -> aggregate(raw1m, 15);
            case "1D"  -> aggregateDaily(raw1m);
            default    -> raw1m; // 1m
        };

        // Apply limit — take the last N candles
        if (candles.size() > limit) {
            candles = candles.subList(candles.size() - limit, candles.size());
        }

        List<BigDecimal> closes = candles.stream().map(PriceCandle::getClose).toList();

        // Build indicator map
        Map<String, List<BigDecimal>> indicators = new LinkedHashMap<>();

        if (sma20)  indicators.put("sma20",  indicatorService.sma(closes, 20));
        if (sma50)  indicators.put("sma50",  indicatorService.sma(closes, 50));
        if (ema20)  indicators.put("ema20",  indicatorService.ema(closes, 20));
        if (rsi)    indicators.put("rsi",    indicatorService.rsi(closes, 14));
        if (bb) {
            var bbMap = indicatorService.bollingerBands(closes, 20);
            indicators.put("bbUpper",  bbMap.get("upper"));
            indicators.put("bbMiddle", bbMap.get("middle"));
            indicators.put("bbLower",  bbMap.get("lower"));
        }
        if (macd) {
            var macdMap = indicatorService.macd(closes);
            indicators.put("macdLine",      macdMap.get("macd"));
            indicators.put("macdSignal",    macdMap.get("signal"));
            indicators.put("macdHistogram", macdMap.get("histogram"));
        }
        if (vwap) {
            indicators.put("vwap", indicatorService.vwap(candles));
        }

        List<CandleDto> dtos = candles.stream().map(c -> CandleDto.builder()
                .time(c.getCandleTime())
                .open(c.getOpen())
                .high(c.getHigh())
                .low(c.getLow())
                .close(c.getClose())
                .volume(c.getVolume())
                .build()).toList();

        return CandleResponse.builder()
                .candles(dtos)
                .indicators(indicators)
                .build();
    }

    // Collapse N 1m candles into one candle
    private List<PriceCandle> aggregate(List<PriceCandle> raw, int minutes) {
        Map<LocalDateTime, List<PriceCandle>> groups = raw.stream()
                .collect(Collectors.groupingBy(c ->
                        c.getCandleTime().truncatedTo(ChronoUnit.MINUTES)
                                .withMinute((c.getCandleTime().getMinute() / minutes) * minutes)));

        return groups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> mergeCandles(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<PriceCandle> aggregateDaily(List<PriceCandle> raw) {
        Map<LocalDateTime, List<PriceCandle>> groups = raw.stream()
                .collect(Collectors.groupingBy(c ->
                        c.getCandleTime().truncatedTo(ChronoUnit.DAYS)));

        return groups.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> mergeCandles(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private PriceCandle mergeCandles(LocalDateTime time, List<PriceCandle> group) {
        group.sort(Comparator.comparing(PriceCandle::getCandleTime));
        BigDecimal open  = group.get(0).getOpen();
        BigDecimal close = group.get(group.size() - 1).getClose();
        BigDecimal high  = group.stream().map(PriceCandle::getHigh).max(BigDecimal::compareTo).orElse(open);
        BigDecimal low   = group.stream().map(PriceCandle::getLow).min(BigDecimal::compareTo).orElse(open);
        long volume      = group.stream().mapToLong(PriceCandle::getVolume).sum();

        return PriceCandle.builder()
                .symbol(group.get(0).getSymbol())
                .timeframe("merged")
                .candleTime(time)
                .open(open).high(high).low(low).close(close)
                .volume(volume)
                .build();
    }
}