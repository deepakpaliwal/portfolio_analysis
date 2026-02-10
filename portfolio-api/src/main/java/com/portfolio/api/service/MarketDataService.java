package com.portfolio.api.service;

import com.portfolio.api.config.FinnhubConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    private final RestTemplate restTemplate;
    private final FinnhubConfig finnhubConfig;

    public MarketDataService(RestTemplate restTemplate, FinnhubConfig finnhubConfig) {
        this.restTemplate = restTemplate;
        this.finnhubConfig = finnhubConfig;
    }

    // ───────── Existing: Quote + FX ─────────

    @Cacheable(value = "quotes", key = "#ticker")
    public BigDecimal getCurrentPrice(String ticker) {
        try {
            String url = String.format("%s/quote?symbol=%s&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, finnhubConfig.getApiKey());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.get("c") != null) {
                double currentPrice = ((Number) response.get("c")).doubleValue();
                if (currentPrice > 0) {
                    return BigDecimal.valueOf(currentPrice);
                }
            }
            log.warn("No price data for ticker: {}", ticker);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch price for {}: {}", ticker, e.getMessage());
            return null;
        }
    }

    /** Returns full quote map with keys: c, h, l, o, pc, d, dp */
    @Cacheable(value = "quotes", key = "'full-' + #ticker")
    public Map<String, Object> getQuote(String ticker) {
        try {
            String url = String.format("%s/quote?symbol=%s&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch quote for {}: {}", ticker, e.getMessage());
            return null;
        }
    }

    @Cacheable(value = "fxRates", key = "#fromCurrency + '-' + #toCurrency")
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }
        try {
            String url = String.format("%s/forex/rates?base=%s&token=%s",
                    finnhubConfig.getBaseUrl(), fromCurrency, finnhubConfig.getApiKey());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.get("quote") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> quotes = (Map<String, Object>) response.get("quote");
                Object rate = quotes.get(toCurrency);
                if (rate != null) {
                    return BigDecimal.valueOf(((Number) rate).doubleValue());
                }
            }
            log.warn("No FX rate for {}/{}", fromCurrency, toCurrency);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch FX rate {}/{}: {}", fromCurrency, toCurrency, e.getMessage());
            return null;
        }
    }

    // ───────── Company Profile (FR-SC-001) ─────────

    @Cacheable(value = "companyProfiles", key = "#ticker")
    public Map<String, Object> getCompanyProfile(String ticker) {
        try {
            String url = String.format("%s/stock/profile2?symbol=%s&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch company profile for {}: {}", ticker, e.getMessage());
            return null;
        }
    }

    // ───────── Basic Financials / Metrics (FR-SC-002) ─────────

    @Cacheable(value = "basicFinancials", key = "#ticker")
    public Map<String, Object> getBasicFinancials(String ticker) {
        try {
            String url = String.format("%s/stock/metric?symbol=%s&metric=all&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch basic financials for {}: {}", ticker, e.getMessage());
            return null;
        }
    }

    // ───────── Financial Statements (FR-SC-003) ─────────

    @Cacheable(value = "financialStatements", key = "#ticker + '-' + #freq")
    public List<Map<String, Object>> getFinancialStatements(String ticker, String freq) {
        try {
            String url = String.format("%s/stock/financials-reported?symbol=%s&freq=%s&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, freq, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("data") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                return data;
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch financial statements for {}: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ───────── SEC Filings (FR-SC-004) ─────────

    @Cacheable(value = "secFilings", key = "#ticker")
    public List<Map<String, Object>> getSecFilings(String ticker) {
        try {
            String url = String.format("%s/stock/filings?symbol=%s&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            return response != null ? response : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch SEC filings for {}: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ───────── Analyst Recommendations (FR-SC-005) ─────────

    @Cacheable(value = "recommendations", key = "#ticker")
    public List<Map<String, Object>> getRecommendations(String ticker) {
        try {
            String url = String.format("%s/stock/recommendation?symbol=%s&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            return response != null ? response : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch recommendations for {}: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Cacheable(value = "priceTargets", key = "#ticker")
    public Map<String, Object> getPriceTarget(String ticker) {
        try {
            String url = String.format("%s/stock/price-target?symbol=%s&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch price target for {}: {}", ticker, e.getMessage());
            return null;
        }
    }

    @Cacheable(value = "earnings", key = "#ticker")
    public List<Map<String, Object>> getEarnings(String ticker) {
        try {
            String url = String.format("%s/stock/earnings?symbol=%s&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            return response != null ? response : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch earnings for {}: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ───────── Sector Peers (FR-SC-006/007) ─────────

    @Cacheable(value = "peers", key = "#ticker")
    public List<String> getPeers(String ticker) {
        try {
            String url = String.format("%s/stock/peers?symbol=%s&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            List<String> response = restTemplate.getForObject(url, List.class);
            return response != null ? response : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch peers for {}: {}", ticker, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ───────── Stock Symbols (for custom screener FR-SC-008) ─────────

    @Cacheable(value = "stockSymbols", key = "#exchange")
    public List<Map<String, Object>> getStockSymbols(String exchange) {
        try {
            String url = String.format("%s/stock/symbol?exchange=%s&token=%s",
                    finnhubConfig.getBaseUrl(), exchange, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            return response != null ? response : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch stock symbols for exchange {}: {}", exchange, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ───────── Technical Indicators (FR-SC-009) ─────────

    @Cacheable(value = "technicalIndicators", key = "#ticker + '-' + #indicator + '-' + #resolution + '-' + #timeperiod")
    public Map<String, Object> getTechnicalIndicator(String ticker, String indicator,
                                                      String resolution, long from, long to, int timeperiod) {
        try {
            String url = String.format(
                    "%s/indicator?symbol=%s&resolution=%s&from=%d&to=%d&indicator=%s&timeperiod=%d&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, resolution, from, to, indicator, timeperiod,
                    finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response;
        } catch (Exception e) {
            log.error("Failed to fetch technical indicator {} for {}: {}", indicator, ticker, e.getMessage());
            return null;
        }
    }

    // ───────── Stock Candles / Historical Prices (FR-RA) ─────────

    @Cacheable(value = "stockCandles", key = "#ticker + '-' + #resolution + '-' + #from + '-' + #to")
    public Map<String, Object> getStockCandles(String ticker, String resolution, long from, long to) {
        try {
            String url = String.format("%s/stock/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, resolution, from, to, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "ok".equals(response.get("s"))) {
                return response;
            }
            log.warn("No candle data for {}: status={}", ticker, response != null ? response.get("s") : "null");
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch candles for {}: {}", ticker, e.getMessage());
            return null;
        }
    }
}
