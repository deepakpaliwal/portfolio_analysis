--liquibase formatted sql

--changeset portfolio:008-create-broker-accounts-table
--comment: Create the broker_accounts table for linked brokerage and exchange accounts
CREATE TABLE broker_accounts (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             BIGINT        NOT NULL,
    broker_name         VARCHAR(100)  NOT NULL,
    api_key_encrypted   TEXT,
    api_secret_encrypted TEXT,
    account_type        VARCHAR(30)   NOT NULL DEFAULT 'BROKERAGE',
    is_paper_trading    BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_broker_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_broker_accounts_user_id ON broker_accounts(user_id);

--rollback DROP TABLE IF EXISTS broker_accounts;
