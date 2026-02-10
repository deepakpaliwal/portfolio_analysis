package com.portfolio.api.service;

import com.portfolio.api.dto.ValuationResponse;
import com.portfolio.api.exception.ResourceNotFoundException;
import com.portfolio.api.model.Holding;
import com.portfolio.api.model.Portfolio;
import com.portfolio.api.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ValuationService {

    private final PortfolioRepository portfolioRepository;
    private final MarketDataService marketDataService;

    public ValuationService(PortfolioRepository portfolioRepository, MarketDataService marketDataService) {
        this.portfolioRepository = portfolioRepository;
        this.marketDataService = marketDataService;
    }

    @Transactional(readOnly = true)
    public ValuationResponse getValuation(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "id", portfolioId));

        String baseCurrency = portfolio.getBaseCurrency();
        List<ValuationResponse.HoldingValuation> valuations = new ArrayList<>();

        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalMarket = BigDecimal.ZERO;

        for (Holding h : portfolio.getHoldings()) {
            ValuationResponse.HoldingValuation hv = new ValuationResponse.HoldingValuation();
            hv.setId(h.getId());
            hv.setTicker(h.getTicker());
            hv.setName(h.getName());
            hv.setAssetType(h.getAssetType().name());
            hv.setQuantity(h.getQuantity());
            hv.setPurchasePrice(h.getPurchasePrice());
            hv.setHoldingCurrency(h.getCurrency());

            // Fetch current price from Finnhub
            BigDecimal currentPrice = marketDataService.getCurrentPrice(h.getTicker());
            hv.setCurrentPrice(currentPrice);

            // FX conversion: holding currency -> portfolio base currency
            BigDecimal fxRate = BigDecimal.ONE;
            if (!h.getCurrency().equalsIgnoreCase(baseCurrency)) {
                BigDecimal rate = marketDataService.getExchangeRate(h.getCurrency(), baseCurrency);
                if (rate != null) {
                    fxRate = rate;
                }
            }
            hv.setFxRate(fxRate);

            // Cost basis in base currency
            BigDecimal costBasis = h.getQuantity()
                    .multiply(h.getPurchasePrice())
                    .multiply(fxRate)
                    .setScale(2, RoundingMode.HALF_UP);
            hv.setCostBasis(costBasis);
            totalCost = totalCost.add(costBasis);

            // Market value in base currency
            if (currentPrice != null) {
                BigDecimal marketValue = h.getQuantity()
                        .multiply(currentPrice)
                        .multiply(fxRate)
                        .setScale(2, RoundingMode.HALF_UP);
                hv.setMarketValue(marketValue);
                totalMarket = totalMarket.add(marketValue);

                BigDecimal gainLoss = marketValue.subtract(costBasis);
                hv.setGainLoss(gainLoss);
                hv.setGainLossPercent(
                        costBasis.compareTo(BigDecimal.ZERO) > 0
                                ? gainLoss.multiply(BigDecimal.valueOf(100)).divide(costBasis, 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO
                );
            } else {
                // No market data — use cost basis as fallback
                hv.setMarketValue(costBasis);
                totalMarket = totalMarket.add(costBasis);
                hv.setGainLoss(BigDecimal.ZERO);
                hv.setGainLossPercent(BigDecimal.ZERO);
            }

            valuations.add(hv);
        }

        ValuationResponse response = new ValuationResponse();
        response.setPortfolioId(portfolioId);
        response.setBaseCurrency(baseCurrency);
        response.setTotalCostBasis(totalCost);
        response.setTotalMarketValue(totalMarket);
        response.setTotalGainLoss(totalMarket.subtract(totalCost));
        response.setTotalGainLossPercent(
                totalCost.compareTo(BigDecimal.ZERO) > 0
                        ? totalMarket.subtract(totalCost).multiply(BigDecimal.valueOf(100)).divide(totalCost, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO
        );
        response.setHoldings(valuations);

        return response;
    }
}
