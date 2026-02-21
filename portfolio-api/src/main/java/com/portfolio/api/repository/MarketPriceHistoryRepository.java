package com.portfolio.api.repository;

import com.portfolio.api.model.MarketPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MarketPriceHistoryRepository extends JpaRepository<MarketPriceHistory, Long> {

    List<MarketPriceHistory> findByTickerAndAssetClassOrderByTradeDateAsc(String ticker, String assetClass);

    @Query("SELECT COUNT(m) FROM MarketPriceHistory m")
    long countAllRecords();

    @Query("SELECT MAX(m.tradeDate) FROM MarketPriceHistory m")
    LocalDate findLatestTradeDate();
}
