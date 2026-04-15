package com.trading.simulator.controller;
import com.trading.simulator.dto.PlaceOrderRequest;
import com.trading.simulator.entity.Order;
import com.trading.simulator.security.JwtUtil;
import com.trading.simulator.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PlaceOrderRequest req) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(orderService.placeOrder(userId, req, false));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Order> cancelOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(orderService.cancelOrder(userId, orderId));
    }
    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }

}