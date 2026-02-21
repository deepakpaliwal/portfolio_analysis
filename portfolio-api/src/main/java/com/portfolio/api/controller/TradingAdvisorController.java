package com.portfolio.api.controller;

import com.portfolio.api.dto.TradingAdvisorResponse;
import com.portfolio.api.service.TradingAdvisorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/advisor")
@Tag(name = "Trading Advisor", description = "Single-stock trading advisor with persisted history, indicators, risk and chart data")
public class TradingAdvisorController {

    private final TradingAdvisorService tradingAdvisorService;

    public TradingAdvisorController(TradingAdvisorService tradingAdvisorService) {
        this.tradingAdvisorService = tradingAdvisorService;
    }

    @GetMapping("/{ticker}")
    @Operation(summary = "Analyze one ticker and return advisor data")
    public ResponseEntity<TradingAdvisorResponse> analyze(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "10000") BigDecimal positionValue,
            @RequestParam(defaultValue = "252") int lookbackDays) {
        return ResponseEntity.ok(tradingAdvisorService.analyze(ticker, positionValue, lookbackDays));
    }
}
