package com.trading.simulator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderBookService {

    private final StringRedisTemplate redisTemplate;

    private static final String BUY_KEY  = "ORDERBOOK:BUY:";
    private static final String SELL_KEY = "ORDERBOOK:SELL:";

    // Member format: "orderId:userId:quantity"
    private String buildMember(Long orderId, Long userId, int quantity) {
        return orderId + ":" + userId + ":" + quantity;
    }

    public void addBuyOrder(String symbol, Long orderId, Long userId,
                            int quantity, BigDecimal price) {
        String key    = BUY_KEY + symbol;
        String member = buildMember(orderId, userId, quantity);
        redisTemplate.opsForZSet().add(key, member, price.doubleValue());
    }

    public void addSellOrder(String symbol, Long orderId, Long userId,
                             int quantity, BigDecimal price) {
        String key    = SELL_KEY + symbol;
        String member = buildMember(orderId, userId, quantity);
        redisTemplate.opsForZSet().add(key, member, price.doubleValue());
    }

    // Highest BUY bid (ZREVRANGE index 0)
    public ZSetOperations.TypedTuple<String> getBestBid(String symbol) {
        Set<ZSetOperations.TypedTuple<String>> result =
                redisTemplate.opsForZSet()
                        .reverseRangeWithScores(BUY_KEY + symbol, 0, 0);
        return (result == null || result.isEmpty()) ? null
                : result.iterator().next();
    }

    // Lowest SELL ask (ZRANGE index 0)
    public ZSetOperations.TypedTuple<String> getBestAsk(String symbol) {
        Set<ZSetOperations.TypedTuple<String>> result =
                redisTemplate.opsForZSet()
                        .rangeWithScores(SELL_KEY + symbol, 0, 0);
        return (result == null || result.isEmpty()) ? null
                : result.iterator().next();
    }

    public void removeBuyOrder(String symbol, String member) {
        redisTemplate.opsForZSet().remove(BUY_KEY + symbol, member);
    }

    public void removeSellOrder(String symbol, String member) {
        redisTemplate.opsForZSet().remove(SELL_KEY + symbol, member);
    }

    // Update quantity in sorted set (remove old member, re-add with new qty)
    public void updateBuyOrderQty(String symbol, String oldMember,
                                  String newMember, double price) {
        redisTemplate.opsForZSet().remove(BUY_KEY + symbol, oldMember);
        redisTemplate.opsForZSet().add(BUY_KEY + symbol, newMember, price);
    }

    public void updateSellOrderQty(String symbol, String oldMember,
                                   String newMember, double price) {
        redisTemplate.opsForZSet().remove(SELL_KEY + symbol, oldMember);
        redisTemplate.opsForZSet().add(SELL_KEY + symbol, newMember, price);
    }
}