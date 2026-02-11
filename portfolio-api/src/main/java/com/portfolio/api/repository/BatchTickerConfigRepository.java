package com.portfolio.api.repository;

import com.portfolio.api.model.BatchTickerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchTickerConfigRepository extends JpaRepository<BatchTickerConfig, Long> {

    Optional<BatchTickerConfig> findByTicker(String ticker);

    List<BatchTickerConfig> findByEnabledTrueOrderByTickerAsc();

    List<BatchTickerConfig> findAllByOrderByTickerAsc();

    boolean existsByTicker(String ticker);
}
