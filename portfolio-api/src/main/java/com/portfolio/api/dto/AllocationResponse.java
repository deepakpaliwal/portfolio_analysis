package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AllocationResponse {

    private List<AllocationEntry> byAssetType;
    private List<AllocationEntry> bySector;
    private List<AllocationEntry> byCurrency;
    private BigDecimal totalCostBasis;

    public List<AllocationEntry> getByAssetType() { return byAssetType; }
    public void setByAssetType(List<AllocationEntry> byAssetType) { this.byAssetType = byAssetType; }

    public List<AllocationEntry> getBySector() { return bySector; }
    public void setBySector(List<AllocationEntry> bySector) { this.bySector = bySector; }

    public List<AllocationEntry> getByCurrency() { return byCurrency; }
    public void setByCurrency(List<AllocationEntry> byCurrency) { this.byCurrency = byCurrency; }

    public BigDecimal getTotalCostBasis() { return totalCostBasis; }
    public void setTotalCostBasis(BigDecimal totalCostBasis) { this.totalCostBasis = totalCostBasis; }

    public static class AllocationEntry {
        private String label;
        private BigDecimal value;
        private BigDecimal percentage;

        public AllocationEntry() {}

        public AllocationEntry(String label, BigDecimal value, BigDecimal percentage) {
            this.label = label;
            this.value = value;
            this.percentage = percentage;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }

        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    }
}
