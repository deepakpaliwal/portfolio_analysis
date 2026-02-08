--liquibase formatted sql

--changeset portfolio:011-create-batch-jobs-table
--comment: Create the batch_jobs table for tracking batch processing job executions
CREATE TABLE batch_jobs (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_name            VARCHAR(200)  NOT NULL,
    job_type            VARCHAR(50)   NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    parameters          TEXT,
    started_at          TIMESTAMP,
    completed_at        TIMESTAMP,
    records_processed   BIGINT        DEFAULT 0,
    error_message       TEXT,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_batch_jobs_status ON batch_jobs(status);
CREATE INDEX idx_batch_jobs_type ON batch_jobs(job_type);

--rollback DROP TABLE IF EXISTS batch_jobs;
