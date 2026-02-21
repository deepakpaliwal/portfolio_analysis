package com.portfolio.api.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_price_history",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ticker", "asset_class", "trade_date", "option_contract"}))
public class MarketPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String ticker;

    @Column(name = "asset_class", nullable = false, length = 20)
    private String assetClass;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "open_price", precision = 19, scale = 4)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 19, scale = 4)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 19, scale = 4)
    private BigDecimal lowPrice;

    @Column(name = "close_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal closePrice;

    @Column
    private Long volume;

    @Column(name = "option_contract", length = 120)
    private String optionContract;

    @Column(name = "option_type", length = 10)
    private String optionType;

    @Column(name = "option_strike", precision = 19, scale = 4)
    private BigDecimal optionStrike;

    @Column(name = "option_expiry")
    private LocalDate optionExpiry;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @PrePersist
    protected void onCreate() {
        fetchedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getAssetClass() { return assetClass; }
    public void setAssetClass(String assetClass) { this.assetClass = assetClass; }
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    public String getOptionContract() { return optionContract; }
    public void setOptionContract(String optionContract) { this.optionContract = optionContract; }
    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }
    public BigDecimal getOptionStrike() { return optionStrike; }
    public void setOptionStrike(BigDecimal optionStrike) { this.optionStrike = optionStrike; }
    public LocalDate getOptionExpiry() { return optionExpiry; }
    public void setOptionExpiry(LocalDate optionExpiry) { this.optionExpiry = optionExpiry; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
}
