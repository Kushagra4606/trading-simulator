package com.trading.simulator.service;

import com.trading.simulator.dto.PlaceOrderRequest;
import com.trading.simulator.entity.Order;
import com.trading.simulator.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketMakerBotService {

    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;

    public void act(User bot, String symbol) {
        try {
            String ltpVal = redisTemplate.opsForValue().get("LTP:" + symbol);
            if (ltpVal == null) return;

            BigDecimal ltp = new BigDecimal(ltpVal);
            if (ltp.compareTo(BigDecimal.ZERO) <= 0) return;

            double spreadPct = 0.001 + Math.random() * 0.004;
            BigDecimal spread = ltp.multiply(BigDecimal.valueOf(spreadPct));

            BigDecimal bidPrice = ltp.subtract(spread).setScale(2, RoundingMode.HALF_UP);
            BigDecimal askPrice = ltp.add(spread).setScale(2, RoundingMode.HALF_UP);

            int quantity = 1 + (int)(Math.random() * 10);

            // BUY limit below LTP
            PlaceOrderRequest buyReq = new PlaceOrderRequest();
            buyReq.setSymbol(symbol);
            buyReq.setSide(Order.OrderSide.BUY);
            buyReq.setOrderType(Order.OrderType.LIMIT);
            buyReq.setPrice(bidPrice);
            buyReq.setQuantity(quantity);
            orderService.placeOrder(bot.getId(), buyReq, true);

            // SELL limit above LTP
            PlaceOrderRequest sellReq = new PlaceOrderRequest();
            sellReq.setSymbol(symbol);
            sellReq.setSide(Order.OrderSide.SELL);
            sellReq.setOrderType(Order.OrderType.LIMIT);
            sellReq.setPrice(askPrice);
            sellReq.setQuantity(quantity);
            orderService.placeOrder(bot.getId(), sellReq, true);

        } catch (Exception e) {
            log.warn("[MarketMaker] Bot {} failed on {}: {}", bot.getId(), symbol, e.getMessage());
        }
    }
}