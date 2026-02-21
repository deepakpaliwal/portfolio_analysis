package com.portfolio.api.repository;

import com.portfolio.api.model.BatchTickerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchTickerConfigRepository extends JpaRepository<BatchTickerConfig, Long> {

    Optional<BatchTickerConfig> findByTicker(String ticker);

    List<BatchTickerConfig> findByEnabledTrueOrderByTickerAsc();

    List<BatchTickerConfig> findAllByOrderByTickerAsc();

    boolean existsByTicker(String ticker);

    long countByEnabledTrue();

    long countByEnabledFalse();

    long countByLastRunStatus(String status);

    @Query("SELECT COALESCE(SUM(b.recordCount),0) FROM BatchTickerConfig b")
    long sumRecordCount();

    @Query("SELECT MAX(b.lastRunAt) FROM BatchTickerConfig b")
    LocalDateTime findLastBatchRunAt();

    @Query("SELECT b.assetClass, COUNT(b) FROM BatchTickerConfig b GROUP BY b.assetClass")
    List<Object[]> countByAssetClass();
}
