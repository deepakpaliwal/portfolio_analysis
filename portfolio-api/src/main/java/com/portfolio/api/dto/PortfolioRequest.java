package com.portfolio.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a portfolio.
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
public class PortfolioRequest {

    @NotBlank(message = "Portfolio name is required")
    @Size(max = 200, message = "Portfolio name must not exceed 200 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(min = 3, max = 3, message = "Base currency must be a 3-letter ISO code")
    private String baseCurrency = "USD";

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }
}
