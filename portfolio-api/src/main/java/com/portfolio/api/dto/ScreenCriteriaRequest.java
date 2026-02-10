package com.portfolio.api.dto;

import java.math.BigDecimal;

/**
 * Custom screening criteria request (FR-SC-008).
 */
public class ScreenCriteriaRequest {

    private String sector;
    private String exchange;

    private BigDecimal peRatioMin;
    private BigDecimal peRatioMax;
    private BigDecimal dividendYieldMin;
    private BigDecimal dividendYieldMax;
    private BigDecimal marketCapMin;
    private BigDecimal marketCapMax;
    private BigDecimal betaMin;
    private BigDecimal betaMax;
    private BigDecimal epsMin;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private BigDecimal weekHigh52PctMin;
    private BigDecimal weekHigh52PctMax;

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public BigDecimal getPeRatioMin() { return peRatioMin; }
    public void setPeRatioMin(BigDecimal peRatioMin) { this.peRatioMin = peRatioMin; }
    public BigDecimal getPeRatioMax() { return peRatioMax; }
    public void setPeRatioMax(BigDecimal peRatioMax) { this.peRatioMax = peRatioMax; }
    public BigDecimal getDividendYieldMin() { return dividendYieldMin; }
    public void setDividendYieldMin(BigDecimal dividendYieldMin) { this.dividendYieldMin = dividendYieldMin; }
    public BigDecimal getDividendYieldMax() { return dividendYieldMax; }
    public void setDividendYieldMax(BigDecimal dividendYieldMax) { this.dividendYieldMax = dividendYieldMax; }
    public BigDecimal getMarketCapMin() { return marketCapMin; }
    public void setMarketCapMin(BigDecimal marketCapMin) { this.marketCapMin = marketCapMin; }
    public BigDecimal getMarketCapMax() { return marketCapMax; }
    public void setMarketCapMax(BigDecimal marketCapMax) { this.marketCapMax = marketCapMax; }
    public BigDecimal getBetaMin() { return betaMin; }
    public void setBetaMin(BigDecimal betaMin) { this.betaMin = betaMin; }
    public BigDecimal getBetaMax() { return betaMax; }
    public void setBetaMax(BigDecimal betaMax) { this.betaMax = betaMax; }
    public BigDecimal getEpsMin() { return epsMin; }
    public void setEpsMin(BigDecimal epsMin) { this.epsMin = epsMin; }
    public BigDecimal getPriceMin() { return priceMin; }
    public void setPriceMin(BigDecimal priceMin) { this.priceMin = priceMin; }
    public BigDecimal getPriceMax() { return priceMax; }
    public void setPriceMax(BigDecimal priceMax) { this.priceMax = priceMax; }
    public BigDecimal getWeekHigh52PctMin() { return weekHigh52PctMin; }
    public void setWeekHigh52PctMin(BigDecimal weekHigh52PctMin) { this.weekHigh52PctMin = weekHigh52PctMin; }
    public BigDecimal getWeekHigh52PctMax() { return weekHigh52PctMax; }
    public void setWeekHigh52PctMax(BigDecimal weekHigh52PctMax) { this.weekHigh52PctMax = weekHigh52PctMax; }
}
