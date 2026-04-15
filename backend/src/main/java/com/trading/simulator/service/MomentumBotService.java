package com.trading.simulator.service;

import com.trading.simulator.dto.PlaceOrderRequest;
import com.trading.simulator.entity.Order;
import com.trading.simulator.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomentumBotService {

    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;

    /**
     * Strategy:
     * 1. Read current LTP
     * 2. Read previous LTP stored under LTP_PREV:{symbol}
     * 3. Price went UP   → place BUY  market order (follow the trend)
     * 4. Price went DOWN → place SELL market order (follow the trend)
     * 5. No change       → do nothing
     * 6. Always save current LTP as LTP_PREV for next cycle
     */
    public void act(User bot, String symbol) {
        try {
            String currentVal = redisTemplate.opsForValue().get("LTP:" + symbol);
            if (currentVal == null) return;

            BigDecimal currentLtp = new BigDecimal(currentVal);

            String prevVal = redisTemplate.opsForValue().get("LTP_PREV:" + symbol);

            // Save current as previous for next cycle (TTL 30s is enough)
            redisTemplate.opsForValue().set(
                    "LTP_PREV:" + symbol, currentVal, Duration.ofSeconds(30)
            );

            if (prevVal == null) return; // First run — no direction yet, skip

            BigDecimal prevLtp = new BigDecimal(prevVal);
            int comparison = currentLtp.compareTo(prevLtp);

            if (comparison == 0) return; // No movement — sit out

            Order.OrderSide side = comparison > 0 ? Order.OrderSide.BUY : Order.OrderSide.SELL;

            int quantity = 2 + (int)(Math.random() * 8); // 2–9 shares

            PlaceOrderRequest req = new PlaceOrderRequest();
            req.setSymbol(symbol);
            req.setSide(side);
            req.setOrderType(Order.OrderType.MARKET);
            req.setQuantity(quantity);

            orderService.placeOrder(bot.getId(), req, true);

        } catch (Exception e) {
            log.warn("[MomentumBot] Bot {} failed on {}: {}", bot.getId(), symbol, e.getMessage());
        }
    }
}