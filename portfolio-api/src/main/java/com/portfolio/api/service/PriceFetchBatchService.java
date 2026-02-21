package com.portfolio.api.service;

import com.portfolio.api.model.BatchTickerConfig;
import com.portfolio.api.model.MarketPriceHistory;
import com.portfolio.api.model.StockPriceHistory;
import com.portfolio.api.repository.BatchScheduleConfigRepository;
import com.portfolio.api.repository.BatchTickerConfigRepository;
import com.portfolio.api.repository.MarketPriceHistoryRepository;
import com.portfolio.api.repository.StockPriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private final MarketPriceHistoryRepository marketPriceHistoryRepository;
    private final RestTemplate restTemplate;

    public PriceFetchBatchService(BatchTickerConfigRepository tickerConfigRepo,
                                  BatchScheduleConfigRepository scheduleConfigRepo,
                                  StockPriceHistoryRepository priceHistoryRepo,
                                  MarketPriceHistoryRepository marketPriceHistoryRepository) {
        this.tickerConfigRepo = tickerConfigRepo;
        this.scheduleConfigRepo = scheduleConfigRepo;
        this.priceHistoryRepo = priceHistoryRepo;
        this.marketPriceHistoryRepository = marketPriceHistoryRepository;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> runBatchFetch() {
        List<BatchTickerConfig> tickers = tickerConfigRepo.findByEnabledTrueOrderByTickerAsc();
        if (tickers.isEmpty()) {
            return Map.of("status", "no_tickers", "message", "No enabled tickers configured");
        }

        long rateLimitMs = getRateLimitMs();
        Map<String, Object> results = new LinkedHashMap<>();
        int totalRecords = 0;
        int successCount = 0;
        int errorCount = 0;

        for (BatchTickerConfig config : tickers) {
            try {
                int count = fetchAndStoreTicker(config);
                results.put(config.getTicker(), Map.of(
                        "status", "ok",
                        "newRecords", count,
                        "assetClass", safeAssetClass(config)
                ));
                totalRecords += count;
                successCount++;
                Thread.sleep(rateLimitMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
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
        return results;
    }

    @Transactional
    public int fetchAndStoreTicker(BatchTickerConfig config) {
        String ticker = config.getTicker();
        LocalDate today = LocalDate.now();

        LocalDate fromDate = config.getLastSyncDate() != null
                ? config.getLastSyncDate().plusDays(1)
                : today.minusYears(LOOKBACK_YEARS);

        if (!fromDate.isBefore(today)) {
            updateTickerStatus(config, "OK", null);
            return 0;
        }

        long fromEpoch = fromDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long toEpoch = today.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC);

        List<PriceRecord> records = fetchFromYahoo(ticker, fromEpoch, toEpoch);
        if (records.isEmpty()) {
            updateTickerStatus(config, "OK", null);
            return 0;
        }

        writeToCsv(ticker, records);
        int dbCount = persistToDatabase(config, records);
        persistToUnifiedMarketHistory(config, records);

        LocalDate latestDate = records.stream().map(PriceRecord::date).max(LocalDate::compareTo).orElse(today);
        config.setLastSyncDate(latestDate);
        config.setRecordCount(config.getRecordCount() + dbCount);
        updateTickerStatus(config, "OK", null);

        return dbCount;
    }

    @Transactional
    public int fetchSingleTicker(String lTicker) {
        final String ticker = lTicker.toUpperCase();
        BatchTickerConfig config = tickerConfigRepo.findByTicker(ticker)
                .orElseGet(() -> {
                    BatchTickerConfig c = new BatchTickerConfig();
                    c.setTicker(ticker);
                    c.setEnabled(true);
                    c.setAssetClass("EQUITY");
                    return tickerConfigRepo.save(c);
                });
        return fetchAndStoreTicker(config);
    }

    public Map<String, Object> getMonitoringSummary() {
        Map<String, Long> assetClassCounts = new LinkedHashMap<>();
        for (Object[] row : tickerConfigRepo.countByAssetClass()) {
            assetClassCounts.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }

        Map<String, Object> statusCounts = new LinkedHashMap<>();
        statusCounts.put("OK", tickerConfigRepo.countByLastRunStatus("OK"));
        statusCounts.put("ERROR", tickerConfigRepo.countByLastRunStatus("ERROR"));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalConfiguredTickers", tickerConfigRepo.count());
        summary.put("enabledTickers", tickerConfigRepo.countByEnabledTrue());
        summary.put("disabledTickers", tickerConfigRepo.countByEnabledFalse());
        summary.put("totalBatchRecordCount", tickerConfigRepo.sumRecordCount());
        summary.put("totalMarketHistoryRecords", marketPriceHistoryRepository.countAllRecords());
        summary.put("lastBatchRunAt", tickerConfigRepo.findLastBatchRunAt());
        summary.put("latestTradeDate", marketPriceHistoryRepository.findLatestTradeDate());
        summary.put("assetClassCounts", assetClassCounts);
        summary.put("statusCounts", statusCounts);
        return summary;
    }

    @SuppressWarnings("unchecked")
    private List<PriceRecord> fetchFromYahoo(String ticker, long fromEpoch, long toEpoch) {
        String url = String.format("%s/%s?period1=%d&period2=%d&interval=1d", YAHOO_CHART_URL, ticker, fromEpoch, toEpoch);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Accept", "application/json");

        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> response = responseEntity.getBody();
        if (response == null || response.get("chart") == null) {
            throw new RuntimeException("Yahoo Finance returned null for " + ticker);
        }

        Map<String, Object> chart = (Map<String, Object>) response.get("chart");
        List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
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
            double close = closeVal.doubleValue();
            double open = (opens != null && i < opens.size() && opens.get(i) != null) ? opens.get(i).doubleValue() : close;
            double high = (highs != null && i < highs.size() && highs.get(i) != null) ? highs.get(i).doubleValue() : close;
            double low = (lows != null && i < lows.size() && lows.get(i) != null) ? lows.get(i).doubleValue() : close;
            long volume = (volumes != null && i < volumes.size() && volumes.get(i) != null) ? volumes.get(i).longValue() : 0L;
            records.add(new PriceRecord(tradeDate, open, high, low, close, volume));
        }

        return records;
    }

    private int writeToCsv(String ticker, List<PriceRecord> records) {
        try {
            Path dir = Paths.get(PRICES_DIR);
            Files.createDirectories(dir);
            Path csvFile = dir.resolve(ticker + ".csv");

            Set<LocalDate> existingDates = new HashSet<>();
            if (Files.exists(csvFile)) {
                List<String> lines = Files.readAllLines(csvFile);
                for (int i = 1; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (!line.isEmpty()) existingDates.add(LocalDate.parse(line.split(",")[0], DATE_FMT));
                }
            }

            List<PriceRecord> newRecords = records.stream()
                    .filter(r -> !existingDates.contains(r.date()))
                    .sorted(Comparator.comparing(PriceRecord::date))
                    .toList();

            if (newRecords.isEmpty()) return 0;

            List<String> lines = new ArrayList<>();
            if (!Files.exists(csvFile)) lines.add(CSV_HEADER);
            for (PriceRecord r : newRecords) {
                lines.add(String.format("%s,%.4f,%.4f,%.4f,%.4f,%d", r.date().format(DATE_FMT), r.open(), r.high(), r.low(), r.close(), r.volume()));
            }
            Files.write(csvFile, lines, Files.exists(csvFile) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
            return newRecords.size();
        } catch (IOException e) {
            log.error("Failed to write CSV for {}: {}", ticker, e.getMessage());
            return 0;
        }
    }


    public double[] readClosingPricesFromCsv(String ticker, LocalDate from, LocalDate to) {
        Path csvFile = Paths.get(PRICES_DIR, ticker + ".csv");
        if (!Files.exists(csvFile)) return new double[0];

        try {
            List<String> lines = Files.readAllLines(csvFile);
            List<Double> prices = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                LocalDate date = LocalDate.parse(parts[0], DATE_FMT);
                if (!date.isBefore(from) && !date.isAfter(to)) prices.add(Double.parseDouble(parts[4]));
            }
            return prices.stream().mapToDouble(Double::doubleValue).toArray();
        } catch (IOException e) {
            return new double[0];
        }
    }

    public boolean hasCsvData(String ticker) {
        return Files.exists(Paths.get(PRICES_DIR, ticker + ".csv"));
    }

    private int persistToDatabase(BatchTickerConfig config, List<PriceRecord> records) {
        String ticker = config.getTicker();
        String assetClass = safeAssetClass(config);
        if (!"EQUITY".equals(assetClass) && !"CRYPTO".equals(assetClass)) {
            return 0;
        }

        Set<LocalDate> existingDates = new HashSet<>();
        priceHistoryRepo.findByTickerOrderByTradeDateAsc(ticker).forEach(p -> existingDates.add(p.getTradeDate()));

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
        if (!newEntities.isEmpty()) priceHistoryRepo.saveAll(newEntities);
        return newEntities.size();
    }

    private int persistToUnifiedMarketHistory(BatchTickerConfig config, List<PriceRecord> records) {
        String ticker = config.getTicker();
        String assetClass = safeAssetClass(config);

        Set<LocalDate> existingDates = new HashSet<>();
        marketPriceHistoryRepository.findByTickerAndAssetClassOrderByTradeDateAsc(ticker, assetClass)
                .forEach(m -> existingDates.add(m.getTradeDate()));

        List<MarketPriceHistory> entities = new ArrayList<>();
        for (PriceRecord r : records) {
            if (existingDates.contains(r.date())) continue;
            MarketPriceHistory m = new MarketPriceHistory();
            m.setTicker(ticker);
            m.setAssetClass(assetClass);
            m.setTradeDate(r.date());
            m.setOpenPrice(BigDecimal.valueOf(r.open()));
            m.setHighPrice(BigDecimal.valueOf(r.high()));
            m.setLowPrice(BigDecimal.valueOf(r.low()));
            m.setClosePrice(BigDecimal.valueOf(r.close()));
            m.setVolume(r.volume());
            m.setOptionContract(config.getOptionContract());
            m.setOptionType(config.getOptionType());
            m.setOptionStrike(config.getOptionStrike());
            m.setOptionExpiry(config.getOptionExpiry());
            entities.add(m);
        }

        if (!entities.isEmpty()) marketPriceHistoryRepository.saveAll(entities);
        return entities.size();
    }

    private void updateTickerStatus(BatchTickerConfig config, String status, String errorMsg) {
        config.setLastRunAt(LocalDateTime.now());
        config.setLastRunStatus(status);
        config.setErrorMessage(errorMsg);
        tickerConfigRepo.save(config);
    }

    private String safeAssetClass(BatchTickerConfig config) {
        return (config.getAssetClass() == null || config.getAssetClass().isBlank()) ? "EQUITY" : config.getAssetClass().toUpperCase();
    }

    public long getRateLimitMs() {
        return scheduleConfigRepo.findByConfigKey("rate_limit_ms").map(c -> Long.parseLong(c.getConfigValue())).orElse(3000L);
    }

    public String getCronExpression() {
        return scheduleConfigRepo.findByConfigKey("cron_expression").map(c -> c.getConfigValue()).orElse("0 0 6 * * *");
    }

    public boolean isSchedulerEnabled() {
        return scheduleConfigRepo.findByConfigKey("scheduler_enabled").map(c -> "true".equalsIgnoreCase(c.getConfigValue())).orElse(false);
    }

    public record PriceRecord(LocalDate date, double open, double high, double low, double close, long volume) {}
}
