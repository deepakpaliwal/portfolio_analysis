package com.portfolio.api.repository;

import com.portfolio.api.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for Transaction entity operations.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByHoldingId(Long holdingId);

    @Query("SELECT t FROM Transaction t WHERE t.holding.portfolio.id = :portfolioId ORDER BY t.executedAt DESC")
    List<Transaction> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    @Query("SELECT t FROM Transaction t WHERE t.holding.portfolio.id = :portfolioId AND t.executedAt BETWEEN :start AND :end ORDER BY t.executedAt DESC")
    List<Transaction> findByPortfolioIdAndDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
