package com.trading.simulator.service;

import com.trading.simulator.entity.BotConfig;
import com.trading.simulator.entity.User;
import com.trading.simulator.repository.BotConfigRepository;
import com.trading.simulator.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BotScheduler {

    private final BotConfigRepository botConfigRepository;
    private final BotAccountService botAccountService;
    private final MarketMakerBotService marketMakerBotService;
    private final MomentumBotService momentumBotService;
    private final SentimentBotService sentimentBotService;
    private final WhaleBotService whaleBotService;
    private final StockRepository stockRepository;

    private final Map<String, List<User>> botAccounts = new HashMap<>();

    // 16 threads: handles 200 bots with jitter across 4 types comfortably
    private final ScheduledExecutorService jitterExecutor =
            Executors.newScheduledThreadPool(16);

    @PostConstruct
    public void initBotAccounts() {
        List<BotConfig> configs = botConfigRepository.findByEnabledTrue();
        for (BotConfig config : configs) {
            String type = config.getBotType().name();
            List<User> bots = botAccountService.provisionBots(type, config.getCount());
            botAccounts.put(type, bots);
            log.info("[BotScheduler] Provisioned {} {} bots", bots.size(), type);
        }
    }
    @PreDestroy
    public void shutdown() {
        log.info("[BotScheduler] Shutting down jitter executor...");
        jitterExecutor.shutdown();
        try {
            if (!jitterExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                jitterExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            jitterExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("[BotScheduler] Jitter executor shut down.");
    }
    // ── Market Makers: every 5s ───────────────────────────────────────────────
    @Scheduled(fixedDelay = 5000)
    public void runMarketMakers() {
        dispatchBots("MARKET_MAKER", 4000,
                (bot, symbol) -> marketMakerBotService.act(bot, symbol));
    }

    // ── Momentum Bots: every 8s ───────────────────────────────────────────────
    @Scheduled(fixedDelay = 8000)
    public void runMomentumBots() {
        dispatchBots("MOMENTUM", 6000,
                (bot, symbol) -> momentumBotService.act(bot, symbol));
    }

    // ── Sentiment Bots: every 10s ─────────────────────────────────────────────
    @Scheduled(fixedDelay = 10000)
    public void runSentimentBots() {
        dispatchBots("SENTIMENT", 8000,
                (bot, symbol) -> sentimentBotService.act(bot, symbol));
    }

    // ── Whale Bots: every 45s ─────────────────────────────────────────────────
    @Scheduled(fixedDelay = 45000)
    public void runWhaleBots() {
        dispatchBots("WHALE", 15000,
                (bot, symbol) -> whaleBotService.act(bot, symbol));
    }

    // ── Shared dispatch helper ────────────────────────────────────────────────

    @FunctionalInterface
    interface BotAction {
        void execute(User bot, String symbol);
    }

    private void dispatchBots(String type, long maxJitterMs, BotAction action) {
        List<User> bots = botAccounts.getOrDefault(type, List.of());
        List<String> symbols = getAllSymbols();
        if (bots.isEmpty() || symbols.isEmpty()) return;

        for (User bot : bots) {
            long jitter = (long)(Math.random() * maxJitterMs);
            String symbol = symbols.get((int)(Math.random() * symbols.size()));
            jitterExecutor.schedule(
                    () -> action.execute(bot, symbol),
                    jitter,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    

    private List<String> getAllSymbols() {
        return stockRepository.findAll()
                .stream().map(s -> s.getSymbol()).toList();
    }
}