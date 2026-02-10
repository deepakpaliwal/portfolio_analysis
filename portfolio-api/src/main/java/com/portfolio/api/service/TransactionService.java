package com.portfolio.api.service;

import com.portfolio.api.dto.TransactionRequest;
import com.portfolio.api.dto.TransactionResponse;
import com.portfolio.api.exception.ResourceNotFoundException;
import com.portfolio.api.model.Holding;
import com.portfolio.api.model.Transaction;
import com.portfolio.api.repository.HoldingRepository;
import com.portfolio.api.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final HoldingRepository holdingRepository;

    public TransactionService(TransactionRepository transactionRepository, HoldingRepository holdingRepository) {
        this.transactionRepository = transactionRepository;
        this.holdingRepository = holdingRepository;
    }

    public TransactionResponse addTransaction(Long holdingId, TransactionRequest request) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding", "id", holdingId));

        Transaction tx = new Transaction();
        tx.setHolding(holding);
        tx.setTransactionType(request.getTransactionType());
        tx.setQuantity(request.getQuantity());
        tx.setPrice(request.getPrice());
        tx.setFees(request.getFees());
        tx.setExecutedAt(request.getExecutedAt());
        tx.setNotes(request.getNotes());

        return toResponse(transactionRepository.save(tx));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByHolding(Long holdingId) {
        return transactionRepository.findByHoldingId(holdingId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByPortfolio(Long portfolioId) {
        return transactionRepository.findByPortfolioId(portfolioId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse toResponse(Transaction tx) {
        TransactionResponse r = new TransactionResponse();
        r.setId(tx.getId());
        r.setHoldingId(tx.getHolding().getId());
        r.setHoldingTicker(tx.getHolding().getTicker());
        r.setTransactionType(tx.getTransactionType());
        r.setQuantity(tx.getQuantity());
        r.setPrice(tx.getPrice());
        r.setFees(tx.getFees());
        r.setExecutedAt(tx.getExecutedAt());
        r.setNotes(tx.getNotes());
        r.setCreatedAt(tx.getCreatedAt());
        return r;
    }
}
