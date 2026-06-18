
package com.trading.simulator.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "stocks")
@Data
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String symbol;          // e.g. "RELIANCE"

    @Column(nullable = false)
    private String name;            // e.g. "Reliance Industries"

    @Column(nullable = false)
    private String sector;          // e.g. "Energy"

    @Column(nullable = false)
    private String marketCap;       // "LARGE", "MID", "SMALL"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;   // Starting price, seeded into Redis as LTP

    @Column(nullable = false)
    private Double circuitLimitPercent; // e.g. 10.0 means ±10%

    @Column(nullable = false, precision = 4, scale = 3)
    private java.math.BigDecimal volatility = new java.math.BigDecimal("0.020"); // base daily volatility e.g. 0.02 = 2%
}