package com.trading.simulator.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "holdings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "symbol"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private Integer quantity;

    // Weighted average buy price
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal avgBuyPrice;
}