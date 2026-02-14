package com.portfolio.api.controller;

import com.portfolio.api.dto.StrategyResponse.*;
import com.portfolio.api.service.StrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/strategies")
@Tag(name = "Strategy Engine", description = "Investment strategies, backtesting, trade signals")
public class StrategyController {

    private final StrategyService strategyService;

    public StrategyController(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @GetMapping
    @Operation(summary = "List all available predefined strategies")
    public ResponseEntity<List<StrategyDefinition>> listStrategies() {
        return ResponseEntity.ok(strategyService.getAvailableStrategies());
    }

    @PostMapping("/backtest")
    @Operation(summary = "Run a strategy backtest on a ticker")
    public ResponseEntity<BacktestResult> backtest(@RequestBody Map<String, Object> body) {
        String strategyId = (String) body.getOrDefault("strategyId", "sma_crossover");
        String ticker = ((String) body.getOrDefault("ticker", "SPY")).toUpperCase();
        int lookbackDays = body.containsKey("lookbackDays")
                ? ((Number) body.get("lookbackDays")).intValue() : 504;

        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) body.getOrDefault("params", Map.of());

        BacktestResult result = strategyService.backtest(strategyId, ticker, lookbackDays, params);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/signals/portfolio/{portfolioId}")
    @Operation(summary = "Generate trade signals for all holdings in a portfolio")
    public ResponseEntity<PortfolioSignals> getSignals(
            Authentication authentication,
            @PathVariable Long portfolioId) {

        PortfolioSignals signals = strategyService.generateSignals(
                portfolioId, authentication.getName());
        return ResponseEntity.ok(signals);
    }
}
