package com.portfolio.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Portfolio Batch Processing Application.
 *
 * <p>This application performs deep data analysis, recommendation generation,
 * and portfolio optimization using Apache Spark and Spring Batch.</p>
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class PortfolioBatchApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PortfolioBatchApplication.class, args);
    }
}
