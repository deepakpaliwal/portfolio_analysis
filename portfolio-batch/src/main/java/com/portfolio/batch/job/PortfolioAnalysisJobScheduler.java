package com.portfolio.batch.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Scheduler that triggers batch analysis jobs on a configurable schedule.
 *
 * <p>Supports daily, weekly, and on-demand execution of portfolio analysis jobs.</p>
 *
 * @author Portfolio Analysis Team
 * @version 1.0.0
 */
@Component
public class PortfolioAnalysisJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(PortfolioAnalysisJobScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job portfolioAnalysisJob;

    public PortfolioAnalysisJobScheduler(JobLauncher jobLauncher, Job portfolioAnalysisJob) {
        this.jobLauncher = jobLauncher;
        this.portfolioAnalysisJob = portfolioAnalysisJob;
    }

    /**
     * Runs portfolio analysis daily at 2:00 AM.
     */
    @Scheduled(cron = "${batch.schedule.portfolio-analysis:0 0 2 * * *}")
    public void runDailyAnalysis() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addDate("runDate", new Date())
                    .addString("type", "DAILY")
                    .toJobParameters();

            log.info("Starting daily portfolio analysis batch job");
            jobLauncher.run(portfolioAnalysisJob, params);
            log.info("Daily portfolio analysis batch job completed");
        } catch (Exception e) {
            log.error("Failed to run daily portfolio analysis job", e);
        }
    }
}
