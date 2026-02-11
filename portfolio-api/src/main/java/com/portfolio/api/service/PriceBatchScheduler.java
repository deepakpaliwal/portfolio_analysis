package com.portfolio.api.service;

import com.portfolio.api.model.BatchScheduleConfig;
import com.portfolio.api.repository.BatchScheduleConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Manages the scheduled batch price fetch job.
 * Supports dynamic cron expression changes and enable/disable via DB config.
 */
@Component
public class PriceBatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceBatchScheduler.class);

    private final TaskScheduler taskScheduler;
    private final PriceFetchBatchService batchService;
    private final BatchScheduleConfigRepository scheduleConfigRepo;

    private ScheduledFuture<?> scheduledTask;

    public PriceBatchScheduler(TaskScheduler taskScheduler,
                                PriceFetchBatchService batchService,
                                BatchScheduleConfigRepository scheduleConfigRepo) {
        this.taskScheduler = taskScheduler;
        this.batchService = batchService;
        this.scheduleConfigRepo = scheduleConfigRepo;
    }

    @PostConstruct
    public void init() {
        if (batchService.isSchedulerEnabled()) {
            startScheduler();
        } else {
            log.info("Batch price scheduler is disabled. Enable via schedule config.");
        }
    }

    public synchronized void startScheduler() {
        stopScheduler();
        String cron = batchService.getCronExpression();
        log.info("Starting batch price scheduler with cron: {}", cron);
        scheduledTask = taskScheduler.schedule(this::executeJob, new CronTrigger(cron));
    }

    public synchronized void stopScheduler() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
            log.info("Batch price scheduler stopped");
        }
        scheduledTask = null;
    }

    public boolean isRunning() {
        return scheduledTask != null && !scheduledTask.isCancelled();
    }

    /**
     * Update scheduler state based on DB config.
     * Call this after changing schedule config to apply changes.
     */
    public void refreshScheduler() {
        if (batchService.isSchedulerEnabled()) {
            startScheduler();
        } else {
            stopScheduler();
        }
    }

    /**
     * Update a schedule config value and refresh the scheduler.
     */
    public void updateConfig(String key, String value) {
        BatchScheduleConfig config = scheduleConfigRepo.findByConfigKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Unknown config key: " + key));
        config.setConfigValue(value);
        scheduleConfigRepo.save(config);
        refreshScheduler();
    }

    public Map<String, String> getScheduleConfig() {
        Map<String, String> config = new java.util.LinkedHashMap<>();
        scheduleConfigRepo.findAll().forEach(c -> config.put(c.getConfigKey(), c.getConfigValue()));
        config.put("scheduler_running", String.valueOf(isRunning()));
        return config;
    }

    private void executeJob() {
        log.info("Scheduled batch price fetch starting...");
        try {
            Map<String, Object> results = batchService.runBatchFetch();
            log.info("Scheduled batch price fetch complete: {}", results.get("_summary"));
        } catch (Exception e) {
            log.error("Scheduled batch price fetch failed: {}", e.getMessage(), e);
        }
    }
}
