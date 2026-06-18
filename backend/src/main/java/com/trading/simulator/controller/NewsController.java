package com.trading.simulator.controller;

import com.trading.simulator.entity.NewsEvent;
import com.trading.simulator.repository.NewsEventRepository;
import com.trading.simulator.service.NewsEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsEngineService newsEngineService;
    private final NewsEventRepository newsEventRepository;

    // Frontend news feed — latest 20 events
    @GetMapping
    public List<NewsEvent> getRecentNews() {
        return newsEventRepository.findTop20ByOrderByCreatedAtDesc();
    }

    // Admin manual trigger
    @PostMapping("/trigger")
    public ResponseEntity<NewsEvent> triggerNews(
            @RequestParam NewsEvent.Scope scope,
            @RequestParam(required = false) String target) {
        NewsEvent event = newsEngineService.triggerManualNews(scope, target);
        if (event == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(event);
    }
}
