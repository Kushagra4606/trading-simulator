package com.trading.simulator.config;

import com.trading.simulator.entity.BotConfig;
import com.trading.simulator.entity.BotConfig.BotType;
import com.trading.simulator.repository.BotConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotSeeder implements CommandLineRunner {

    private final BotConfigRepository botConfigRepository;

    @Override
    public void run(String... args) {
        if (botConfigRepository.count() > 0) return; // already seeded

        botConfigRepository.save(BotConfig.builder()
                .botType(BotType.MARKET_MAKER).count(80)
                .aggressionLevel(4).enabled(true).build());

        botConfigRepository.save(BotConfig.builder()
                .botType(BotType.MOMENTUM).count(60)
                .aggressionLevel(6).enabled(true).build());

        botConfigRepository.save(BotConfig.builder()
                .botType(BotType.SENTIMENT).count(40)
                .aggressionLevel(5).enabled(true).build());

        botConfigRepository.save(BotConfig.builder()
                .botType(BotType.WHALE).count(20)
                .aggressionLevel(9).enabled(true).build());

        System.out.println("[BotSeeder] Bot configs seeded.");
    }
}