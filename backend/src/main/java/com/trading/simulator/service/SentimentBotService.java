package com.trading.simulator.service;

import com.trading.simulator.dto.PlaceOrderRequest;
import com.trading.simulator.entity.Order;
import com.trading.simulator.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentBotService {

    private final OrderService orderService;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Strategy:
     * 1. Read SENTIMENT:{symbol} from Redis  → value between -1.0 and +1.0
     * 2. Convert to buy probability:
     *      sentiment +1.0 → 95% chance BUY
     *      sentiment  0.0 → 50% chance BUY  (neutral = coin flip)
     *      sentiment -1.0 → 5%  chance BUY  (mostly SELL)
     * 3. Roll the dice and place a LIMIT order near LTP
     *
     * Phase 6 News Engine will overwrite SENTIMENT keys with real values.
     * Until then, all stocks default to 0.0 (neutral).
     */
    public void act(User bot, String symbol) {
        try {
            String ltpVal = redisTemplate.opsForValue().get("LTP:" + symbol);
            if (ltpVal == null) return;
            BigDecimal ltp = new BigDecimal(ltpVal);

            // Read sentiment — default 0.0 if not set
            String sentimentVal = redisTemplate.opsForValue().get("SENTIMENT:" + symbol);
            double sentiment = sentimentVal != null ? Double.parseDouble(sentimentVal) : 0.0;

            // Map sentiment (-1 to +1) → buy probability (0.05 to 0.95)
            double buyProbability = 0.5 + (sentiment * 0.45);

            Order.OrderSide side = Math.random() < buyProbability
                    ? Order.OrderSide.BUY
                    : Order.OrderSide.SELL;

            // Small offset from LTP for limit order (0.2% – 1.0%)
            double offset = 0.002 + Math.random() * 0.008;
            BigDecimal price;
            if (side == Order.OrderSide.BUY) {
                price = ltp.multiply(BigDecimal.valueOf(1 - offset))
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                price = ltp.multiply(BigDecimal.valueOf(1 + offset))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            int quantity = 1 + (int)(Math.random() * 12); // 1–12 shares

            PlaceOrderRequest req = new PlaceOrderRequest();
            req.setSymbol(symbol);
            req.setSide(side);
            req.setOrderType(Order.OrderType.LIMIT);
            req.setPrice(price);
            req.setQuantity(quantity);

            orderService.placeOrder(bot.getId(), req, true);

        } catch (Exception e) {
            log.warn("[SentimentBot] Bot {} failed on {}: {}", bot.getId(), symbol, e.getMessage());
        }
    }
}