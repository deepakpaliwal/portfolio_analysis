package com.portfolio.api.controller;

import com.portfolio.api.dto.HoldingRequest;
import com.portfolio.api.dto.HoldingResponse;
import com.portfolio.api.service.HoldingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios/{portfolioId}/holdings")
@Tag(name = "Holdings", description = "Holdings management within a portfolio")
public class HoldingController {

    private final HoldingService holdingService;

    public HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @PostMapping
    @Operation(summary = "Add a holding to a portfolio")
    public ResponseEntity<HoldingResponse> addHolding(
            @PathVariable Long portfolioId,
            @Valid @RequestBody HoldingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(holdingService.addHolding(portfolioId, request));
    }

    @GetMapping
    @Operation(summary = "Get all holdings in a portfolio")
    public ResponseEntity<List<HoldingResponse>> getHoldings(@PathVariable Long portfolioId) {
        return ResponseEntity.ok(holdingService.getHoldingsByPortfolio(portfolioId));
    }

    @GetMapping("/{holdingId}")
    @Operation(summary = "Get a specific holding")
    public ResponseEntity<HoldingResponse> getHolding(@PathVariable Long holdingId) {
        return ResponseEntity.ok(holdingService.getHolding(holdingId));
    }

    @PutMapping("/{holdingId}")
    @Operation(summary = "Update a holding")
    public ResponseEntity<HoldingResponse> updateHolding(
            @PathVariable Long holdingId,
            @Valid @RequestBody HoldingRequest request) {
        return ResponseEntity.ok(holdingService.updateHolding(holdingId, request));
    }

    @DeleteMapping("/{holdingId}")
    @Operation(summary = "Remove a holding from a portfolio")
    public ResponseEntity<Void> deleteHolding(@PathVariable Long holdingId) {
        holdingService.deleteHolding(holdingId);
        return ResponseEntity.noContent().build();
    }
}
