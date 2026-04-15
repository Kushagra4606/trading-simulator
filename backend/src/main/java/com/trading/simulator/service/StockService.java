
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
            // Only seed if not already present (so restarts don't reset live prices)
            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().set(key, stock.getBasePrice().toString());
            }
        }
        stocks.forEach(stock ->
                redisTemplate.opsForValue().set("SENTIMENT:" + stock.getSymbol(), "0.0")
        );
        log.info("[Seeder] Sentiment scores initialized to neutral (0.0) for all stocks");
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

    public void updateLtp(String symbol, String price) {
        redisTemplate.opsForValue().set(LTP_PREFIX + symbol.toUpperCase(), price);
    }
}