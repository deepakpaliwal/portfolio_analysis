package com.portfolio.api.dto;

import com.portfolio.api.model.AssetType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a holding.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
public class HoldingRequest {

    @NotNull(message = "Asset type is required")
    private AssetType assetType;

    @NotBlank(message = "Ticker is required")
    @Size(max = 50, message = "Ticker must not exceed 50 characters")
    private String ticker;

    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0001", message = "Purchase price must be positive")
    private BigDecimal purchasePrice;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency = "USD";

    private String sector;
    private String category;

    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
