package com.trading.simulator.repository;

import com.trading.simulator.entity.BotConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BotConfigRepository extends JpaRepository<BotConfig, Long> {
    List<BotConfig> findByEnabledTrue();
}