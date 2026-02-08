package com.portfolio.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for portfolio data.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
public class PortfolioResponse {

    private Long id;
    private String name;
    private String description;
    private String baseCurrency;
    private int holdingCount;
    private List<HoldingResponse> holdings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public int getHoldingCount() { return holdingCount; }
    public void setHoldingCount(int holdingCount) { this.holdingCount = holdingCount; }

    public List<HoldingResponse> getHoldings() { return holdings; }
    public void setHoldings(List<HoldingResponse> holdings) { this.holdings = holdings; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
