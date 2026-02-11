package com.portfolio.api.controller;

import com.portfolio.api.dto.RiskAnalyticsResponse;
import com.portfolio.api.service.RiskAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk")
@Tag(name = "Risk Analytics", description = "Portfolio risk analytics endpoints")
public class RiskAnalyticsController {

    private final RiskAnalyticsService riskAnalyticsService;

    public RiskAnalyticsController(RiskAnalyticsService riskAnalyticsService) {
        this.riskAnalyticsService = riskAnalyticsService;
    }

    @GetMapping("/portfolio/{portfolioId}")
    @Operation(summary = "Compute comprehensive risk analytics for a portfolio")
    public ResponseEntity<RiskAnalyticsResponse> getRiskAnalytics(
            Authentication authentication,
            @PathVariable Long portfolioId,
            @RequestParam(defaultValue = "0.95") double confidenceLevel,
            @RequestParam(defaultValue = "1") int timeHorizonDays,
            @RequestParam(defaultValue = "252") int lookbackDays) {

        RiskAnalyticsResponse response = riskAnalyticsService.computeRiskAnalytics(
                portfolioId,
                authentication.getName(),
                confidenceLevel,
                timeHorizonDays,
                lookbackDays
        );

        return ResponseEntity.ok(response);
    }
}
