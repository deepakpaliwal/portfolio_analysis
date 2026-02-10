package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sector screener report (FR-SC-006, FR-SC-007).
 */
public class SectorReportResponse {

    private String sector;
    private BigDecimal sectorPerformancePercent;
    private BigDecimal spPerformancePercent;
    private BigDecimal relativePerformancePercent;
    private BigDecimal averagePE;
    private BigDecimal averageDividendYield;
    private int stockCount;
    private List<StockPerformance> topPerformers;
    private List<StockPerformance> bottomPerformers;
    private List<SectorRotationSignal> rotationSignals;

    public static class StockPerformance {
        private String ticker;
        private String name;
        private BigDecimal currentPrice;
        private BigDecimal changePercent;
        private BigDecimal marketCap;
        private BigDecimal peRatio;

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getChangePercent() { return changePercent; }
        public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }
        public BigDecimal getMarketCap() { return marketCap; }
        public void setMarketCap(BigDecimal marketCap) { this.marketCap = marketCap; }
        public BigDecimal getPeRatio() { return peRatio; }
        public void setPeRatio(BigDecimal peRatio) { this.peRatio = peRatio; }
    }

    public static class SectorRotationSignal {
        private String signal;
        private String description;

        public String getSignal() { return signal; }
        public void setSignal(String signal) { this.signal = signal; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public BigDecimal getSectorPerformancePercent() { return sectorPerformancePercent; }
    public void setSectorPerformancePercent(BigDecimal sectorPerformancePercent) { this.sectorPerformancePercent = sectorPerformancePercent; }
    public BigDecimal getSpPerformancePercent() { return spPerformancePercent; }
    public void setSpPerformancePercent(BigDecimal spPerformancePercent) { this.spPerformancePercent = spPerformancePercent; }
    public BigDecimal getRelativePerformancePercent() { return relativePerformancePercent; }
    public void setRelativePerformancePercent(BigDecimal relativePerformancePercent) { this.relativePerformancePercent = relativePerformancePercent; }
    public BigDecimal getAveragePE() { return averagePE; }
    public void setAveragePE(BigDecimal averagePE) { this.averagePE = averagePE; }
    public BigDecimal getAverageDividendYield() { return averageDividendYield; }
    public void setAverageDividendYield(BigDecimal averageDividendYield) { this.averageDividendYield = averageDividendYield; }
    public int getStockCount() { return stockCount; }
    public void setStockCount(int stockCount) { this.stockCount = stockCount; }
    public List<StockPerformance> getTopPerformers() { return topPerformers; }
    public void setTopPerformers(List<StockPerformance> topPerformers) { this.topPerformers = topPerformers; }
    public List<StockPerformance> getBottomPerformers() { return bottomPerformers; }
    public void setBottomPerformers(List<StockPerformance> bottomPerformers) { this.bottomPerformers = bottomPerformers; }
    public List<SectorRotationSignal> getRotationSignals() { return rotationSignals; }
    public void setRotationSignals(List<SectorRotationSignal> rotationSignals) { this.rotationSignals = rotationSignals; }
}
