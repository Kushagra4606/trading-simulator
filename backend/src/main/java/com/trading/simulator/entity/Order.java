package com.trading.simulator.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;           // BUY or SELL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;           // MARKET or LIMIT

    @Column(precision = 10, scale = 2)
    private BigDecimal price;         // null for MARKET orders

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @Builder.Default
    private Integer filledQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;       // PENDING, PARTIAL, FILLED, CANCELLED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isBot = false;

    private LocalDateTime updatedAt;

    // Enums defined as inner enums or separate files
    public enum OrderSide { BUY, SELL }
    public enum OrderType { MARKET, LIMIT }
    public enum OrderStatus { PENDING, PARTIAL, FILLED, CANCELLED }
}