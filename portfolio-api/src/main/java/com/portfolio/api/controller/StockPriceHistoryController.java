package com.portfolio.api.controller;

import com.portfolio.api.service.StockPriceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/price-history")
@Tag(name = "Price History", description = "Historical stock price data management")
public class StockPriceHistoryController {

    private final StockPriceHistoryService priceHistoryService;

    public StockPriceHistoryController(StockPriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }

    @PostMapping("/sync/portfolio/{portfolioId}")
    @Operation(summary = "Sync 5-year price history for all tickers in a portfolio + SPY benchmark")
    public ResponseEntity<Map<String, Object>> syncPortfolioHistory(
            Authentication authentication,
            @PathVariable Long portfolioId) {

        Map<String, Object> results = priceHistoryService.syncPortfolioPriceHistory(
                portfolioId, authentication.getName());

        return ResponseEntity.ok(results);
    }

    @PostMapping("/sync/ticker/{ticker}")
    @Operation(summary = "Sync 5-year price history for a single ticker")
    public ResponseEntity<Map<String, Object>> syncTickerHistory(@PathVariable String ticker) {
        int count = priceHistoryService.syncTickerHistory(ticker.toUpperCase());
        return ResponseEntity.ok(Map.of(
                "ticker", ticker.toUpperCase(),
                "recordsSynced", count
        ));
    }

    @GetMapping("/tickers")
    @Operation(summary = "List all tickers with stored price history")
    public ResponseEntity<List<String>> getStoredTickers() {
        return ResponseEntity.ok(priceHistoryService.getStoredTickers());
    }

    @GetMapping("/count/{ticker}")
    @Operation(summary = "Get record count for a ticker")
    public ResponseEntity<Map<String, Object>> getRecordCount(@PathVariable String ticker) {
        long count = priceHistoryService.getRecordCount(ticker.toUpperCase());
        return ResponseEntity.ok(Map.of(
                "ticker", ticker.toUpperCase(),
                "records", count
        ));
    }
}
