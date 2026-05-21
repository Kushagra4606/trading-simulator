// TradeRepository.java
package com.trading.simulator.repository;

import com.trading.simulator.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findBySymbolOrderByExecutedAtDesc(String symbol);

    @Query("SELECT DISTINCT t.symbol FROM Trade t")
    List<String> findDistinctSymbols();

    List<Trade> findBySymbolAndExecutedAtBetween(String symbol, LocalDateTime from, LocalDateTime to);
}

