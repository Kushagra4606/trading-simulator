package com.trading.simulator.dto;

import com.trading.simulator.entity.Order;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlaceOrderRequest {

    @NotBlank
    private String symbol;

    @NotNull
    private Order.OrderSide side;      // BUY or SELL

    @NotNull
    private Order.OrderType orderType; // MARKET or LIMIT

    // Required only for LIMIT orders
    private BigDecimal price;

    @NotNull
    @Min(1)
    private Integer quantity;
}