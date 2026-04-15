package com.trading.simulator.service;

import com.trading.simulator.dto.PlaceOrderRequest;
import com.trading.simulator.entity.Order;
import com.trading.simulator.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhaleBotService {

    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;

    /**
     * Strategy:
     * - Only acts 40% of the time when called (extra rarity)
     * - Pure MARKET orders — whales don't negotiate price
     * - 60% buy bias — whales tend to accumulate
     * - Quantity 100–499 shares — this visibly moves the LTP
     */
    public void act(User bot, String symbol) {
        try {
            // Whales don't always act — 40% activity rate
            if (Math.random() > 0.4) return;

            String ltpVal = redisTemplate.opsForValue().get("LTP:" + symbol);
            if (ltpVal == null) return;

            Order.OrderSide side = Math.random() < 0.6
                    ? Order.OrderSide.BUY
                    : Order.OrderSide.SELL;

            int quantity = 100 + (int)(Math.random() * 400); // 100–499 shares

            PlaceOrderRequest req = new PlaceOrderRequest();
            req.setSymbol(symbol);
            req.setSide(side);
            req.setOrderType(Order.OrderType.MARKET);
            req.setQuantity(quantity);

            orderService.placeOrder(bot.getId(), req, true);

            log.info("[WhaleBot] 🐋 Bot {} placed {} {} x{}",
                    bot.getId(), side, symbol, quantity);

        } catch (Exception e) {
            log.warn("[WhaleBot] Bot {} failed on {}: {}", bot.getId(), symbol, e.getMessage());
        }
    }
}