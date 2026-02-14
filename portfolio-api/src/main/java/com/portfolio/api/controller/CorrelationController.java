package com.portfolio.api.controller;

import com.portfolio.api.dto.CorrelationAnalysisResponse;
import com.portfolio.api.service.CorrelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/correlation")
@Tag(name = "Correlation & Hedging", description = "Correlation matrix, hedging analysis, and diversification scoring")
public class CorrelationController {

    private final CorrelationService correlationService;

    public CorrelationController(CorrelationService correlationService) {
        this.correlationService = correlationService;
    }

    @GetMapping("/portfolio/{portfolioId}")
    @Operation(summary = "Compute correlation analysis for a portfolio's holdings")
    public ResponseEntity<CorrelationAnalysisResponse> analyzeCorrelation(
            Authentication authentication,
            @PathVariable Long portfolioId,
            @RequestParam(defaultValue = "252") int lookbackDays) {

        CorrelationAnalysisResponse response = correlationService.analyzeCorrelation(
                portfolioId, authentication.getName(), lookbackDays);

        return ResponseEntity.ok(response);
    }
}
