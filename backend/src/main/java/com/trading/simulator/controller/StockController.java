
package com.trading.simulator.controller;

import com.trading.simulator.dto.StockResponse;
import com.trading.simulator.entity.Stock;
import com.trading.simulator.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // GET /api/stocks
    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        List<Stock> stocks = stockService.getAllStocks();
        List<StockResponse> response = stocks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // GET /api/stocks/{symbol}
    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> getStock(@PathVariable String symbol) {
        return stockService.getStockBySymbol(symbol)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/stocks/{symbol}/ltp
    @GetMapping("/{symbol}/ltp")
    public ResponseEntity<String> getLtp(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getLtp(symbol));
    }

    private StockResponse toResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .sector(stock.getSector())
                .marketCap(stock.getMarketCap())
                .basePrice(stock.getBasePrice())
                .circuitLimitPercent(stock.getCircuitLimitPercent())
                .currentPrice(stockService.getLtp(stock.getSymbol()))
                .build();
    }
}