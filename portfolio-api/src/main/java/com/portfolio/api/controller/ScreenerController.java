package com.portfolio.api.controller;

import com.portfolio.api.dto.*;
import com.portfolio.api.model.ScreenerReport;
import com.portfolio.api.service.ScreenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/screener")
@Tag(name = "Screener", description = "Stock & Sector Screener endpoints")
public class ScreenerController {

    private final ScreenerService screenerService;

    public ScreenerController(ScreenerService screenerService) {
        this.screenerService = screenerService;
    }

    // ── Ticker screener (FR-SC-001 through FR-SC-005) ──

    @GetMapping("/ticker/{symbol}")
    @Operation(summary = "Generate a comprehensive ticker report")
    public ResponseEntity<TickerReportResponse> getTickerReport(@PathVariable String symbol) {
        return ResponseEntity.ok(screenerService.generateTickerReport(symbol));
    }

    // ── Sector screener (FR-SC-006, FR-SC-007) ──

    @GetMapping("/sectors")
    @Operation(summary = "List available sectors")
    public ResponseEntity<List<String>> getAvailableSectors() {
        return ResponseEntity.ok(screenerService.getAvailableSectors());
    }

    @GetMapping("/sector/{sector}")
    @Operation(summary = "Generate a sector performance report")
    public ResponseEntity<SectorReportResponse> getSectorReport(@PathVariable String sector) {
        return ResponseEntity.ok(screenerService.generateSectorReport(sector));
    }

    // ── Custom screen (FR-SC-008) ──

    @PostMapping("/screen")
    @Operation(summary = "Run a custom stock screen with filters")
    public ResponseEntity<ScreenResultResponse> runCustomScreen(@RequestBody ScreenCriteriaRequest criteria) {
        return ResponseEntity.ok(screenerService.runCustomScreen(criteria));
    }

    // ── Technical indicators (FR-SC-009) ──

    @GetMapping("/indicators/{symbol}")
    @Operation(summary = "Get technical indicator data for a ticker")
    public ResponseEntity<TechnicalIndicatorResponse> getTechnicalIndicator(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "sma") String indicator,
            @RequestParam(defaultValue = "D") String resolution,
            @RequestParam(defaultValue = "20") int timeperiod) {
        return ResponseEntity.ok(screenerService.getTechnicalIndicator(symbol, indicator, resolution, timeperiod));
    }

    // ── Saved reports (FR-SC-010) ──

    @GetMapping("/reports")
    @Operation(summary = "List saved screener reports")
    public ResponseEntity<List<Map<String, Object>>> getSavedReports(Authentication authentication) {
        return ResponseEntity.ok(screenerService.getSavedReports(authentication.getName()));
    }

    @PostMapping("/reports")
    @Operation(summary = "Save a screener report")
    public ResponseEntity<Map<String, Object>> saveReport(Authentication authentication,
                                                           @RequestBody Map<String, Object> body) {
        String reportType = (String) body.get("reportType");
        String target = (String) body.get("target");
        Object reportData = body.get("reportData");

        ScreenerReport saved = screenerService.saveReport(authentication.getName(), reportType, target, reportData);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "reportType", saved.getReportType(),
                "target", saved.getTarget(),
                "createdAt", saved.getCreatedAt().toString()
        ));
    }

    @GetMapping("/reports/{id}")
    @Operation(summary = "Get a saved report's data")
    public ResponseEntity<String> getSavedReportData(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(screenerService.getSavedReportData(authentication.getName(), id));
    }

    @DeleteMapping("/reports/{id}")
    @Operation(summary = "Delete a saved report")
    public ResponseEntity<Void> deleteReport(Authentication authentication, @PathVariable Long id) {
        screenerService.deleteReport(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
