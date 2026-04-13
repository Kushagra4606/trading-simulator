// TradeRepository.java
package com.trading.simulator.repository;

import com.trading.simulator.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findBySymbolOrderByExecutedAtDesc(String symbol);
}