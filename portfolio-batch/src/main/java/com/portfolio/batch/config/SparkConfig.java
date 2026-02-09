package com.portfolio.batch.config;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Apache Spark configuration for batch data processing.
 *
 * <p>Configures the SparkSession with environment-aware settings.
 * In local mode, runs with local[*]; in cluster mode, connects to a Spark master.</p>
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@Configuration
public class SparkConfig {

    @Value("${spark.master:local[*]}")
    private String sparkMaster;

    @Value("${spark.app-name:portfolio-batch}")
    private String appName;

    @Value("${spark.driver-memory:2g}")
    private String driverMemory;

    @Value("${spark.executor-memory:2g}")
    private String executorMemory;

    /**
     * Creates and configures the SparkSession bean.
     *
     * @return configured SparkSession instance
     */
    @Bean
    public SparkSession sparkSession() {
//        SparkConf conf = new SparkConf()
//                .setAppName(appName)
//                .setMaster(sparkMaster)
//                .set("spark.driver.memory", driverMemory)
//                .set("spark.executor.memory", executorMemory)
//                .set("spark.sql.shuffle.partitions", "10")
//                .set("spark.ui.enabled", "false");
//
//        return SparkSession.builder()
//                .config(conf)
//                .getOrCreate();
        return null;
    }
}
