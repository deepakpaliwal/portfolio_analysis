package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comprehensive risk analytics response for a portfolio (FR-RA-001 through FR-RA-011).
 */
public class RiskAnalyticsResponse {

    private Long portfolioId;
    private String portfolioName;
    private BigDecimal portfolioValue;
    private String baseCurrency;

    // VaR (FR-RA-001, FR-RA-002)
    private VaRMetrics var;

    // CVaR / Expected Shortfall (FR-RA-003)
    private BigDecimal cvar95;
    private BigDecimal cvar99;

    // Volatility (FR-RA-004)
    private BigDecimal annualizedVolatility;
    private BigDecimal dailyVolatility;

    // Beta (FR-RA-005)
    private BigDecimal portfolioBeta;
    private List<HoldingBeta> holdingBetas;

    // Alpha (FR-RA-006)
    private BigDecimal portfolioAlpha;

    // Ratios (FR-RA-007)
    private BigDecimal sharpeRatio;
    private BigDecimal sortinoRatio;
    private BigDecimal treynorRatio;

    // Max Drawdown (FR-RA-008)
    private BigDecimal maxDrawdown;
    private String maxDrawdownPeakDate;
    private String maxDrawdownTroughDate;

    // Stress Testing (FR-RA-009)
    private List<StressScenario> stressTests;

    // Monte Carlo (part of VaR FR-RA-001)
    private MonteCarloResult monteCarlo;

    // Configuration used
    private double confidenceLevel;
    private int timeHorizonDays;
    private int lookbackDays;

    // ── Nested types ──

    public static class VaRMetrics {
        private BigDecimal historicalSimulation;
        private BigDecimal parametric;
        private BigDecimal monteCarlo;

        public BigDecimal getHistoricalSimulation() { return historicalSimulation; }
        public void setHistoricalSimulation(BigDecimal historicalSimulation) { this.historicalSimulation = historicalSimulation; }
        public BigDecimal getParametric() { return parametric; }
        public void setParametric(BigDecimal parametric) { this.parametric = parametric; }
        public BigDecimal getMonteCarlo() { return monteCarlo; }
        public void setMonteCarlo(BigDecimal monteCarlo) { this.monteCarlo = monteCarlo; }
    }

    public static class HoldingBeta {
        private String ticker;
        private String name;
        private BigDecimal beta;
        private BigDecimal weight;

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getBeta() { return beta; }
        public void setBeta(BigDecimal beta) { this.beta = beta; }
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
    }

    public static class StressScenario {
        private String name;
        private String description;
        private BigDecimal marketShockPercent;
        private BigDecimal estimatedLoss;
        private BigDecimal estimatedLossPercent;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getMarketShockPercent() { return marketShockPercent; }
        public void setMarketShockPercent(BigDecimal marketShockPercent) { this.marketShockPercent = marketShockPercent; }
        public BigDecimal getEstimatedLoss() { return estimatedLoss; }
        public void setEstimatedLoss(BigDecimal estimatedLoss) { this.estimatedLoss = estimatedLoss; }
        public BigDecimal getEstimatedLossPercent() { return estimatedLossPercent; }
        public void setEstimatedLossPercent(BigDecimal estimatedLossPercent) { this.estimatedLossPercent = estimatedLossPercent; }
    }

    public static class MonteCarloResult {
        private int simulations;
        private BigDecimal meanReturn;
        private BigDecimal percentile5;
        private BigDecimal percentile25;
        private BigDecimal median;
        private BigDecimal percentile75;
        private BigDecimal percentile95;

        public int getSimulations() { return simulations; }
        public void setSimulations(int simulations) { this.simulations = simulations; }
        public BigDecimal getMeanReturn() { return meanReturn; }
        public void setMeanReturn(BigDecimal meanReturn) { this.meanReturn = meanReturn; }
        public BigDecimal getPercentile5() { return percentile5; }
        public void setPercentile5(BigDecimal percentile5) { this.percentile5 = percentile5; }
        public BigDecimal getPercentile25() { return percentile25; }
        public void setPercentile25(BigDecimal percentile25) { this.percentile25 = percentile25; }
        public BigDecimal getMedian() { return median; }
        public void setMedian(BigDecimal median) { this.median = median; }
        public BigDecimal getPercentile75() { return percentile75; }
        public void setPercentile75(BigDecimal percentile75) { this.percentile75 = percentile75; }
        public BigDecimal getPercentile95() { return percentile95; }
        public void setPercentile95(BigDecimal percentile95) { this.percentile95 = percentile95; }
    }

    // ── Top-level getters/setters ──

    public Long getPortfolioId() { return portfolioId; }
    public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }
    public String getPortfolioName() { return portfolioName; }
    public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }
    public BigDecimal getPortfolioValue() { return portfolioValue; }
    public void setPortfolioValue(BigDecimal portfolioValue) { this.portfolioValue = portfolioValue; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
    public VaRMetrics getVar() { return var; }
    public void setVar(VaRMetrics var) { this.var = var; }
    public BigDecimal getCvar95() { return cvar95; }
    public void setCvar95(BigDecimal cvar95) { this.cvar95 = cvar95; }
    public BigDecimal getCvar99() { return cvar99; }
    public void setCvar99(BigDecimal cvar99) { this.cvar99 = cvar99; }
    public BigDecimal getAnnualizedVolatility() { return annualizedVolatility; }
    public void setAnnualizedVolatility(BigDecimal annualizedVolatility) { this.annualizedVolatility = annualizedVolatility; }
    public BigDecimal getDailyVolatility() { return dailyVolatility; }
    public void setDailyVolatility(BigDecimal dailyVolatility) { this.dailyVolatility = dailyVolatility; }
    public BigDecimal getPortfolioBeta() { return portfolioBeta; }
    public void setPortfolioBeta(BigDecimal portfolioBeta) { this.portfolioBeta = portfolioBeta; }
    public List<HoldingBeta> getHoldingBetas() { return holdingBetas; }
    public void setHoldingBetas(List<HoldingBeta> holdingBetas) { this.holdingBetas = holdingBetas; }
    public BigDecimal getPortfolioAlpha() { return portfolioAlpha; }
    public void setPortfolioAlpha(BigDecimal portfolioAlpha) { this.portfolioAlpha = portfolioAlpha; }
    public BigDecimal getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
    public BigDecimal getSortinoRatio() { return sortinoRatio; }
    public void setSortinoRatio(BigDecimal sortinoRatio) { this.sortinoRatio = sortinoRatio; }
    public BigDecimal getTreynorRatio() { return treynorRatio; }
    public void setTreynorRatio(BigDecimal treynorRatio) { this.treynorRatio = treynorRatio; }
    public BigDecimal getMaxDrawdown() { return maxDrawdown; }
    public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
    public String getMaxDrawdownPeakDate() { return maxDrawdownPeakDate; }
    public void setMaxDrawdownPeakDate(String maxDrawdownPeakDate) { this.maxDrawdownPeakDate = maxDrawdownPeakDate; }
    public String getMaxDrawdownTroughDate() { return maxDrawdownTroughDate; }
    public void setMaxDrawdownTroughDate(String maxDrawdownTroughDate) { this.maxDrawdownTroughDate = maxDrawdownTroughDate; }
    public List<StressScenario> getStressTests() { return stressTests; }
    public void setStressTests(List<StressScenario> stressTests) { this.stressTests = stressTests; }
    public MonteCarloResult getMonteCarlo() { return monteCarlo; }
    public void setMonteCarlo(MonteCarloResult monteCarlo) { this.monteCarlo = monteCarlo; }
    public double getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(double confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    public int getTimeHorizonDays() { return timeHorizonDays; }
    public void setTimeHorizonDays(int timeHorizonDays) { this.timeHorizonDays = timeHorizonDays; }
    public int getLookbackDays() { return lookbackDays; }
    public void setLookbackDays(int lookbackDays) { this.lookbackDays = lookbackDays; }
}
