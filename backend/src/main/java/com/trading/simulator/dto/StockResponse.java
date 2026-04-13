package com.trading.simulator.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class StockResponse {
    private Long id;
    private String symbol;
    private String name;
    private String sector;
    private String marketCap;
    private BigDecimal basePrice;
    private Double circuitLimitPercent;
    private String currentPrice;  // Comes from Redis LTP
}