package com.portfolio.api.service;

import com.portfolio.api.config.FinnhubConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
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

    /**
     * Fetches the current price for a given ticker from Finnhub.
     * Finnhub /quote endpoint returns: { c: currentPrice, h: high, l: low, o: open, pc: previousClose }
     */
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

    /**
     * Fetches the FX exchange rate between two currencies using Finnhub forex rates.
     * Finnhub /forex/rates endpoint: base currency -> map of rates.
     */
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
}
