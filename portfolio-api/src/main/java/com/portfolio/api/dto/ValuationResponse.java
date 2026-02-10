package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class ValuationResponse {

    private Long portfolioId;
    private String baseCurrency;
    private BigDecimal totalCostBasis;
    private BigDecimal totalMarketValue;
    private BigDecimal totalGainLoss;
    private BigDecimal totalGainLossPercent;
    private List<HoldingValuation> holdings;

    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public BigDecimal getTotalCostBasis() { return totalCostBasis; }
    public void setTotalCostBasis(BigDecimal totalCostBasis) { this.totalCostBasis = totalCostBasis; }

    public BigDecimal getTotalMarketValue() { return totalMarketValue; }
    public void setTotalMarketValue(BigDecimal totalMarketValue) { this.totalMarketValue = totalMarketValue; }

    public BigDecimal getTotalGainLoss() { return totalGainLoss; }
    public void setTotalGainLoss(BigDecimal totalGainLoss) { this.totalGainLoss = totalGainLoss; }

    public BigDecimal getTotalGainLossPercent() { return totalGainLossPercent; }
    public void setTotalGainLossPercent(BigDecimal totalGainLossPercent) { this.totalGainLossPercent = totalGainLossPercent; }

    public List<HoldingValuation> getHoldings() { return holdings; }
    public void setHoldings(List<HoldingValuation> holdings) { this.holdings = holdings; }

    public static class HoldingValuation {
        private Long id;
        private String ticker;
        private String name;
        private String assetType;
        private BigDecimal quantity;
        private BigDecimal purchasePrice;
        private String holdingCurrency;
        private BigDecimal currentPrice;
        private BigDecimal fxRate;
        private BigDecimal costBasis;
        private BigDecimal marketValue;
        private BigDecimal gainLoss;
        private BigDecimal gainLossPercent;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAssetType() { return assetType; }
        public void setAssetType(String assetType) { this.assetType = assetType; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public BigDecimal getPurchasePrice() { return purchasePrice; }
        public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

        public String getHoldingCurrency() { return holdingCurrency; }
        public void setHoldingCurrency(String holdingCurrency) { this.holdingCurrency = holdingCurrency; }

        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

        public BigDecimal getFxRate() { return fxRate; }
        public void setFxRate(BigDecimal fxRate) { this.fxRate = fxRate; }

        public BigDecimal getCostBasis() { return costBasis; }
        public void setCostBasis(BigDecimal costBasis) { this.costBasis = costBasis; }

        public BigDecimal getMarketValue() { return marketValue; }
        public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }

        public BigDecimal getGainLoss() { return gainLoss; }
        public void setGainLoss(BigDecimal gainLoss) { this.gainLoss = gainLoss; }

        public BigDecimal getGainLossPercent() { return gainLossPercent; }
        public void setGainLossPercent(BigDecimal gainLossPercent) { this.gainLossPercent = gainLossPercent; }
    }
}
