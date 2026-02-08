package com.portfolio.api.repository;

import com.portfolio.api.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Portfolio entity operations.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUserId(Long userId);

    long countByUserId(Long userId);
}
