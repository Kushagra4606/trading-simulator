package com.trading.simulator.service;

import com.trading.simulator.dto.PlaceOrderRequest;
import com.trading.simulator.entity.*;
import com.trading.simulator.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;
    private final HoldingRepository holdingRepository;
    private final OrderBookService orderBookService;
    private final MatchingEngine matchingEngine;
    private final StockService stockService;

    @Transactional
    public Order placeOrder(Long userId, PlaceOrderRequest req) {

        // Determine execution price
        BigDecimal price;
        if (req.getOrderType() == Order.OrderType.MARKET) {
            // For market orders, use current LTP as the price
            price = stockService.getLtpAsBigDecimal(req.getSymbol());
        } else {
            price = req.getPrice();
        }

        // Validate
        if (req.getSide() == Order.OrderSide.BUY) {
            validateAndDeductWalletForBuy(userId, price, req.getQuantity());
        } else {
            validateHoldingsForSell(userId, req.getSymbol(), req.getQuantity());
        }

        // Persist order
        Order order = Order.builder()
                .userId(userId)
                .symbol(req.getSymbol())
                .side(req.getSide())
                .type(req.getOrderType())
                .price(price)
                .quantity(req.getQuantity())
                .filledQuantity(0)
                .status(Order.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        order = orderRepository.save(order);

        // Add to Redis order book
        if (req.getSide() == Order.OrderSide.BUY) {
            orderBookService.addBuyOrder(req.getSymbol(), order.getId(),
                    userId, req.getQuantity(), price);
        } else {
            orderBookService.addSellOrder(req.getSymbol(), order.getId(),
                    userId, req.getQuantity(), price);
        }

        // Trigger matching engine
        matchingEngine.match(req.getSymbol());

        return orderRepository.findById(order.getId()).orElseThrow();
    }

    private void validateAndDeductWalletForBuy(Long userId, BigDecimal price, int qty) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        BigDecimal required = price.multiply(BigDecimal.valueOf(qty));
        if (wallet.getBalance().compareTo(required) < 0) {
            throw new RuntimeException("Insufficient balance. Required: ₹"
                    + required + ", Available: ₹" + wallet.getBalance());
        }
        wallet.setBalance(wallet.getBalance().subtract(required));
        walletRepository.save(wallet);
    }

    private void validateHoldingsForSell(Long userId, String symbol, int qty) {
        Holding holding = holdingRepository.findByUserIdAndSymbol(userId, symbol)
                .orElseThrow(() -> new RuntimeException("No holdings for " + symbol));
        if (holding.getQuantity() < qty) {
            throw new RuntimeException("Insufficient shares. You own: "
                    + holding.getQuantity() + ", trying to sell: " + qty);
        }
    }

    @Transactional
    public Order cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (order.getStatus() == Order.OrderStatus.FILLED
                || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel a " + order.getStatus() + " order");
        }

        // Remove from Redis
        String member = order.getId() + ":" + userId + ":"
                + (order.getQuantity() - order.getFilledQuantity());
        if (order.getSide() == Order.OrderSide.BUY) {
            orderBookService.removeBuyOrder(order.getSymbol(), member);
            // Refund wallet
            BigDecimal refund = order.getPrice()
                    .multiply(BigDecimal.valueOf(order.getQuantity() - order.getFilledQuantity()));
            creditWalletOnCancel(userId, refund);
        } else {
            orderBookService.removeSellOrder(order.getSymbol(), member);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private void creditWalletOnCancel(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}