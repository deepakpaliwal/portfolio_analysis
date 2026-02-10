package com.portfolio.api.repository;

import com.portfolio.api.model.ScreenerReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenerReportRepository extends JpaRepository<ScreenerReport, Long> {

    List<ScreenerReport> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ScreenerReport> findByUserIdAndReportTypeOrderByCreatedAtDesc(Long userId, String reportType);
}
