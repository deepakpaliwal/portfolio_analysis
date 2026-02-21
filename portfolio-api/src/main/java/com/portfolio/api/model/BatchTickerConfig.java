package com.portfolio.api.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_ticker_config")
public class BatchTickerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String ticker;

    @Column(name = "ticker_name", length = 200)
    private String tickerName;

    @Column(nullable = false)
    private Boolean enabled = true;


    @Column(name = "asset_class", nullable = false, length = 20)
    private String assetClass = "EQUITY";

    @Column(name = "market_source", nullable = false, length = 50)
    private String marketSource = "YAHOO";

    @Column(name = "option_contract", length = 120)
    private String optionContract;

    @Column(name = "option_type", length = 10)
    private String optionType;

    @Column(name = "option_strike", precision = 19, scale = 4)
    private java.math.BigDecimal optionStrike;

    @Column(name = "option_expiry")
    private LocalDate optionExpiry;

    @Column(name = "last_sync_date")
    private LocalDate lastSyncDate;

    @Column(name = "record_count", nullable = false)
    private Long recordCount = 0L;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "last_run_status", length = 20)
    private String lastRunStatus;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getTickerName() { return tickerName; }
    public void setTickerName(String tickerName) { this.tickerName = tickerName; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public LocalDate getLastSyncDate() { return lastSyncDate; }
    public void setLastSyncDate(LocalDate lastSyncDate) { this.lastSyncDate = lastSyncDate; }

    public Long getRecordCount() { return recordCount; }
    public void setRecordCount(Long recordCount) { this.recordCount = recordCount; }

    public LocalDateTime getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(LocalDateTime lastRunAt) { this.lastRunAt = lastRunAt; }

    public String getLastRunStatus() { return lastRunStatus; }
    public void setLastRunStatus(String lastRunStatus) { this.lastRunStatus = lastRunStatus; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }


    public String getAssetClass() { return assetClass; }
    public void setAssetClass(String assetClass) { this.assetClass = assetClass; }

    public String getMarketSource() { return marketSource; }
    public void setMarketSource(String marketSource) { this.marketSource = marketSource; }

    public String getOptionContract() { return optionContract; }
    public void setOptionContract(String optionContract) { this.optionContract = optionContract; }

    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }

    public java.math.BigDecimal getOptionStrike() { return optionStrike; }
    public void setOptionStrike(java.math.BigDecimal optionStrike) { this.optionStrike = optionStrike; }

    public LocalDate getOptionExpiry() { return optionExpiry; }
    public void setOptionExpiry(LocalDate optionExpiry) { this.optionExpiry = optionExpiry; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
