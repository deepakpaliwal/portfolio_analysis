package com.portfolio.api.service;

import com.portfolio.api.model.BatchTickerConfig;
import com.portfolio.api.model.StockPriceHistory;
import com.portfolio.api.repository.BatchScheduleConfigRepository;
import com.portfolio.api.repository.BatchTickerConfigRepository;
import com.portfolio.api.repository.StockPriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Batch service for fetching and storing historical stock prices.
 *
 * - Fetches from Yahoo Finance v8 chart API with proper User-Agent headers
 * - Stores data in CSV files under data/prices/{TICKER}.csv
 * - Also persists to stock_price_history DB table for risk analytics
 * - Uses watermark (last_sync_date) to only fetch new data on subsequent runs
 * - Configurable rate limiting between requests
 */
@Service
public class PriceFetchBatchService {

    private static final Logger log = LoggerFactory.getLogger(PriceFetchBatchService.class);
    private static final String YAHOO_CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart";
    private static final String CSV_HEADER = "date,open,high,low,close,volume";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int LOOKBACK_YEARS = 5;
    private static final String PRICES_DIR = "data/prices";

    private final BatchTickerConfigRepository tickerConfigRepo;
    private final BatchScheduleConfigRepository scheduleConfigRepo;
    private final StockPriceHistoryRepository priceHistoryRepo;
    private final RestTemplate restTemplate;

    public PriceFetchBatchService(BatchTickerConfigRepository tickerConfigRepo,
                                   BatchScheduleConfigRepository scheduleConfigRepo,
                                   StockPriceHistoryRepository priceHistoryRepo) {
        this.tickerConfigRepo = tickerConfigRepo;
        this.scheduleConfigRepo = scheduleConfigRepo;
        this.priceHistoryRepo = priceHistoryRepo;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Run the batch fetch for all enabled tickers.
     * Returns a summary map with per-ticker results.
     */
    public Map<String, Object> runBatchFetch() {
        List<BatchTickerConfig> tickers = tickerConfigRepo.findByEnabledTrueOrderByTickerAsc();
        if (tickers.isEmpty()) {
            return Map.of("status", "no_tickers", "message", "No enabled tickers configured");
        }

        long rateLimitMs = getRateLimitMs();
        log.info("Starting batch price fetch for {} tickers (rate limit: {}ms)", tickers.size(), rateLimitMs);

        Map<String, Object> results = new LinkedHashMap<>();
        int totalRecords = 0;
        int successCount = 0;
        int errorCount = 0;

        for (BatchTickerConfig config : tickers) {
            try {
                int count = fetchAndStoreTicker(config);
                results.put(config.getTicker(), Map.of("status", "ok", "newRecords", count));
                totalRecords += count;
                successCount++;

                // Rate limit between requests
                Thread.sleep(rateLimitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Failed to fetch {}: {}", config.getTicker(), e.getMessage());
                results.put(config.getTicker(), Map.of("status", "error", "message", e.getMessage()));
                updateTickerStatus(config, "ERROR", e.getMessage());
                errorCount++;

                try { Thread.sleep(rateLimitMs); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        results.put("_summary", Map.of(
                "totalTickers", tickers.size(),
                "success", successCount,
                "errors", errorCount,
                "totalNewRecords", totalRecords
        ));

        log.info("Batch fetch complete: {} success, {} errors, {} new records",
                successCount, errorCount, totalRecords);
        return results;
    }

    /**
     * Fetch and store price data for a single ticker.
     * Uses watermark to only fetch from last_sync_date onwards.
     */
    @Transactional
    public int fetchAndStoreTicker(BatchTickerConfig config) {
        String ticker = config.getTicker();
        log.info("Fetching prices for {} (watermark: {})", ticker, config.getLastSyncDate());

        // Determine date range: from watermark (or 5 years ago) to today
        LocalDate today = LocalDate.now();
        LocalDate fromDate;
        if (config.getLastSyncDate() != null) {
            fromDate = config.getLastSyncDate().plusDays(1); // Day after watermark
            if (!fromDate.isBefore(today)) {
                log.info("Ticker {} already up to date (watermark={})", ticker, config.getLastSyncDate());
                updateTickerStatus(config, "OK", null);
                return 0;
            }
        } else {
            fromDate = today.minusYears(LOOKBACK_YEARS);
        }

        long fromEpoch = fromDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long toEpoch = today.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC);

        // Fetch from Yahoo Finance with proper headers
        List<PriceRecord> records = fetchFromYahoo(ticker, fromEpoch, toEpoch);

        if (records.isEmpty()) {
            log.info("No new price records for {}", ticker);
            updateTickerStatus(config, "OK", null);
            return 0;
        }

        // Write to CSV file (append mode if file exists, otherwise full write)
        int csvCount = writeToCsv(ticker, records);

        // Also persist to database
        int dbCount = persistToDatabase(ticker, records);

        // Update watermark and status
        LocalDate latestDate = records.stream()
                .map(PriceRecord::date)
                .max(LocalDate::compareTo)
                .orElse(today);

        config.setLastSyncDate(latestDate);
        config.setRecordCount(config.getRecordCount() + dbCount);
        updateTickerStatus(config, "OK", null);

        log.info("Fetched {} records for {} (CSV: {}, DB: {})", records.size(), ticker, csvCount, dbCount);
        return dbCount;
    }

    /**
     * Fetch a single ticker by name (creates config if needed).
     */
    @Transactional
    public int fetchSingleTicker(String l_ticker) {
        final String ticker = l_ticker.toUpperCase();
        BatchTickerConfig config = tickerConfigRepo.findByTicker(ticker)
                .orElseGet(() -> {
                    BatchTickerConfig c = new BatchTickerConfig();
                    c.setTicker(ticker);
                    c.setEnabled(true);
                    return tickerConfigRepo.save(c);
                });
        return fetchAndStoreTicker(config);
    }

    // ── Yahoo Finance Fetcher ──

    @SuppressWarnings("unchecked")
    private List<PriceRecord> fetchFromYahoo(String ticker, long fromEpoch, long toEpoch) {
        String url = String.format("%s/%s?period1=%d&period2=%d&interval=1d",
                YAHOO_CHART_URL, ticker, fromEpoch, toEpoch);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        Map<String, Object> response = responseEntity.getBody();

        if (response == null || response.get("chart") == null) {
            throw new RuntimeException("Yahoo Finance returned null for " + ticker);
        }

        Map<String, Object> chart = (Map<String, Object>) response.get("chart");
        List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("Yahoo Finance returned empty results for " + ticker);
        }

        Map<String, Object> result = results.get(0);
        List<Number> timestamps = (List<Number>) result.get("timestamp");
        if (timestamps == null || timestamps.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> indicators = (Map<String, Object>) result.get("indicators");
        List<Map<String, Object>> quoteList = (List<Map<String, Object>>) indicators.get("quote");
        if (quoteList == null || quoteList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> quote = quoteList.get(0);
        List<Number> closes = (List<Number>) quote.get("close");
        List<Number> opens = (List<Number>) quote.get("open");
        List<Number> highs = (List<Number>) quote.get("high");
        List<Number> lows = (List<Number>) quote.get("low");
        List<Number> volumes = (List<Number>) quote.get("volume");

        List<PriceRecord> records = new ArrayList<>();
        for (int i = 0; i < timestamps.size(); i++) {
            Number closeVal = (closes != null && i < closes.size()) ? closes.get(i) : null;
            if (closeVal == null) continue;

            long epochSec = timestamps.get(i).longValue();
            LocalDate tradeDate = LocalDate.ofInstant(Instant.ofEpochSecond(epochSec), ZoneOffset.UTC);

            double open = (opens != null && i < opens.size() && opens.get(i) != null)
                    ? opens.get(i).doubleValue() : closeVal.doubleValue();
            double high = (highs != null && i < highs.size() && highs.get(i) != null)
                    ? highs.get(i).doubleValue() : closeVal.doubleValue();
            double low = (lows != null && i < lows.size() && lows.get(i) != null)
                    ? lows.get(i).doubleValue() : closeVal.doubleValue();
            long volume = (volumes != null && i < volumes.size() && volumes.get(i) != null)
                    ? volumes.get(i).longValue() : 0L;

            records.add(new PriceRecord(tradeDate, open, high, low, closeVal.doubleValue(), volume));
        }

        return records;
    }

    // ── CSV File Storage ──

    private int writeToCsv(String ticker, List<PriceRecord> records) {
        try {
            Path dir = Paths.get(PRICES_DIR);
            Files.createDirectories(dir);
            Path csvFile = dir.resolve(ticker + ".csv");

            // Read existing dates from CSV to avoid duplicates
            Set<LocalDate> existingDates = new HashSet<>();
            if (Files.exists(csvFile)) {
                List<String> lines = Files.readAllLines(csvFile);
                for (int i = 1; i < lines.size(); i++) { // skip header
                    String line = lines.get(i).trim();
                    if (!line.isEmpty()) {
                        String dateStr = line.split(",")[0];
                        existingDates.add(LocalDate.parse(dateStr, DATE_FMT));
                    }
                }
            }

            // Filter to new records only
            List<PriceRecord> newRecords = records.stream()
                    .filter(r -> !existingDates.contains(r.date()))
                    .sorted(Comparator.comparing(PriceRecord::date))
                    .toList();

            if (newRecords.isEmpty()) return 0;

            // If file doesn't exist, write header + all records
            // If file exists, append new records
            if (!Files.exists(csvFile)) {
                List<String> lines = new ArrayList<>();
                lines.add(CSV_HEADER);
                for (PriceRecord r : newRecords) {
                    lines.add(formatCsvLine(r));
                }
                Files.write(csvFile, lines);
            } else {
                List<String> appendLines = new ArrayList<>();
                for (PriceRecord r : newRecords) {
                    appendLines.add(formatCsvLine(r));
                }
                Files.write(csvFile, appendLines, StandardOpenOption.APPEND);
            }

            return newRecords.size();
        } catch (IOException e) {
            log.error("Failed to write CSV for {}: {}", ticker, e.getMessage());
            return 0;
        }
    }

    private String formatCsvLine(PriceRecord r) {
        return String.format("%s,%.4f,%.4f,%.4f,%.4f,%d",
                r.date().format(DATE_FMT), r.open(), r.high(), r.low(), r.close(), r.volume());
    }

    /**
     * Read closing prices from CSV file for a ticker within a date range.
     */
    public double[] readClosingPricesFromCsv(String ticker, LocalDate from, LocalDate to) {
        Path csvFile = Paths.get(PRICES_DIR, ticker + ".csv");
        if (!Files.exists(csvFile)) {
            return new double[0];
        }

        try {
            List<String> lines = Files.readAllLines(csvFile);
            List<Double> prices = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                LocalDate date = LocalDate.parse(parts[0], DATE_FMT);
                if (!date.isBefore(from) && !date.isAfter(to)) {
                    prices.add(Double.parseDouble(parts[4])); // close price
                }
            }
            return prices.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (IOException e) {
            log.error("Failed to read CSV for {}: {}", ticker, e.getMessage());
            return new double[0];
        }
    }

    /**
     * Check if CSV file exists for a ticker.
     */
    public boolean hasCsvData(String ticker) {
        return Files.exists(Paths.get(PRICES_DIR, ticker + ".csv"));
    }

    // ── Database Persistence ──

    private int persistToDatabase(String ticker, List<PriceRecord> records) {
        // Load existing dates to avoid duplicate inserts
        Set<LocalDate> existingDates = new HashSet<>();
        priceHistoryRepo.findByTickerOrderByTradeDateAsc(ticker)
                .forEach(p -> existingDates.add(p.getTradeDate()));

        List<StockPriceHistory> newEntities = new ArrayList<>();
        for (PriceRecord r : records) {
            if (existingDates.contains(r.date())) continue;

            StockPriceHistory entity = new StockPriceHistory();
            entity.setTicker(ticker);
            entity.setTradeDate(r.date());
            entity.setOpenPrice(BigDecimal.valueOf(r.open()));
            entity.setHighPrice(BigDecimal.valueOf(r.high()));
            entity.setLowPrice(BigDecimal.valueOf(r.low()));
            entity.setClosePrice(BigDecimal.valueOf(r.close()));
            entity.setVolume(r.volume());
            newEntities.add(entity);
        }

        if (!newEntities.isEmpty()) {
            priceHistoryRepo.saveAll(newEntities);
        }
        return newEntities.size();
    }

    // ── Ticker Status ──

    private void updateTickerStatus(BatchTickerConfig config, String status, String errorMsg) {
        config.setLastRunAt(LocalDateTime.now());
        config.setLastRunStatus(status);
        config.setErrorMessage(errorMsg);
        tickerConfigRepo.save(config);
    }

    // ── Config Helpers ──

    public long getRateLimitMs() {
        return scheduleConfigRepo.findByConfigKey("rate_limit_ms")
                .map(c -> Long.parseLong(c.getConfigValue()))
                .orElse(3000L);
    }

    public String getCronExpression() {
        return scheduleConfigRepo.findByConfigKey("cron_expression")
                .map(c -> c.getConfigValue())
                .orElse("0 0 6 * * *");
    }

    public boolean isSchedulerEnabled() {
        return scheduleConfigRepo.findByConfigKey("scheduler_enabled")
                .map(c -> "true".equalsIgnoreCase(c.getConfigValue()))
                .orElse(false);
    }

    // ── Price Record ──

    public record PriceRecord(LocalDate date, double open, double high, double low, double close, long volume) {}
}
