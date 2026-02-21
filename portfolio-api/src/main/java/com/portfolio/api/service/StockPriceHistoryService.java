package com.portfolio.api.service;

import com.portfolio.api.model.Holding;
import com.portfolio.api.model.Portfolio;
import com.portfolio.api.model.StockPriceHistory;
import com.portfolio.api.repository.PortfolioRepository;
import com.portfolio.api.repository.StockPriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Service to fetch and persist historical daily stock prices (5-year lookback).
 * Data is sourced from Finnhub /stock/candle endpoint and stored in stock_price_history table.
 */
@Service
public class StockPriceHistoryService {

    private static final Logger log = LoggerFactory.getLogger(StockPriceHistoryService.class);
    private static final String BENCHMARK_TICKER = "SPY";
    private static final int LOOKBACK_YEARS = 5;
    private static final long FINNHUB_RATE_LIMIT_MS = 1100; // ~1 req/sec to avoid rate limits

    private final StockPriceHistoryRepository priceHistoryRepository;
    private final PortfolioRepository portfolioRepository;
    private final MarketDataService marketDataService;
    private final PriceFetchBatchService priceFetchBatchService;

    public StockPriceHistoryService(StockPriceHistoryRepository priceHistoryRepository,
                                     PortfolioRepository portfolioRepository,
                                     MarketDataService marketDataService,
                                     PriceFetchBatchService priceFetchBatchService) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.portfolioRepository = portfolioRepository;
        this.marketDataService = marketDataService;
        this.priceFetchBatchService = priceFetchBatchService;
    }

    /**
     * Sync 5 years of daily price history for all tickers in a portfolio + SPY benchmark.
     * Returns a summary map with sync results per ticker.
     */
    @Transactional
    public Map<String, Object> syncPortfolioPriceHistory(Long portfolioId, String username) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + portfolioId));

        if (!portfolio.getUser().getEmail().equals(username)) {
            throw new SecurityException("Access denied");
        }

        // Collect unique tickers from holdings (STOCK and ETF types that have tradeable tickers)
        Set<String> tickers = new LinkedHashSet<>();
        for (Holding h : portfolio.getHoldings()) {
            if (h.getTicker() != null && !h.getTicker().isBlank()
                    && isTradeableTicker(h)) {
                tickers.add(h.getTicker().toUpperCase());
            }
        }

        // Always include SPY for benchmark calculations
        tickers.add(BENCHMARK_TICKER);

        log.info("Syncing price history for {} tickers: {}", tickers.size(), tickers);

        Map<String, Object> results = new LinkedHashMap<>();
        int totalSaved = 0;

        for (String ticker : tickers) {
            try {
                int count = syncTickerHistory(ticker);
                results.put(ticker, Map.of("status", "ok", "records", count));
                totalSaved += count;
                log.info("Synced {} price records for {}", count, ticker);

                // Rate limit to avoid Finnhub 429s
                Thread.sleep(FINNHUB_RATE_LIMIT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Failed to sync price history for {}: {}", ticker, e.getMessage());
                results.put(ticker, Map.of("status", "error", "message", e.getMessage()));
            }
        }

        results.put("_summary", Map.of("totalTickers", tickers.size(), "totalRecords", totalSaved));
        return results;
    }

    /**
     * Sync a single ticker's 5-year price history.
     * Performs upsert — existing dates are skipped, new dates are inserted.
     */
    @Transactional
    public int syncTickerHistory(String ticker) {
        String symbol = ticker == null ? "" : ticker.toUpperCase().trim();
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Ticker is required");
        }

        LocalDate today = LocalDate.now();

        LocalDate fromDate = priceHistoryRepository.findTopByTickerOrderByTradeDateDesc(symbol)
                .map(StockPriceHistory::getTradeDate)
                .map(d -> d.plusDays(1))
                .orElse(today.minusYears(LOOKBACK_YEARS));

        // Delta pull no-op: if already synced through today, skip API call entirely.
        if (!fromDate.isBefore(today)) {
            log.info("Ticker {} is already up to date in stock_price_history (fromDate={})", symbol, fromDate);
            return 0;
        }

        long fromEpoch = fromDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
        long toEpoch = today.atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC);

        Map<String, Object> candles = marketDataService.getStockCandles(symbol, "D", fromEpoch, toEpoch);

        if (candles == null || !"ok".equals(candles.get("s"))) {
            log.info("Primary candle source unavailable for {} (status={}), falling back to batch fetch", symbol,
                    candles == null ? "null" : candles.get("s"));
            int fallbackCount = priceFetchBatchService.fetchSingleTicker(symbol);
            log.info("Fallback batch fetch completed for {} with {} new records", symbol, fallbackCount);
            return fallbackCount;
        }

        @SuppressWarnings("unchecked")
        List<Number> closes = (List<Number>) candles.get("c");
        @SuppressWarnings("unchecked")
        List<Number> opens = (List<Number>) candles.get("o");
        @SuppressWarnings("unchecked")
        List<Number> highs = (List<Number>) candles.get("h");
        @SuppressWarnings("unchecked")
        List<Number> lows = (List<Number>) candles.get("l");
        @SuppressWarnings("unchecked")
        List<Number> volumes = (List<Number>) candles.get("v");
        @SuppressWarnings("unchecked")
        List<Number> timestamps = (List<Number>) candles.get("t");

        if (closes == null || timestamps == null || closes.size() != timestamps.size()) {
            throw new RuntimeException("Invalid candle data structure for " + symbol);
        }

        // Load existing dates for this ticker to avoid duplicate inserts
        Set<LocalDate> existingDates = new HashSet<>();
        priceHistoryRepository.findByTickerOrderByTradeDateAsc(symbol)
                .forEach(p -> existingDates.add(p.getTradeDate()));

        List<StockPriceHistory> newRecords = new ArrayList<>();
        for (int i = 0; i < closes.size(); i++) {
            long epochSec = timestamps.get(i).longValue();
            LocalDate tradeDate = LocalDate.ofInstant(Instant.ofEpochSecond(epochSec), ZoneOffset.UTC);

            if (existingDates.contains(tradeDate)) {
                continue; // skip duplicates
            }

            StockPriceHistory record = new StockPriceHistory();
            record.setTicker(symbol);
            record.setTradeDate(tradeDate);
            record.setClosePrice(BigDecimal.valueOf(closes.get(i).doubleValue()));

            if (opens != null && i < opens.size()) {
                record.setOpenPrice(BigDecimal.valueOf(opens.get(i).doubleValue()));
            }
            if (highs != null && i < highs.size()) {
                record.setHighPrice(BigDecimal.valueOf(highs.get(i).doubleValue()));
            }
            if (lows != null && i < lows.size()) {
                record.setLowPrice(BigDecimal.valueOf(lows.get(i).doubleValue()));
            }
            if (volumes != null && i < volumes.size()) {
                record.setVolume(volumes.get(i).longValue());
            }

            newRecords.add(record);
        }

        if (!newRecords.isEmpty()) {
            priceHistoryRepository.saveAll(newRecords);
        }

        return newRecords.size();
    }

    /**
     * Get closing prices for a ticker within a date range.
     * Tries local DB first, falls back to CSV files from batch module.
     */
    public double[] getClosingPrices(String ticker, LocalDate from, LocalDate to) {
        List<StockPriceHistory> records = priceHistoryRepository
                .findByTickerAndTradeDateBetweenOrderByTradeDateAsc(ticker, from, to);

        if (!records.isEmpty()) {
            return records.stream()
                    .mapToDouble(r -> r.getClosePrice().doubleValue())
                    .toArray();
        }

        // Fallback: try CSV files from batch price fetch
        log.info("No DB records for {}, trying CSV fallback", ticker);
        return priceFetchBatchService.readClosingPricesFromCsv(ticker, from, to);
    }

    /**
     * Get trade dates for a ticker within a date range from local DB.
     */
    public List<LocalDate> getTradeDates(String ticker, LocalDate from, LocalDate to) {
        return priceHistoryRepository
                .findByTickerAndTradeDateBetweenOrderByTradeDateAsc(ticker, from, to)
                .stream()
                .map(StockPriceHistory::getTradeDate)
                .toList();
    }

    /**
     * Get the latest close price for a ticker from local DB.
     */
    public BigDecimal getLatestClosePrice(String ticker) {
        return priceHistoryRepository.findTopByTickerOrderByTradeDateDesc(ticker)
                .map(StockPriceHistory::getClosePrice)
                .orElse(null);
    }

    /**
     * Check how many records exist for a ticker.
     */
    public long getRecordCount(String ticker) {
        return priceHistoryRepository.countByTicker(ticker);
    }

    /**
     * Get all tickers that have stored price history.
     */
    public List<String> getStoredTickers() {
        return priceHistoryRepository.findDistinctTickers();
    }

    private boolean isTradeableTicker(Holding h) {
        String type = h.getAssetType() != null ? h.getAssetType().name() : "";
        // Only sync tickers that are actually traded on Finnhub-supported exchanges
        return "STOCK".equals(type) || "ETF".equals(type);
    }
}
