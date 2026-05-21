package com.trading.simulator.repository;

import com.trading.simulator.entity.PriceCandle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PriceCandleRepository extends JpaRepository<PriceCandle, Long> {

    List<PriceCandle> findBySymbolAndTimeframeOrderByCandleTimeAsc(
            String symbol, String timeframe);

    List<PriceCandle> findBySymbolAndTimeframeAndCandleTimeAfterOrderByCandleTimeAsc(
            String symbol, String timeframe, LocalDateTime after);

    Optional<PriceCandle> findBySymbolAndTimeframeAndCandleTime(
            String symbol, String timeframe, LocalDateTime candleTime);

    @Query("SELECT MAX(c.candleTime) FROM PriceCandle c WHERE c.symbol = :symbol AND c.timeframe = '1m'")
    Optional<LocalDateTime> findLatestCandleTime(@Param("symbol") String symbol);
}
