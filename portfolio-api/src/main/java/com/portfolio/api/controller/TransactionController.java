package com.portfolio.api.controller;

import com.portfolio.api.dto.TransactionRequest;
import com.portfolio.api.dto.TransactionResponse;
import com.portfolio.api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Transactions", description = "Transaction history endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/api/v1/holdings/{holdingId}/transactions")
    @Operation(summary = "Record a transaction on a holding")
    public ResponseEntity<TransactionResponse> addTransaction(
            @PathVariable Long holdingId,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.addTransaction(holdingId, request));
    }

    @GetMapping("/api/v1/holdings/{holdingId}/transactions")
    @Operation(summary = "Get transactions for a specific holding")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByHolding(
            @PathVariable Long holdingId) {
        return ResponseEntity.ok(transactionService.getTransactionsByHolding(holdingId));
    }

    @GetMapping("/api/v1/portfolios/{portfolioId}/transactions")
    @Operation(summary = "Get all transactions for a portfolio")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByPortfolio(
            @PathVariable Long portfolioId) {
        return ResponseEntity.ok(transactionService.getTransactionsByPortfolio(portfolioId));
    }
}
