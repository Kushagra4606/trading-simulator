package com.trading.simulator.repository;

import com.trading.simulator.entity.NewsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NewsEventRepository extends JpaRepository<NewsEvent, Long> {
    List<NewsEvent> findTop20ByOrderByCreatedAtDesc();
}
