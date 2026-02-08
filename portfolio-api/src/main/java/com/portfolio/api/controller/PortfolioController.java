package com.portfolio.api.controller;

import com.portfolio.api.dto.PortfolioRequest;
import com.portfolio.api.dto.PortfolioResponse;
import com.portfolio.api.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for portfolio CRUD operations.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/portfolios")
@Tag(name = "Portfolio", description = "Portfolio management endpoints")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    @Operation(summary = "Create a new portfolio")
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PortfolioRequest request) {
        PortfolioResponse response = portfolioService.createPortfolio(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all portfolios for a user")
    public ResponseEntity<List<PortfolioResponse>> getPortfolios(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(portfolioService.getPortfoliosByUser(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific portfolio by ID")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.getPortfolio(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a portfolio")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioRequest request) {
        return ResponseEntity.ok(portfolioService.updatePortfolio(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a portfolio")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }
}
