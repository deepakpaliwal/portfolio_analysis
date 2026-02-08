--liquibase formatted sql

--changeset portfolio:010-create-alert-rules-table
--comment: Create the alert_rules table for user notification preferences and triggers
CREATE TABLE alert_rules (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             BIGINT        NOT NULL,
    alert_type          VARCHAR(50)   NOT NULL,
    channel             VARCHAR(20)   NOT NULL DEFAULT 'EMAIL',
    condition_config    TEXT          NOT NULL,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    last_triggered_at   TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alert_rules_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_alert_rules_user_id ON alert_rules(user_id);
CREATE INDEX idx_alert_rules_type ON alert_rules(alert_type);

--rollback DROP TABLE IF EXISTS alert_rules;
