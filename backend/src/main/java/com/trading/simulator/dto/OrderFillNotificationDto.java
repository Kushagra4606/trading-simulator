package com.trading.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderFillNotificationDto {
    private Long orderId;
    private String symbol;
    private String side;       // BUY or SELL
    private Integer filledQty;
    private Double price;
    private String status;     // PARTIAL or FILLED
}