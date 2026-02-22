package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;

public class TradingAdvisorResponse {
    private String ticker;
    private String name;
    private String industry;
    private BigDecimal currentPrice;
    private BigDecimal changePercent;
    private long recordsSynced;
    private long storedRecords;
    private Indicators indicators;
    private Risk risk;
    private String recommendation;
    private String rationale;
    private List<ChartPoint> chart;

    public static class Indicators {
        private BigDecimal sma20;
        private BigDecimal ema20;
        private BigDecimal rsi14;
        private BigDecimal macd;
        private BigDecimal signal9;
        private BigDecimal annualizedVolatility;
        public BigDecimal getSma20() { return sma20; }
        public void setSma20(BigDecimal sma20) { this.sma20 = sma20; }
        public BigDecimal getEma20() { return ema20; }
        public void setEma20(BigDecimal ema20) { this.ema20 = ema20; }
        public BigDecimal getRsi14() { return rsi14; }
        public void setRsi14(BigDecimal rsi14) { this.rsi14 = rsi14; }
        public BigDecimal getMacd() { return macd; }
        public void setMacd(BigDecimal macd) { this.macd = macd; }
        public BigDecimal getSignal9() { return signal9; }
        public void setSignal9(BigDecimal signal9) { this.signal9 = signal9; }
        public BigDecimal getAnnualizedVolatility() { return annualizedVolatility; }
        public void setAnnualizedVolatility(BigDecimal annualizedVolatility) { this.annualizedVolatility = annualizedVolatility; }
    }

    public static class Risk {
        private BigDecimal var95;
        private BigDecimal var99;
        public BigDecimal getVar95() { return var95; }
        public void setVar95(BigDecimal var95) { this.var95 = var95; }
        public BigDecimal getVar99() { return var99; }
        public void setVar99(BigDecimal var99) { this.var99 = var99; }
    }

    public static class ChartPoint {
        private String date;
        private BigDecimal close;
        public ChartPoint() {}
        public ChartPoint(String date, BigDecimal close) { this.date = date; this.close = close; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getClose() { return close; }
        public void setClose(BigDecimal close) { this.close = close; }
    }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }
    public long getRecordsSynced() { return recordsSynced; }
    public void setRecordsSynced(long recordsSynced) { this.recordsSynced = recordsSynced; }
    public long getStoredRecords() { return storedRecords; }
    public void setStoredRecords(long storedRecords) { this.storedRecords = storedRecords; }
    public Indicators getIndicators() { return indicators; }
    public void setIndicators(Indicators indicators) { this.indicators = indicators; }
    public Risk getRisk() { return risk; }
    public void setRisk(Risk risk) { this.risk = risk; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
    public List<ChartPoint> getChart() { return chart; }
    public void setChart(List<ChartPoint> chart) { this.chart = chart; }
}
