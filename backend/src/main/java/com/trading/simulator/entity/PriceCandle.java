package com.trading.simulator.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_candles", indexes = {
        @Index(name = "idx_candle_symbol_tf_time", columnList = "symbol, timeframe, candle_time")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceCandle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String symbol;

    // "1m", "5m", "15m", "1D"
    @Column(nullable = false, length = 5)
    private String timeframe;

    @Column(name = "candle_time", nullable = false)
    private LocalDateTime candleTime;   // the START of this candle window

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal open;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal high;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal low;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal close;

    @Column(nullable = false)
    private Long volume;    // total shares traded in this candle
}