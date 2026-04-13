// OrderRepository.java
package com.trading.simulator.repository;

import com.trading.simulator.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByUserIdAndStatusIn(Long userId, List<Order.OrderStatus> statuses);
}