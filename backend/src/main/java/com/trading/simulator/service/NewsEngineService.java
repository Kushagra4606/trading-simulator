package com.trading.simulator.service;

import com.trading.simulator.entity.NewsEvent;
import com.trading.simulator.entity.Stock;
import com.trading.simulator.repository.NewsEventRepository;
import com.trading.simulator.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsEngineService {

    private final NewsEventRepository newsEventRepository;
    private final StockRepository stockRepository;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final Random RNG = new Random();

    // Half-life in minutes: sentiment decays to 50% of original after this many minutes
    private static final double HALF_LIFE_MINUTES = 10.0;

    // ─── Template bank ───────────────────────────────────────────────────────

    // Format: {company} or {sector} will be substituted at generation time
    private static final List<String> POSITIVE_COMPANY = List.of(
        "{company} reports strong quarterly earnings, profit up 18% YoY",
        "{company} wins major government contract worth ₹2,400 crore",
        "{company} announces share buyback programme at premium to market price",
        "{company} receives SEBI approval for overseas acquisition",
        "{company} board declares special dividend amid record cash reserves",
        "{company} Q3 margins expand sharply, analysts raise price targets",
        "{company} signs strategic partnership with global tech giant",
        "{company} reports highest-ever order book, guidance raised for FY26"
    );

    private static final List<String> NEGATIVE_COMPANY = List.of(
        "{company} misses earnings estimates, revenue down 12% sequentially",
        "{company} faces SEBI investigation over alleged insider trading",
        "{company} promoter pledges additional stake, raises liquidity concerns",
        "{company} major client cancels ₹800 crore contract",
        "{company} CFO resigns unexpectedly, board cites strategic differences",
        "{company} Q3 operating margins compress on rising input costs",
        "{company} faces class-action suit over product quality issues",
        "{company} credit rating downgraded by ICRA on rising debt levels"
    );

    private static final List<String> POSITIVE_SECTOR = List.of(
        "{sector} sector gets booster shot as govt announces PLI scheme expansion",
        "RBI policy stance boosts {sector} stocks; analysts turn bullish",
        "FII inflows surge into {sector} amid improving global outlook",
        "Budget allocation for {sector} doubled; stocks across sector rally",
        "Strong GST collections signal robust demand in {sector} space",
        "Export data shows {sector} sector hitting multi-year highs"
    );

    private static final List<String> NEGATIVE_SECTOR = List.of(
        "{sector} sector under pressure as crude oil prices spike 6%",
        "RBI rate hike fears weigh on {sector} stocks across the board",
        "Regulatory tightening in {sector} space rattles investor sentiment",
        "Global slowdown fears trigger FII selling in {sector} names",
        "{sector} companies face margin squeeze as input costs surge",
        "New govt compliance norms for {sector} seen as near-term headwind"
    );

    private static final List<String> POSITIVE_MACRO = List.of(
        "India GDP growth revised upward to 7.4% for FY26, beats estimates",
        "CPI inflation cools to 4.1%, opens door for RBI rate cut",
        "FII inflows touch ₹12,000 crore in a single session — markets cheer",
        "India manufacturing PMI hits 14-month high at 57.3",
        "Moody's upgrades India outlook to positive on fiscal consolidation",
        "Rupee strengthens to 82.40 against dollar on strong macro data"
    );

    private static final List<String> NEGATIVE_MACRO = List.of(
        "US Fed signals higher-for-longer rates; emerging markets under pressure",
        "India WPI inflation spikes unexpectedly to 6.8% in latest reading",
        "FII outflows cross ₹15,000 crore this week on global risk-off",
        "Crude oil touches $98/barrel — inflation and CAD concerns resurface",
        "India PMI services dips to 51.2, signals slowing momentum",
        "Rupee hits 84.60 against dollar, RBI intervention likely"
    );

    // ─── Sentiment score ranges per polarity ─────────────────────────────────
    // Positive: +0.30 to +0.90  |  Negative: -0.30 to -0.90
    private double randomPositiveSentiment() {
        return 0.30 + RNG.nextDouble() * 0.60;
    }

    private double randomNegativeSentiment() {
        return -(0.30 + RNG.nextDouble() * 0.60);
    }

    // ─── Scheduled auto-generation ───────────────────────────────────────────
    // Fires every 3 minutes; 70% chance it actually generates an event (not every tick)
    @Scheduled(fixedDelay = 3 * 60 * 1000)
    public void scheduledNewsGeneration() {
        if (RNG.nextDouble() > 0.70) {
            log.debug("[NewsEngine] Skipped this tick (random suppression)");
            return;
        }
        // Pick random scope weighted: 50% COMPANY, 30% SECTOR, 20% MACRO
        double roll = RNG.nextDouble();
        if (roll < 0.50) {
            generateCompanyNews();
        } else if (roll < 0.80) {
            generateSectorNews();
        } else {
            generateMacroNews();
        }
    }

    // ─── Manual trigger (for admin endpoint) ─────────────────────────────────
    public NewsEvent triggerManualNews(NewsEvent.Scope scope, String target) {
        return switch (scope) {
            case COMPANY -> generateCompanyNews(target);
            case SECTOR  -> generateSectorNews(target);
            case MACRO   -> generateMacroNews();
        };
    }

    // ─── Company-scoped news ─────────────────────────────────────────────────
    private NewsEvent generateCompanyNews() {
        List<Stock> all = stockRepository.findAll();
        if (all.isEmpty()) return null;
        Stock stock = all.get(RNG.nextInt(all.size()));
        return generateCompanyNews(stock.getSymbol());
    }

    private NewsEvent generateCompanyNews(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol).orElse(null);
        if (stock == null) return null;

        boolean positive = RNG.nextBoolean();
        List<String> templates = positive ? POSITIVE_COMPANY : NEGATIVE_COMPANY;
        String headline = pick(templates).replace("{company}", stock.getName());
        double score = positive ? randomPositiveSentiment() : randomNegativeSentiment();

        NewsEvent event = persist(headline, NewsEvent.Scope.COMPANY, symbol, score);
        writeSentimentToRedis(symbol, score);
        broadcast(event);

        log.info("[NewsEngine] COMPANY event → {} | score={} | {}", symbol, score, headline);
        return event;
    }

    // ─── Sector-scoped news ──────────────────────────────────────────────────
    private NewsEvent generateSectorNews() {
        List<Stock> all = stockRepository.findAll();
        if (all.isEmpty()) return null;
        String sector = all.get(RNG.nextInt(all.size())).getSector();
        return generateSectorNews(sector);
    }

    private NewsEvent generateSectorNews(String sector) {
        boolean positive = RNG.nextBoolean();
        List<String> templates = positive ? POSITIVE_SECTOR : NEGATIVE_SECTOR;
        String headline = pick(templates).replace("{sector}", sector);
        double score = positive ? randomPositiveSentiment() : randomNegativeSentiment();

        NewsEvent event = persist(headline, NewsEvent.Scope.SECTOR, sector, score);

        // Fan-out to all stocks in this sector
        List<Stock> sectorStocks = stockRepository.findBySector(sector);
        for (Stock s : sectorStocks) {
            // Sector news has slightly dampened impact per stock (80% of base score)
            double dampened = score * 0.80;
            writeSentimentToRedis(s.getSymbol(), dampened);
        }
        broadcast(event);

        log.info("[NewsEngine] SECTOR event → {} ({} stocks) | score={} | {}",
                sector, sectorStocks.size(), score, headline);
        return event;
    }

    // ─── Macro-scoped news ───────────────────────────────────────────────────
    private NewsEvent generateMacroNews() {
        boolean positive = RNG.nextBoolean();
        List<String> templates = positive ? POSITIVE_MACRO : NEGATIVE_MACRO;
        String headline = pick(templates);
        double score = positive ? randomPositiveSentiment() : randomNegativeSentiment();

        NewsEvent event = persist(headline, NewsEvent.Scope.MACRO, null, score);

        // Macro news affects ALL stocks, dampened to 50% of base score
        List<Stock> all = stockRepository.findAll();
        for (Stock s : all) {
            writeSentimentToRedis(s.getSymbol(), score * 0.50);
        }
        broadcast(event);

        log.info("[NewsEngine] MACRO event → ALL stocks | score={} | {}", score, headline);
        return event;
    }

    // ─── Sentiment decay recompute ───────────────────────────────────────────
    // Runs every 15 seconds: reads SENTIMENT_EVENT:* keys, computes exponential
    // decay, writes decayed value back to SENTIMENT:{symbol}
    @Scheduled(fixedDelay = 15_000)
    public void recomputeDecay() {
        var keys = redisTemplate.keys("SENTIMENT_EVENT:*");
        if (keys == null || keys.isEmpty()) return;

        long now = System.currentTimeMillis();

        for (String key : keys) {
            String raw = redisTemplate.opsForValue().get(key);
            if (raw == null) continue;

            String[] parts = raw.split(":");
            if (parts.length != 2) continue;

            double magnitude  = Double.parseDouble(parts[0]);
            long   eventEpoch = Long.parseLong(parts[1]);

            double elapsedMinutes = (now - eventEpoch) / 60_000.0;
            double decayed = magnitude * Math.exp(
                    -elapsedMinutes * Math.log(2) / HALF_LIFE_MINUTES);

            // If decayed value is negligible, clean up both keys
            if (Math.abs(decayed) < 0.02) {
                String symbol = key.replace("SENTIMENT_EVENT:", "");
                redisTemplate.delete(key);
                redisTemplate.opsForValue().set("SENTIMENT:" + symbol, "0.0");
                log.debug("[NewsEngine] Sentiment fully decayed for {}", symbol);
                continue;
            }

            String symbol = key.replace("SENTIMENT_EVENT:", "");
            redisTemplate.opsForValue().set(
                    "SENTIMENT:" + symbol,
                    String.format("%.4f", decayed)
            );
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private String pick(List<String> list) {
        return list.get(RNG.nextInt(list.size()));
    }

    private NewsEvent persist(String headline, NewsEvent.Scope scope,
                               String target, double score) {
        NewsEvent event = NewsEvent.builder()
                .headline(headline)
                .scope(scope)
                .target(target)
                .sentimentScore(BigDecimal.valueOf(score).setScale(2,
                        java.math.RoundingMode.HALF_UP))
                .createdAt(LocalDateTime.now())
                .build();
        return newsEventRepository.save(event);
    }

    private void writeSentimentToRedis(String symbol, double score) {
        // Store raw event data for decay computation
        String raw = score + ":" + System.currentTimeMillis();
        redisTemplate.opsForValue().set("SENTIMENT_EVENT:" + symbol, raw);
        // Write initial flat value immediately so bots react right away
        redisTemplate.opsForValue().set("SENTIMENT:" + symbol,
                String.format("%.4f", score));
    }

    private void broadcast(NewsEvent event) {
        // Broadcasts to frontend news feed via WebSocket
        messagingTemplate.convertAndSend("/topic/news", event);
    }
}
