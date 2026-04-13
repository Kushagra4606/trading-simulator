package com.trading.simulator.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class ConnectionVerifier {

    private static final Logger log = LoggerFactory.getLogger(ConnectionVerifier.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostConstruct
    public void verify() {
        // MySQL check
        try (Connection conn = dataSource.getConnection()) {
            log.info("✅ MySQL connected: {}", conn.getMetaData().getURL());
        } catch (Exception e) {
            log.error("❌ MySQL connection failed: {}", e.getMessage());
        }

        // Redis check
        try {
            redisTemplate.opsForValue().set("health_check", "ok");
            String val = redisTemplate.opsForValue().get("health_check");
            log.info("✅ Redis connected, test value: {}", val);
        } catch (Exception e) {
            log.error("❌ Redis connection failed: {}", e.getMessage());
        }
    }
}
