package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Result of a custom screener run (FR-SC-008).
 */
public class ScreenResultResponse {

    private int totalMatches;
    private List<ScreenedStock> stocks;

    public static class ScreenedStock {
        private String ticker;
        private String name;
        private String sector;
        private String industry;
        private BigDecimal currentPrice;
        private BigDecimal changePercent;
        private BigDecimal marketCap;
        private BigDecimal peRatio;
        private BigDecimal eps;
        private BigDecimal dividendYield;
        private BigDecimal beta;
        private BigDecimal weekHigh52;
        private BigDecimal weekLow52;

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSector() { return sector; }
        public void setSector(String sector) { this.sector = sector; }
        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getChangePercent() { return changePercent; }
        public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }
        public BigDecimal getMarketCap() { return marketCap; }
        public void setMarketCap(BigDecimal marketCap) { this.marketCap = marketCap; }
        public BigDecimal getPeRatio() { return peRatio; }
        public void setPeRatio(BigDecimal peRatio) { this.peRatio = peRatio; }
        public BigDecimal getEps() { return eps; }
        public void setEps(BigDecimal eps) { this.eps = eps; }
        public BigDecimal getDividendYield() { return dividendYield; }
        public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }
        public BigDecimal getBeta() { return beta; }
        public void setBeta(BigDecimal beta) { this.beta = beta; }
        public BigDecimal getWeekHigh52() { return weekHigh52; }
        public void setWeekHigh52(BigDecimal weekHigh52) { this.weekHigh52 = weekHigh52; }
        public BigDecimal getWeekLow52() { return weekLow52; }
        public void setWeekLow52(BigDecimal weekLow52) { this.weekLow52 = weekLow52; }
    }

    public int getTotalMatches() { return totalMatches; }
    public void setTotalMatches(int totalMatches) { this.totalMatches = totalMatches; }
    public List<ScreenedStock> getStocks() { return stocks; }
    public void setStocks(List<ScreenedStock> stocks) { this.stocks = stocks; }
}
