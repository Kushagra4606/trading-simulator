package com.trading.simulator.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long buyOrderId;

    @Column(nullable = false)
    private Long sellOrderId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;         // execution price

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime executedAt;
}