package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy engine response DTOs (FR-SE-001 through FR-SE-007).
 */
public class StrategyResponse {

    // ── Strategy Definition ──

    public static class StrategyDefinition {
        private String id;
        private String name;
        private String category;
        private String description;
        private String riskLevel;
        private List<String> suitableFor;
        private List<StrategyParameter> parameters;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public List<String> getSuitableFor() { return suitableFor; }
        public void setSuitableFor(List<String> suitableFor) { this.suitableFor = suitableFor; }
        public List<StrategyParameter> getParameters() { return parameters; }
        public void setParameters(List<StrategyParameter> parameters) { this.parameters = parameters; }
    }

    public static class StrategyParameter {
        private String name;
        private String label;
        private String type;
        private String defaultValue;
        private String description;

        public StrategyParameter() {}
        public StrategyParameter(String name, String label, String type, String defaultValue, String description) {
            this.name = name; this.label = label; this.type = type;
            this.defaultValue = defaultValue; this.description = description;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // ── Backtest Results (FR-SE-003) ──

    public static class BacktestResult {
        private String strategyId;
        private String strategyName;
        private String ticker;
        private int lookbackDays;
        private BigDecimal cagr;
        private BigDecimal totalReturn;
        private BigDecimal maxDrawdown;
        private BigDecimal sharpeRatio;
        private BigDecimal sortinoRatio;
        private BigDecimal winRate;
        private int totalTrades;
        private int winningTrades;
        private int losingTrades;
        private BigDecimal avgWin;
        private BigDecimal avgLoss;
        private BigDecimal profitFactor;
        private BigDecimal benchmarkReturn;
        private BigDecimal alpha;
        private List<TradeRecord> trades;

        public String getStrategyId() { return strategyId; }
        public void setStrategyId(String strategyId) { this.strategyId = strategyId; }
        public String getStrategyName() { return strategyName; }
        public void setStrategyName(String strategyName) { this.strategyName = strategyName; }
        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public int getLookbackDays() { return lookbackDays; }
        public void setLookbackDays(int lookbackDays) { this.lookbackDays = lookbackDays; }
        public BigDecimal getCagr() { return cagr; }
        public void setCagr(BigDecimal cagr) { this.cagr = cagr; }
        public BigDecimal getTotalReturn() { return totalReturn; }
        public void setTotalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; }
        public BigDecimal getMaxDrawdown() { return maxDrawdown; }
        public void setMaxDrawdown(BigDecimal maxDrawdown) { this.maxDrawdown = maxDrawdown; }
        public BigDecimal getSharpeRatio() { return sharpeRatio; }
        public void setSharpeRatio(BigDecimal sharpeRatio) { this.sharpeRatio = sharpeRatio; }
        public BigDecimal getSortinoRatio() { return sortinoRatio; }
        public void setSortinoRatio(BigDecimal sortinoRatio) { this.sortinoRatio = sortinoRatio; }
        public BigDecimal getWinRate() { return winRate; }
        public void setWinRate(BigDecimal winRate) { this.winRate = winRate; }
        public int getTotalTrades() { return totalTrades; }
        public void setTotalTrades(int totalTrades) { this.totalTrades = totalTrades; }
        public int getWinningTrades() { return winningTrades; }
        public void setWinningTrades(int winningTrades) { this.winningTrades = winningTrades; }
        public int getLosingTrades() { return losingTrades; }
        public void setLosingTrades(int losingTrades) { this.losingTrades = losingTrades; }
        public BigDecimal getAvgWin() { return avgWin; }
        public void setAvgWin(BigDecimal avgWin) { this.avgWin = avgWin; }
        public BigDecimal getAvgLoss() { return avgLoss; }
        public void setAvgLoss(BigDecimal avgLoss) { this.avgLoss = avgLoss; }
        public BigDecimal getProfitFactor() { return profitFactor; }
        public void setProfitFactor(BigDecimal profitFactor) { this.profitFactor = profitFactor; }
        public BigDecimal getBenchmarkReturn() { return benchmarkReturn; }
        public void setBenchmarkReturn(BigDecimal benchmarkReturn) { this.benchmarkReturn = benchmarkReturn; }
        public BigDecimal getAlpha() { return alpha; }
        public void setAlpha(BigDecimal alpha) { this.alpha = alpha; }
        public List<TradeRecord> getTrades() { return trades; }
        public void setTrades(List<TradeRecord> trades) { this.trades = trades; }
    }

    public static class TradeRecord {
        private String entryDate;
        private String exitDate;
        private String signal;
        private BigDecimal entryPrice;
        private BigDecimal exitPrice;
        private BigDecimal returnPct;

        public String getEntryDate() { return entryDate; }
        public void setEntryDate(String entryDate) { this.entryDate = entryDate; }
        public String getExitDate() { return exitDate; }
        public void setExitDate(String exitDate) { this.exitDate = exitDate; }
        public String getSignal() { return signal; }
        public void setSignal(String signal) { this.signal = signal; }
        public BigDecimal getEntryPrice() { return entryPrice; }
        public void setEntryPrice(BigDecimal entryPrice) { this.entryPrice = entryPrice; }
        public BigDecimal getExitPrice() { return exitPrice; }
        public void setExitPrice(BigDecimal exitPrice) { this.exitPrice = exitPrice; }
        public BigDecimal getReturnPct() { return returnPct; }
        public void setReturnPct(BigDecimal returnPct) { this.returnPct = returnPct; }
    }

    // ── Trade Signals (FR-SE-006) ──

    public static class TradeSignal {
        private String ticker;
        private String holdingName;
        private String signal;
        private String rationale;
        private String strategySource;
        private BigDecimal confidence;
        private BigDecimal currentPrice;
        private BigDecimal targetPrice;
        private BigDecimal stopLoss;

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public String getHoldingName() { return holdingName; }
        public void setHoldingName(String holdingName) { this.holdingName = holdingName; }
        public String getSignal() { return signal; }
        public void setSignal(String signal) { this.signal = signal; }
        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }
        public String getStrategySource() { return strategySource; }
        public void setStrategySource(String strategySource) { this.strategySource = strategySource; }
        public BigDecimal getConfidence() { return confidence; }
        public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getTargetPrice() { return targetPrice; }
        public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }
        public BigDecimal getStopLoss() { return stopLoss; }
        public void setStopLoss(BigDecimal stopLoss) { this.stopLoss = stopLoss; }
    }

    // ── Portfolio Signals Response ──

    public static class PortfolioSignals {
        private Long portfolioId;
        private String portfolioName;
        private List<TradeSignal> signals;
        private List<TaxLossCandidate> taxLossCandidates;

        public Long getPortfolioId() { return portfolioId; }
        public void setPortfolioId(Long portfolioId) { this.portfolioId = portfolioId; }
        public String getPortfolioName() { return portfolioName; }
        public void setPortfolioName(String portfolioName) { this.portfolioName = portfolioName; }
        public List<TradeSignal> getSignals() { return signals; }
        public void setSignals(List<TradeSignal> signals) { this.signals = signals; }
        public List<TaxLossCandidate> getTaxLossCandidates() { return taxLossCandidates; }
        public void setTaxLossCandidates(List<TaxLossCandidate> taxLossCandidates) { this.taxLossCandidates = taxLossCandidates; }
    }

    // ── Tax-Loss Harvesting (FR-SE-007) ──

    public static class TaxLossCandidate {
        private String ticker;
        private String name;
        private BigDecimal purchasePrice;
        private BigDecimal currentPrice;
        private BigDecimal unrealizedLoss;
        private BigDecimal unrealizedLossPct;
        private String suggestion;

        public String getTicker() { return ticker; }
        public void setTicker(String ticker) { this.ticker = ticker; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPurchasePrice() { return purchasePrice; }
        public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getUnrealizedLoss() { return unrealizedLoss; }
        public void setUnrealizedLoss(BigDecimal unrealizedLoss) { this.unrealizedLoss = unrealizedLoss; }
        public BigDecimal getUnrealizedLossPct() { return unrealizedLossPct; }
        public void setUnrealizedLossPct(BigDecimal unrealizedLossPct) { this.unrealizedLossPct = unrealizedLossPct; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
}
