package com.portfolio.api.controller;

import com.portfolio.api.dto.AllocationResponse;
import com.portfolio.api.dto.HoldingResponse;
import com.portfolio.api.dto.PortfolioRequest;
import com.portfolio.api.dto.PortfolioResponse;
import com.portfolio.api.dto.ValuationResponse;
import com.portfolio.api.model.User;
import com.portfolio.api.repository.UserRepository;
import com.portfolio.api.service.PortfolioService;
import com.portfolio.api.service.ValuationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    private final ValuationService valuationService;
    private final UserRepository userRepository;

    public PortfolioController(PortfolioService portfolioService, ValuationService valuationService, UserRepository userRepository) {
        this.portfolioService = portfolioService;
        this.valuationService = valuationService;
        this.userRepository = userRepository;
    }

    private Long getUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @PostMapping
    @Operation(summary = "Create a new portfolio")
    public ResponseEntity<PortfolioResponse> createPortfolio(
            Authentication authentication,
            @Valid @RequestBody PortfolioRequest request) {
        PortfolioResponse response = portfolioService.createPortfolio(getUserId(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all portfolios for a user")
    public ResponseEntity<List<PortfolioResponse>> getPortfolios(Authentication authentication) {
        return ResponseEntity.ok(portfolioService.getPortfoliosByUser(getUserId(authentication)));
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

    @GetMapping("/{id}/allocation")
    @Operation(summary = "Get portfolio allocation breakdown by asset type, sector, and currency")
    public ResponseEntity<AllocationResponse> getAllocation(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.getAllocation(id));
    }

    @GetMapping("/{id}/valuation")
    @Operation(summary = "Get real-time portfolio valuation with market prices, FX conversion, and gain/loss")
    public ResponseEntity<ValuationResponse> getValuation(@PathVariable Long id) {
        return ResponseEntity.ok(valuationService.getValuation(id));
    }

    @PostMapping(value = "/{id}/holdings/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import holdings from a CSV file")
    public ResponseEntity<?> importHoldings(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CSV file is empty"));
        }
        List<HoldingResponse> imported = portfolioService.importHoldingsFromCsv(id, file.getInputStream());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("imported", imported.size(), "holdings", imported));
    }
}
