package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Correlation and hedging analysis response (FR-CH-001 through FR-CH-008).
 */
public class CorrelationAnalysisResponse {

    private Long portfolioId;
    private String portfolioName;
    private int holdingCount;
    private int lookbackDays;

    // FR-CH-001: Correlation matrix (ticker -> ticker -> correlation value)
    private List<String> tickers;
    private List<String> tickerNames;
    private double[][] correlationMatrix;

    // FR-CH-003: Highly correlated pairs (correlation > 0.7)
    private List<CorrelatedPair> highlyCorrelatedPairs;

    // FR-CH-004: Negatively correlated pairs suitable for hedging
    private List<CorrelatedPair> negativelyCorrelatedPairs;

    // FR-CH-005: Hedge suggestions
    private List<HedgeSuggestion> hedgeSuggestions;

    // FR-CH-006: Rolling correlations
    private Map<String, RollingCorrelation> rollingCorrelations;

    // FR-CH-007: Diversification score
    private BigDecimal diversificationScore;
    private String diversificationRating;

    // ── Nested types ──

    public static class CorrelatedPair {
        private String ticker1;
        private String ticker2;
        private String name1;
        private String name2;
        private BigDecimal correlation;
        private String riskLevel;

        public String getTicker1() { return ticker1; }
        public void setTicker1(String ticker1) { this.ticker1 = ticker1; }
        public String getTicker2() { return ticker2; }
        public void setTicker2(String ticker2) { this.ticker2 = ticker2; }
        public String getName1() { return name1; }
        public void setName1(String name1) { this.name1 = name1; }
        public String getName2() { return name2; }
        public void setName2(String name2) { this.name2 = name2; }
        public BigDecimal getCorrelation() { return correlation; }
        public void setCorrelation(BigDecimal correlation) { this.correlation = correlation; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    }

    public static class HedgeSuggestion {
        private String holdingTicker;
        private String holdingName;
        private String hedgeType;
        private String hedgeInstrument;
        private String description;
        private BigDecimal expectedCorrelation;

        public String getHoldingTicker() { return holdingTicker; }
        public void setHoldingTicker(String holdingTicker) { this.holdingTicker = holdingTicker; }
        public String getHoldingName() { return holdingName; }
        public void setHoldingName(String holdingName) { this.holdingName = holdingName; }
        public String getHedgeType() { return hedgeType; }
        public void setHedgeType(String hedgeType) { this.hedgeType = hedgeType; }
        public String getHedgeInstrument() { return hedgeInstrument; }
        public void setHedgeInstrument(String hedgeInstrument) { this.hedgeInstrument = hedgeInstrument; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getExpectedCorrelation() { return expectedCorrelation; }
        public void setExpectedCorrelation(BigDecimal expectedCorrelation) { this.expectedCorrelation = expectedCorrelation; }
    }

    public static class RollingCorrelation {
        private String ticker1;
        private String ticker2;
        private BigDecimal correlation30d;
        private BigDecimal correlation90d;
        private BigDecimal correlation1y;
        private String trend;

        public String getTicker1() { return ticker1; }
        public void setTicker1(String ticker1) { this.ticker1 = ticker1; }
        public String getTicker2() { return ticker2; }
        public void setTicker2(String ticker2) { this.ticker2 = ticker2; }
        public BigDecimal getCorrelation30d() { return correlation30d; }
        public void setCorrelation30d(BigDecimal correlation30d) { this.correlation30d = correlation30d; }
        public BigDecimal getCorrelation90d() { return correlation90d; }
        public void setCorrelation90d(BigDecimal correlation90d) { this.correlation90d = correlation90d; }
        public BigDecimal getCorrelation1y() { return correlation1y; }
        public void setCorrelation1y(BigDecimal correlation1y) { this.correlation1y = correlation1y; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
    }

    // ── Top-level getters/setters ──

    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }
    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }
    public int getHoldingCount() { return holdingCount; }
    public void setHoldingCount(int holdingCount) { this.holdingCount = holdingCount; }
    public int getLookbackDays() { return lookbackDays; }
    public void setLookbackDays(int lookbackDays) { this.lookbackDays = lookbackDays; }
    public List<String> getTickers() { return tickers; }
    public void setTickers(List<String> tickers) { this.tickers = tickers; }
    public List<String> getTickerNames() { return tickerNames; }
    public void setTickerNames(List<String> tickerNames) { this.tickerNames = tickerNames; }
    public double[][] getCorrelationMatrix() { return correlationMatrix; }
    public void setCorrelationMatrix(double[][] correlationMatrix) { this.correlationMatrix = correlationMatrix; }
    public List<CorrelatedPair> getHighlyCorrelatedPairs() { return highlyCorrelatedPairs; }
    public void setHighlyCorrelatedPairs(List<CorrelatedPair> highlyCorrelatedPairs) { this.highlyCorrelatedPairs = highlyCorrelatedPairs; }
    public List<CorrelatedPair> getNegativelyCorrelatedPairs() { return negativelyCorrelatedPairs; }
    public void setNegativelyCorrelatedPairs(List<CorrelatedPair> negativelyCorrelatedPairs) { this.negativelyCorrelatedPairs = negativelyCorrelatedPairs; }
    public List<HedgeSuggestion> getHedgeSuggestions() { return hedgeSuggestions; }
    public void setHedgeSuggestions(List<HedgeSuggestion> hedgeSuggestions) { this.hedgeSuggestions = hedgeSuggestions; }
    public Map<String, RollingCorrelation> getRollingCorrelations() { return rollingCorrelations; }
    public void setRollingCorrelations(Map<String, RollingCorrelation> rollingCorrelations) { this.rollingCorrelations = rollingCorrelations; }
    public BigDecimal getDiversificationScore() { return diversificationScore; }
    public void setDiversificationScore(BigDecimal diversificationScore) { this.diversificationScore = diversificationScore; }
    public String getDiversificationRating() { return diversificationRating; }
    public void setDiversificationRating(String diversificationRating) { this.diversificationRating = diversificationRating; }
}
