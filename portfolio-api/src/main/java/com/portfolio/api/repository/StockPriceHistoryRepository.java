package com.portfolio.api.repository;

import com.portfolio.api.model.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {

    List<StockPriceHistory> findByTickerAndTradeDateBetweenOrderByTradeDateAsc(
            String ticker, LocalDate from, LocalDate to);

    List<StockPriceHistory> findByTickerOrderByTradeDateAsc(String ticker);

    Optional<StockPriceHistory> findTopByTickerOrderByTradeDateDesc(String ticker);

    long countByTicker(String ticker);

    void deleteByTicker(String ticker);

    @Query("SELECT DISTINCT s.ticker FROM StockPriceHistory s")
    List<String> findDistinctTickers();
}
