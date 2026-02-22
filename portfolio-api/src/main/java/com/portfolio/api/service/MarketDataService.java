package com.portfolio.api.service;

import com.portfolio.api.config.FinnhubConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);
    private static final String YAHOO_CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart";
    private static final String BINANCE_KLINES_URL = "https://api.binance.com/api/v3/klines";

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

    /**
     * Fetch historical daily OHLCV data. Tries Finnhub first, falls back to Yahoo Finance.
     * Returns a map with keys: c (close), o (open), h (high), l (low), v (volume), t (timestamps), s ("ok").
     */
    @Cacheable(value = "stockCandles", key = "#ticker + '-' + #resolution + '-' + #from + '-' + #to")
    public Map<String, Object> getStockCandles(String ticker, String resolution, long from, long to) {
        // Try Finnhub first
        try {
            String url = String.format("%s/stock/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s",
                    finnhubConfig.getBaseUrl(), ticker, resolution, from, to, finnhubConfig.getApiKey());
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "ok".equals(response.get("s"))) {
                return response;
            }
            log.info("Finnhub candle returned status={} for {}, trying Yahoo Finance",
                    response != null ? response.get("s") : "null", ticker);
        } catch (Exception e) {
            log.info("Finnhub candle failed for {} ({}), trying Yahoo Finance", ticker, e.getMessage());
        }

        // Fallback to Yahoo Finance
        Map<String, Object> yahoo = getStockCandlesFromYahoo(ticker, resolution, from, to);
        if (yahoo != null && "ok".equals(yahoo.get("s"))) {
            return yahoo;
        }

        // Alternate fallback for crypto symbols when Yahoo is unavailable/rate-limited
        if (isCryptoTicker(ticker)) {
            return getCryptoCandlesFromBinance(ticker, resolution, from, to);
        }

        return null;
    }

    /**
     * Fetch historical daily prices from Yahoo Finance v8 chart API (free, no API key).
     * Converts the Yahoo response format to the same structure as Finnhub candles.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getStockCandlesFromYahoo(String ticker, String resolution, long from, long to) {
        try {
            String interval = "D".equalsIgnoreCase(resolution) ? "1d" : "1h";
            String url = String.format("%s/%s?period1=%d&period2=%d&interval=%s",
                    YAHOO_CHART_URL, ticker, from, to, interval);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || response.get("chart") == null) {
                log.warn("Yahoo Finance returned null for {}", ticker);
                return null;
            }

            Map<String, Object> chart = (Map<String, Object>) response.get("chart");
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");
            if (results == null || results.isEmpty()) {
                log.warn("Yahoo Finance returned empty results for {}", ticker);
                return null;
            }

            Map<String, Object> result = results.get(0);
            List<Number> timestamps = (List<Number>) result.get("timestamp");
            if (timestamps == null || timestamps.isEmpty()) {
                log.warn("Yahoo Finance returned no timestamps for {}", ticker);
                return null;
            }

            Map<String, Object> indicators = (Map<String, Object>) result.get("indicators");
            List<Map<String, Object>> quoteList = (List<Map<String, Object>>) indicators.get("quote");
            if (quoteList == null || quoteList.isEmpty()) {
                return null;
            }

            Map<String, Object> quote = quoteList.get(0);
            List<Number> closes = (List<Number>) quote.get("close");
            List<Number> opens = (List<Number>) quote.get("open");
            List<Number> highs = (List<Number>) quote.get("high");
            List<Number> lows = (List<Number>) quote.get("low");
            List<Number> volumes = (List<Number>) quote.get("volume");

            // Filter out entries where close is null (non-trading days Yahoo sometimes includes)
            List<Number> filteredT = new ArrayList<>();
            List<Number> filteredC = new ArrayList<>();
            List<Number> filteredO = new ArrayList<>();
            List<Number> filteredH = new ArrayList<>();
            List<Number> filteredL = new ArrayList<>();
            List<Number> filteredV = new ArrayList<>();

            for (int i = 0; i < timestamps.size(); i++) {
                Number closeVal = (closes != null && i < closes.size()) ? closes.get(i) : null;
                if (closeVal == null) continue;

                filteredT.add(timestamps.get(i));
                filteredC.add(closeVal);
                filteredO.add((opens != null && i < opens.size()) ? opens.get(i) : closeVal);
                filteredH.add((highs != null && i < highs.size()) ? highs.get(i) : closeVal);
                filteredL.add((lows != null && i < lows.size()) ? lows.get(i) : closeVal);
                filteredV.add((volumes != null && i < volumes.size()) ? volumes.get(i) : 0);
            }

            if (filteredC.isEmpty()) {
                log.warn("Yahoo Finance returned all-null closes for {}", ticker);
                return null;
            }

            // Convert to Finnhub-compatible format
            Map<String, Object> candles = new HashMap<>();
            candles.put("s", "ok");
            candles.put("t", filteredT);
            candles.put("c", filteredC);
            candles.put("o", filteredO);
            candles.put("h", filteredH);
            candles.put("l", filteredL);
            candles.put("v", filteredV);

            log.info("Yahoo Finance returned {} price records for {}", filteredC.size(), ticker);
            return candles;
        } catch (Exception e) {
            log.error("Failed to fetch candles from Yahoo Finance for {}: {}", ticker, e.getMessage());
            return null;
        }
    }

    private boolean isCryptoTicker(String ticker) {
        return ticker != null && (ticker.contains("-") || ticker.endsWith("USDT") || ticker.endsWith("USD"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getCryptoCandlesFromBinance(String ticker, String resolution, long from, long to) {
        String interval = "D".equalsIgnoreCase(resolution) ? "1d" : "1h";
        String symbol = toBinanceSymbol(ticker);
        if (symbol == null) {
            return null;
        }

        long startMs = from * 1000L;
        long endMs = to * 1000L;

        List<Number> ts = new ArrayList<>();
        List<Number> o = new ArrayList<>();
        List<Number> h = new ArrayList<>();
        List<Number> l = new ArrayList<>();
        List<Number> c = new ArrayList<>();
        List<Number> v = new ArrayList<>();

        try {
            long cursor = startMs;
            while (cursor < endMs) {
                String url = String.format(
                        "%s?symbol=%s&interval=%s&startTime=%d&endTime=%d&limit=1000",
                        BINANCE_KLINES_URL, symbol, interval, cursor, endMs);

                List<List<Object>> rows = restTemplate.getForObject(url, List.class);
                if (rows == null || rows.isEmpty()) break;

                for (List<Object> row : rows) {
                    if (row.size() < 6) continue;
                    long openTimeMs = ((Number) row.get(0)).longValue();
                    ts.add(openTimeMs / 1000L);
                    o.add(Double.parseDouble(String.valueOf(row.get(1))));
                    h.add(Double.parseDouble(String.valueOf(row.get(2))));
                    l.add(Double.parseDouble(String.valueOf(row.get(3))));
                    c.add(Double.parseDouble(String.valueOf(row.get(4))));
                    v.add(Double.parseDouble(String.valueOf(row.get(5))));
                }

                long lastOpenMs = ((Number) rows.get(rows.size() - 1).get(0)).longValue();
                long stepMs = "1d".equals(interval) ? 86_400_000L : 3_600_000L;
                long nextCursor = lastOpenMs + stepMs;
                if (nextCursor <= cursor) break;
                cursor = nextCursor;

                if (rows.size() < 1000) break;
            }

            if (c.isEmpty()) {
                log.warn("Binance returned no candle rows for {}", ticker);
                return null;
            }

            Map<String, Object> candles = new HashMap<>();
            candles.put("s", "ok");
            candles.put("t", ts);
            candles.put("o", o);
            candles.put("h", h);
            candles.put("l", l);
            candles.put("c", c);
            candles.put("v", v);
            log.info("Binance returned {} price records for {}", c.size(), ticker);
            return candles;
        } catch (Exception e) {
            log.error("Failed to fetch candles from Binance for {}: {}", ticker, e.getMessage());
            return null;
        }
    }

    private String toBinanceSymbol(String ticker) {
        if (ticker == null) return null;
        String upper = ticker.toUpperCase(Locale.ROOT).trim();
        if (upper.endsWith("-USD")) {
            return upper.substring(0, upper.length() - 4) + "USDT";
        }
        if (upper.endsWith("-USDT")) {
            return upper.replace("-", "");
        }
        if (upper.endsWith("USDT")) {
            return upper;
        }
        return null;
    }

}
