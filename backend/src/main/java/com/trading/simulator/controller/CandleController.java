package com.trading.simulator.controller;

import com.trading.simulator.dto.CandleResponse;
import com.trading.simulator.service.CandleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candles")
@RequiredArgsConstructor
public class CandleController {

    private final CandleService candleService;

    @GetMapping("/{symbol}")
    public CandleResponse getCandles(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1m")  String timeframe,
            @RequestParam(defaultValue = "200") int limit,
            @RequestParam(defaultValue = "false") boolean sma20,
            @RequestParam(defaultValue = "false") boolean sma50,
            @RequestParam(defaultValue = "false") boolean ema20,
            @RequestParam(defaultValue = "false") boolean rsi,
            @RequestParam(defaultValue = "false") boolean macd,
            @RequestParam(defaultValue = "false") boolean bb,
            @RequestParam(defaultValue = "false") boolean vwap
    ) {
        return candleService.getCandles(symbol.toUpperCase(), timeframe, limit,
                sma20, sma50, ema20, rsi, macd, bb, vwap);
    }
}