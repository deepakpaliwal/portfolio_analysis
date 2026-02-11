--liquibase formatted sql

--changeset portfolio:016-create-batch-ticker-config-table
--comment: Create table for batch price fetch configuration - ticker list, watermarks, schedule
CREATE TABLE batch_ticker_config (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticker          VARCHAR(50)   NOT NULL UNIQUE,
    ticker_name     VARCHAR(200),
    enabled         BOOLEAN       NOT NULL DEFAULT TRUE,
    last_sync_date  DATE,
    record_count    BIGINT        NOT NULL DEFAULT 0,
    last_run_at     TIMESTAMP,
    last_run_status VARCHAR(20),
    error_message   VARCHAR(500),
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE batch_schedule_config (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    config_key      VARCHAR(100)  NOT NULL UNIQUE,
    config_value    VARCHAR(500)  NOT NULL,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Default schedule: daily at 6:00 AM
INSERT INTO batch_schedule_config (config_key, config_value) VALUES ('cron_expression', '0 0 6 * * *');
INSERT INTO batch_schedule_config (config_key, config_value) VALUES ('scheduler_enabled', 'false');
INSERT INTO batch_schedule_config (config_key, config_value) VALUES ('rate_limit_ms', '3000');

--rollback DROP TABLE IF EXISTS batch_schedule_config; DROP TABLE IF EXISTS batch_ticker_config;
