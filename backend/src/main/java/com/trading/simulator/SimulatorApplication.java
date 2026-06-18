package com.trading.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@org.springframework.retry.annotation.EnableRetry
@EnableScheduling
public class SimulatorApplication {
	public static void main(String[] args) {
		SpringApplication.run(SimulatorApplication.class, args);
	}
}