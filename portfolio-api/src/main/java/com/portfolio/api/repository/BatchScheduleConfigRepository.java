package com.portfolio.api.repository;

import com.portfolio.api.model.BatchScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatchScheduleConfigRepository extends JpaRepository<BatchScheduleConfig, Long> {

    Optional<BatchScheduleConfig> findByConfigKey(String configKey);
}
