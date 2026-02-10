package com.portfolio.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Technical indicator data for a ticker (FR-SC-009).
 */
public class TechnicalIndicatorResponse {

    private String ticker;
    private String indicator;
    private String resolution;
    private List<DataPoint> values;

    public static class DataPoint {
        private long timestamp;
        private BigDecimal value;

        public DataPoint() {}
        public DataPoint(long timestamp, BigDecimal value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
    }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getIndicator() { return indicator; }
    public void setIndicator(String indicator) { this.indicator = indicator; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public List<DataPoint> getValues() { return values; }
    public void setValues(List<DataPoint> values) { this.values = values; }
}
