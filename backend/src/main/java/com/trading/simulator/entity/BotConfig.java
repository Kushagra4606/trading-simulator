package com.trading.simulator.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bot_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private BotType botType;

    @Column(nullable = false)
    private int count;  // how many bots of this type to run

    @Column(nullable = false)
    private int aggressionLevel;  // 1–10, controls order size and frequency

    @Column(nullable = false)
    private boolean enabled;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum BotType {
        MARKET_MAKER, MOMENTUM, SENTIMENT, WHALE
    }
}