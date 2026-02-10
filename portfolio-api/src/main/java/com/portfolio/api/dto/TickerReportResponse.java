package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comprehensive ticker screener report (FR-SC-001 through FR-SC-005).
 */
public class TickerReportResponse {

    // Company profile (FR-SC-001)
    private String ticker;
    private String name;
    private String industry;
    private String sector;
    private String country;
    private String currency;
    private String exchange;
    private String logo;
    private String weburl;

    // Market data (FR-SC-002)
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal weekHigh52;
    private BigDecimal weekLow52;
    private BigDecimal marketCap;
    private BigDecimal peRatio;
    private BigDecimal eps;
    private BigDecimal dividendYield;
    private BigDecimal beta;
    private BigDecimal revenueGrowthTTM;
    private BigDecimal earningsGrowthTTM;

    // Financial statements (FR-SC-003)
    private List<FinancialStatement> annualFinancials;
    private List<FinancialStatement> quarterlyFinancials;

    // SEC filings (FR-SC-004)
    private List<SecFiling> secFilings;

    // Analyst recommendations (FR-SC-005)
    private List<AnalystRecommendation> recommendations;
    private PriceTarget priceTarget;
    private List<EarningsData> earnings;

    // Nested types

    public static class FinancialStatement {
        private String period;
        private int year;
        private String quarter;
        private BigDecimal revenue;
        private BigDecimal netIncome;
        private BigDecimal totalAssets;
        private BigDecimal totalLiabilities;
        private BigDecimal totalEquity;
        private BigDecimal operatingCashFlow;

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public String getQuarter() { return quarter; }
        public void setQuarter(String quarter) { this.quarter = quarter; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
        public BigDecimal getNetIncome() { return netIncome; }
        public void setNetIncome(BigDecimal netIncome) { this.netIncome = netIncome; }
        public BigDecimal getTotalAssets() { return totalAssets; }
        public void setTotalAssets(BigDecimal totalAssets) { this.totalAssets = totalAssets; }
        public BigDecimal getTotalLiabilities() { return totalLiabilities; }
        public void setTotalLiabilities(BigDecimal totalLiabilities) { this.totalLiabilities = totalLiabilities; }
        public BigDecimal getTotalEquity() { return totalEquity; }
        public void setTotalEquity(BigDecimal totalEquity) { this.totalEquity = totalEquity; }
        public BigDecimal getOperatingCashFlow() { return operatingCashFlow; }
        public void setOperatingCashFlow(BigDecimal operatingCashFlow) { this.operatingCashFlow = operatingCashFlow; }
    }

    public static class SecFiling {
        private String form;
        private String filedDate;
        private String acceptedDate;
        private String reportUrl;

        public String getForm() { return form; }
        public void setForm(String form) { this.form = form; }
        public String getFiledDate() { return filedDate; }
        public void setFiledDate(String filedDate) { this.filedDate = filedDate; }
        public String getAcceptedDate() { return acceptedDate; }
        public void setAcceptedDate(String acceptedDate) { this.acceptedDate = acceptedDate; }
        public String getReportUrl() { return reportUrl; }
        public void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }
    }

    public static class AnalystRecommendation {
        private String period;
        private int strongBuy;
        private int buy;
        private int hold;
        private int sell;
        private int strongSell;

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public int getStrongBuy() { return strongBuy; }
        public void setStrongBuy(int strongBuy) { this.strongBuy = strongBuy; }
        public int getBuy() { return buy; }
        public void setBuy(int buy) { this.buy = buy; }
        public int getHold() { return hold; }
        public void setHold(int hold) { this.hold = hold; }
        public int getSell() { return sell; }
        public void setSell(int sell) { this.sell = sell; }
        public int getStrongSell() { return strongSell; }
        public void setStrongSell(int strongSell) { this.strongSell = strongSell; }
    }

    public static class PriceTarget {
        private BigDecimal targetHigh;
        private BigDecimal targetLow;
        private BigDecimal targetMean;
        private BigDecimal targetMedian;

        public BigDecimal getTargetHigh() { return targetHigh; }
        public void setTargetHigh(BigDecimal targetHigh) { this.targetHigh = targetHigh; }
        public BigDecimal getTargetLow() { return targetLow; }
        public void setTargetLow(BigDecimal targetLow) { this.targetLow = targetLow; }
        public BigDecimal getTargetMean() { return targetMean; }
        public void setTargetMean(BigDecimal targetMean) { this.targetMean = targetMean; }
        public BigDecimal getTargetMedian() { return targetMedian; }
        public void setTargetMedian(BigDecimal targetMedian) { this.targetMedian = targetMedian; }
    }

    public static class EarningsData {
        private String period;
        private BigDecimal actual;
        private BigDecimal estimate;
        private BigDecimal surprise;
        private BigDecimal surprisePercent;

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public BigDecimal getActual() { return actual; }
        public void setActual(BigDecimal actual) { this.actual = actual; }
        public BigDecimal getEstimate() { return estimate; }
        public void setEstimate(BigDecimal estimate) { this.estimate = estimate; }
        public BigDecimal getSurprise() { return surprise; }
        public void setSurprise(BigDecimal surprise) { this.surprise = surprise; }
        public BigDecimal getSurprisePercent() { return surprisePercent; }
        public void setSurprisePercent(BigDecimal surprisePercent) { this.surprisePercent = surprisePercent; }
    }

    // Top-level getters/setters

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getWeburl() { return weburl; }
    public void setWeburl(String weburl) { this.weburl = weburl; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
    public BigDecimal getPreviousClose() { return previousClose; }
    public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; }
    public BigDecimal getChange() { return change; }
    public void setChange(BigDecimal change) { this.change = change; }
    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }
    public BigDecimal getWeekHigh52() { return weekHigh52; }
    public void setWeekHigh52(BigDecimal weekHigh52) { this.weekHigh52 = weekHigh52; }
    public BigDecimal getWeekLow52() { return weekLow52; }
    public void setWeekLow52(BigDecimal weekLow52) { this.weekLow52 = weekLow52; }
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
    public BigDecimal getRevenueGrowthTTM() { return revenueGrowthTTM; }
    public void setRevenueGrowthTTM(BigDecimal revenueGrowthTTM) { this.revenueGrowthTTM = revenueGrowthTTM; }
    public BigDecimal getEarningsGrowthTTM() { return earningsGrowthTTM; }
    public void setEarningsGrowthTTM(BigDecimal earningsGrowthTTM) { this.earningsGrowthTTM = earningsGrowthTTM; }

    public List<FinancialStatement> getAnnualFinancials() { return annualFinancials; }
    public void setAnnualFinancials(List<FinancialStatement> annualFinancials) { this.annualFinancials = annualFinancials; }
    public List<FinancialStatement> getQuarterlyFinancials() { return quarterlyFinancials; }
    public void setQuarterlyFinancials(List<FinancialStatement> quarterlyFinancials) { this.quarterlyFinancials = quarterlyFinancials; }

    public List<SecFiling> getSecFilings() { return secFilings; }
    public void setSecFilings(List<SecFiling> secFilings) { this.secFilings = secFilings; }

    public List<AnalystRecommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<AnalystRecommendation> recommendations) { this.recommendations = recommendations; }
    public PriceTarget getPriceTarget() { return priceTarget; }
    public void setPriceTarget(PriceTarget priceTarget) { this.priceTarget = priceTarget; }
    public List<EarningsData> getEarnings() { return earnings; }
    public void setEarnings(List<EarningsData> earnings) { this.earnings = earnings; }
}
