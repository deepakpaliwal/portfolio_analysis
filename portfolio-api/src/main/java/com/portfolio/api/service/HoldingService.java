package com.portfolio.api.service;

import com.portfolio.api.dto.HoldingRequest;
import com.portfolio.api.dto.HoldingResponse;
import com.portfolio.api.exception.ResourceNotFoundException;
import com.portfolio.api.model.Holding;
import com.portfolio.api.model.Portfolio;
import com.portfolio.api.repository.HoldingRepository;
import com.portfolio.api.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final PortfolioRepository portfolioRepository;

    public HoldingService(HoldingRepository holdingRepository, PortfolioRepository portfolioRepository) {
        this.holdingRepository = holdingRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public HoldingResponse addHolding(Long portfolioId, HoldingRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));

        Holding holding = new Holding();
        holding.setPortfolio(portfolio);
        mapRequestToHolding(request, holding);

        return toResponse(holdingRepository.save(holding));
    }

    @Transactional(readOnly = true)
    public List<HoldingResponse> getHoldingsByPortfolio(Long portfolioId) {
        if (!portfolioRepository.existsById(portfolioId)) {
            throw new ResourceNotFoundException("Portfolio", "id", portfolioId);
        }
        return holdingRepository.findByPortfolioId(portfolioId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HoldingResponse getHolding(Long holdingId) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding", "id", holdingId));
        return toResponse(holding);
    }

    public HoldingResponse updateHolding(Long holdingId, HoldingRequest request) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException("Holding", "id", holdingId));
        mapRequestToHolding(request, holding);
        return toResponse(holdingRepository.save(holding));
    }

    public void deleteHolding(Long holdingId) {
        if (!holdingRepository.existsById(holdingId)) {
            throw new ResourceNotFoundException("Holding", "id", holdingId);
        }
        holdingRepository.deleteById(holdingId);
    }

    private void mapRequestToHolding(HoldingRequest request, Holding holding) {
        holding.setAssetType(request.getAssetType());
        holding.setTicker(request.getTicker());
        holding.setName(request.getName());
        holding.setQuantity(request.getQuantity());
        holding.setPurchasePrice(request.getPurchasePrice());
        holding.setPurchaseDate(request.getPurchaseDate());
        holding.setCurrency(request.getCurrency());
        holding.setSector(request.getSector());
        holding.setCategory(request.getCategory());
    }

    private HoldingResponse toResponse(Holding h) {
        HoldingResponse r = new HoldingResponse();
        r.setId(h.getId());
        r.setAssetType(h.getAssetType());
        r.setTicker(h.getTicker());
        r.setName(h.getName());
        r.setQuantity(h.getQuantity());
        r.setPurchasePrice(h.getPurchasePrice());
        r.setPurchaseDate(h.getPurchaseDate());
        r.setCurrency(h.getCurrency());
        r.setSector(h.getSector());
        r.setCategory(h.getCategory());
        r.setCreatedAt(h.getCreatedAt());
        r.setUpdatedAt(h.getUpdatedAt());
        return r;
    }
}
