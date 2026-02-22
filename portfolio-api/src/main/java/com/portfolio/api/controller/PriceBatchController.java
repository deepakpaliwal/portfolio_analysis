package com.portfolio.api.controller;

import com.portfolio.api.model.BatchTickerConfig;
import com.portfolio.api.repository.BatchTickerConfigRepository;
import com.portfolio.api.service.PriceBatchScheduler;
import com.portfolio.api.service.PriceFetchBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch")
@Tag(name = "Price Batch", description = "Batch price fetch management — ticker CRUD, manual run, schedule config")
public class PriceBatchController {

    private final PriceFetchBatchService batchService;
    private final PriceBatchScheduler scheduler;
    private final BatchTickerConfigRepository tickerConfigRepo;

    public PriceBatchController(PriceFetchBatchService batchService,
                                PriceBatchScheduler scheduler,
                                BatchTickerConfigRepository tickerConfigRepo) {
        this.batchService = batchService;
        this.scheduler = scheduler;
        this.tickerConfigRepo = tickerConfigRepo;
    }

    @GetMapping("/tickers")
    @Operation(summary = "List all configured batch tickers")
    public ResponseEntity<List<BatchTickerConfig>> listTickers() {
        return ResponseEntity.ok(tickerConfigRepo.findAllByOrderByTickerAsc());
    }

    @PostMapping("/tickers")
    @Operation(summary = "Add a new ticker for batch price fetching")
    public ResponseEntity<BatchTickerConfig> addTicker(@RequestBody Map<String, String> body) {
        String ticker = body.getOrDefault("ticker", "").trim().toUpperCase();
        String tickerName = body.getOrDefault("tickerName", "").trim();
        String assetClass = body.getOrDefault("assetClass", "EQUITY").trim().toUpperCase();

        if (ticker.isEmpty()) return ResponseEntity.badRequest().build();
        if (tickerConfigRepo.existsByTicker(ticker)) return ResponseEntity.status(409).build();

        BatchTickerConfig config = new BatchTickerConfig();
        config.setTicker(ticker);
        config.setTickerName(tickerName.isEmpty() ? null : tickerName);
        config.setAssetClass(assetClass);
        config.setEnabled(true);

        if (body.get("optionContract") != null) config.setOptionContract(body.get("optionContract"));
        if (body.get("optionType") != null) config.setOptionType(body.get("optionType"));
        if (body.get("optionStrike") != null && !body.get("optionStrike").isBlank()) config.setOptionStrike(new BigDecimal(body.get("optionStrike")));
        if (body.get("optionExpiry") != null && !body.get("optionExpiry").isBlank()) config.setOptionExpiry(LocalDate.parse(body.get("optionExpiry")));

        return ResponseEntity.ok(tickerConfigRepo.save(config));
    }

    @PutMapping("/tickers/{id}")
    @Operation(summary = "Update a ticker config")
    public ResponseEntity<BatchTickerConfig> updateTicker(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BatchTickerConfig config = tickerConfigRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticker config not found: " + id));

        if (body.containsKey("tickerName")) config.setTickerName((String) body.get("tickerName"));
        if (body.containsKey("enabled")) config.setEnabled((Boolean) body.get("enabled"));
        if (body.containsKey("assetClass")) config.setAssetClass(String.valueOf(body.get("assetClass")).toUpperCase());

        return ResponseEntity.ok(tickerConfigRepo.save(config));
    }

    @DeleteMapping("/tickers/{id}")
    @Operation(summary = "Remove a ticker from batch config")
    public ResponseEntity<Void> deleteTicker(@PathVariable Long id) {
        tickerConfigRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/run")
    @Operation(summary = "Manually trigger batch price fetch for all enabled tickers")
    public ResponseEntity<Map<String, Object>> runBatch() {
        return ResponseEntity.ok(batchService.runBatchFetch());
    }

    @PostMapping("/run/{ticker}")
    @Operation(summary = "Manually trigger price fetch for a single ticker")
    public ResponseEntity<Map<String, Object>> runSingleTicker(@PathVariable String ticker) {
        int count = batchService.fetchSingleTicker(ticker.toUpperCase());
        return ResponseEntity.ok(Map.of("ticker", ticker.toUpperCase(), "newRecords", count));
    }

    @GetMapping("/monitoring")
    @Operation(summary = "Get batch monitoring summary and data health")
    public ResponseEntity<Map<String, Object>> monitoring() {
        return ResponseEntity.ok(batchService.getMonitoringSummary());
    }

    @GetMapping("/schedule")
    @Operation(summary = "Get current schedule configuration")
    public ResponseEntity<Map<String, String>> getScheduleConfig() {
        return ResponseEntity.ok(scheduler.getScheduleConfig());
    }

    @PutMapping("/schedule")
    @Operation(summary = "Update schedule configuration (cron_expression, scheduler_enabled, rate_limit_ms)")
    public ResponseEntity<Map<String, String>> updateScheduleConfig(@RequestBody Map<String, String> body) {
        for (Map.Entry<String, String> entry : body.entrySet()) {
            scheduler.updateConfig(entry.getKey(), entry.getValue());
        }
        return ResponseEntity.ok(scheduler.getScheduleConfig());
    }
}
