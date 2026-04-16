package com.trading.simulator.service;

import com.trading.simulator.entity.*;
import com.trading.simulator.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.trading.simulator.dto.LtpUpdateDto;
import com.trading.simulator.dto.OrderFillNotificationDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

    private final OrderBookService orderBookService;
    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final WalletRepository walletRepository;
    private final HoldingRepository holdingRepository;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    /**
     * Core matching loop. Called after every new order is placed.
     * Keeps matching until no more matches exist for this symbol.
     */
    @Transactional
    public void match(String symbol) {
        while (true) {
            ZSetOperations.TypedTuple<String> bestBid =
                    orderBookService.getBestBid(symbol);
            ZSetOperations.TypedTuple<String> bestAsk =
                    orderBookService.getBestAsk(symbol);

            if (bestBid == null || bestAsk == null) break;

            double bidPrice = bestBid.getScore();
            double askPrice = bestAsk.getScore();

            // The fundamental matching condition
            if (bidPrice < askPrice) break;

            // Parse member: "orderId:userId:quantity"
            String[] bidParts = bestBid.getValue().split(":");
            String[] askParts = bestAsk.getValue().split(":");

            Long buyOrderId  = Long.parseLong(bidParts[0]);
            Long buyUserId   = Long.parseLong(bidParts[1]);
            int  buyQty      = Integer.parseInt(bidParts[2]);

            Long sellOrderId = Long.parseLong(askParts[0]);
            Long sellUserId  = Long.parseLong(askParts[1]);
            int  sellQty     = Integer.parseInt(askParts[2]);

            // Execution price = price of the resting order (the one placed first)
            BigDecimal execPrice;
            if (buyOrderId < sellOrderId) {
                execPrice = BigDecimal.valueOf(bidPrice).setScale(2, RoundingMode.HALF_UP);
            } else {
                execPrice = BigDecimal.valueOf(askPrice).setScale(2, RoundingMode.HALF_UP);
            }

            // How many shares can be traded?
            int tradedQty = Math.min(buyQty, sellQty);

            log.info("MATCH: {} | BUY order {} x SELL order {} | qty={} price={}",
                    symbol, buyOrderId, sellOrderId, tradedQty, execPrice);

            // 1. Persist the trade
            Trade trade = Trade.builder()
                    .buyOrderId(buyOrderId)
                    .sellOrderId(sellOrderId)
                    .symbol(symbol)
                    .price(execPrice)
                    .quantity(tradedQty)
                    .executedAt(LocalDateTime.now())
                    .build();
            tradeRepository.save(trade);

            // 2. Update LTP in Redis
            redisTemplate.opsForValue()
                    .set("LTP:" + symbol, execPrice.toPlainString());

            // 3. Update Order statuses in MySQL
            updateOrderAfterMatch(buyOrderId, tradedQty);
            updateOrderAfterMatch(sellOrderId, tradedQty);
            publishLtpUpdate(symbol, execPrice.doubleValue());
            // Publish fill notifications to both users
            publishOrderFill(orderRepository.findById(buyOrderId).orElseThrow());
            publishOrderFill(orderRepository.findById(sellOrderId).orElseThrow());

            // 4. Update wallet of SELLER (credit proceeds)
            BigDecimal proceeds = execPrice.multiply(BigDecimal.valueOf(tradedQty));
            creditWallet(sellUserId, proceeds);

            // 5. Update holdings
            updateHoldingOnBuy(buyUserId, symbol, tradedQty, execPrice);
            updateHoldingOnSell(sellUserId, symbol, tradedQty);

            // 6. Update or remove from Redis order book
            int remainingBuy  = buyQty  - tradedQty;
            int remainingSell = sellQty - tradedQty;



            if (remainingBuy == 0) {
                orderBookService.removeBuyOrder(symbol, bestBid.getValue());
            } else {
                String newMember = buyOrderId + ":" + buyUserId + ":" + remainingBuy;
                orderBookService.updateBuyOrderQty(symbol, bestBid.getValue(),
                        newMember, bidPrice);
            }

            if (remainingSell == 0) {
                orderBookService.removeSellOrder(symbol, bestAsk.getValue());
            } else {
                String newMember = sellOrderId + ":" + sellUserId + ":" + remainingSell;
                orderBookService.updateSellOrderQty(symbol, bestAsk.getValue(),
                        newMember, askPrice);
            }
        }
    }

    private void updateOrderAfterMatch(Long orderId, int tradedQty) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        int newFilled = order.getFilledQuantity() + tradedQty;
        order.setFilledQuantity(newFilled);
        order.setUpdatedAt(LocalDateTime.now());
        if (newFilled >= order.getQuantity()) {
            order.setStatus(Order.OrderStatus.FILLED);
        } else {
            order.setStatus(Order.OrderStatus.PARTIAL);
        }
        orderRepository.save(order);
    }

    private void creditWallet(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    private void updateHoldingOnBuy(Long userId, String symbol,
                                    int qty, BigDecimal price) {
        holdingRepository.findByUserIdAndSymbol(userId, symbol)
                .ifPresentOrElse(holding -> {
                    // Recalculate weighted average buy price
                    BigDecimal totalCost = holding.getAvgBuyPrice()
                            .multiply(BigDecimal.valueOf(holding.getQuantity()))
                            .add(price.multiply(BigDecimal.valueOf(qty)));
                    int newQty = holding.getQuantity() + qty;
                    holding.setAvgBuyPrice(
                            totalCost.divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP));
                    holding.setQuantity(newQty);
                    holdingRepository.save(holding);
                }, () -> {
                    Holding h = Holding.builder()
                            .userId(userId).symbol(symbol)
                            .quantity(qty).avgBuyPrice(price)
                            .build();
                    holdingRepository.save(h);
                });
    }

    private void updateHoldingOnSell(Long userId, String symbol, int qty) {
        holdingRepository.findByUserIdAndSymbol(userId, symbol)
                .ifPresent(holding -> {
                    int newQty = holding.getQuantity() - qty;
                    if (newQty <= 0) {
                        holdingRepository.delete(holding);
                    } else {
                        holding.setQuantity(newQty);
                        holdingRepository.save(holding);
                    }
                });
    }
    private void publishLtpUpdate(String symbol, Double price) {
        LtpUpdateDto dto = new LtpUpdateDto(symbol, price, System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/price/" + symbol, dto);
    }
    private void publishOrderFill(Order order) {
        OrderFillNotificationDto dto = new OrderFillNotificationDto(
                order.getId(),
                order.getSymbol(),
                order.getSide().name(),
                order.getFilledQuantity(),
                order.getPrice().doubleValue(),
                order.getStatus().name()
        );
        messagingTemplate.convertAndSendToUser(
                String.valueOf(order.getUserId()),
                "/queue/orders",
                dto
        );
    }

}