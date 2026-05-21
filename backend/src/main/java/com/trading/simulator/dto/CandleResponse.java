package com.trading.simulator.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CandleResponse {

    private List<CandleDto> candles;
    private Map<String, List<BigDecimal>> indicators;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CandleDto {
        private LocalDateTime time;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private Long volume;
    }
}