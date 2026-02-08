package com.portfolio.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Portfolio Analysis REST API.
 *
 * <p>This application provides REST endpoints for portfolio management,
 * risk analytics, stock screening, strategy execution, and automated trading.</p>
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class PortfolioApiApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PortfolioApiApplication.class, args);
    }
}
