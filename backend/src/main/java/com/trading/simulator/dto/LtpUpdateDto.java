package com.trading.simulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LtpUpdateDto {
    private String symbol;
    private Double price;
    private Long timestamp;
}