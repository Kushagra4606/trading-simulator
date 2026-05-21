
// src/main/java/com/trading/simulator/service/StockService.java
package com.trading.simulator.service;

import com.trading.simulator.entity.Stock;
import com.trading.simulator.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.Set;
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String LTP_PREFIX = "LTP:";

    // Runs once when Spring context is ready
    @PostConstruct
    public void seedLtpToRedis() {
        log.info("Seeding LTP values to Redis...");
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            String key = LTP_PREFIX + stock.getSymbol();
            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, stock.getBasePrice().toString());
            }
        }
        stocks.forEach(stock ->
                redisTemplate.opsForValue().set("SENTIMENT:" + stock.getSymbol(), "0.0")
        );

        // ── Flush stale order book on every restart ──────────────────────────
        Set<String> buyKeys  = redisTemplate.keys("ORDERBOOK:BUY:*");
        Set<String> sellKeys = redisTemplate.keys("ORDERBOOK:SELL:*");
        if (buyKeys  != null && !buyKeys.isEmpty())  redisTemplate.delete(buyKeys);
        if (sellKeys != null && !sellKeys.isEmpty()) redisTemplate.delete(sellKeys);
        log.info("Flushed stale order book from Redis. BUY keys: {}, SELL keys: {}",
                buyKeys == null ? 0 : buyKeys.size(),
                sellKeys == null ? 0 : sellKeys.size());

        log.info("LTP seeding complete. {} stocks loaded into Redis.", stocks.size());
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Optional<Stock> getStockBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol.toUpperCase());
    }

    public String getLtp(String symbol) {
        String key = LTP_PREFIX + symbol.toUpperCase();
        String ltp = redisTemplate.opsForValue().get(key);
        return ltp != null ? ltp : "N/A";
    }

    public BigDecimal getLtpAsBigDecimal(String symbol) {
        String val = redisTemplate.opsForValue().get("LTP:" + symbol);
        if (val == null) throw new RuntimeException("LTP not found for " + symbol);
        return new BigDecimal(val);
    }

}