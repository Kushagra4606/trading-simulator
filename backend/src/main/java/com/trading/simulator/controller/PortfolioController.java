package com.trading.simulator.controller;

import com.trading.simulator.entity.Holding;
import com.trading.simulator.repository.HoldingRepository;
import com.trading.simulator.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final HoldingRepository holdingRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/holdings")
    public ResponseEntity<List<Holding>> getHoldings(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(holdingRepository.findByUserId(userId));
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token);
    }
}