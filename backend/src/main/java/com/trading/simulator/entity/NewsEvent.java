package com.trading.simulator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "news_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String headline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Scope scope;

    @Column(length = 50)
    private String target; // symbol if COMPANY, sector name if SECTOR, null if MACRO

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal sentimentScore; // -1.00 to +1.00

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum Scope {
        COMPANY, SECTOR, MACRO
    }
}
