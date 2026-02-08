--liquibase formatted sql

--changeset portfolio:004-create-transactions-table
--comment: Create the transactions table for recording buys, sells, dividends, and splits
CREATE TABLE transactions (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    holding_id        BIGINT        NOT NULL,
    transaction_type  VARCHAR(20)   NOT NULL,
    quantity          DECIMAL(19,8) NOT NULL,
    price             DECIMAL(19,4) NOT NULL,
    fees              DECIMAL(19,4) DEFAULT 0,
    executed_at       TIMESTAMP     NOT NULL,
    notes             VARCHAR(500),
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_holding FOREIGN KEY (holding_id) REFERENCES holdings(id) ON DELETE CASCADE
);

CREATE INDEX idx_transactions_holding_id ON transactions(holding_id);
CREATE INDEX idx_transactions_executed_at ON transactions(executed_at);

--rollback DROP TABLE IF EXISTS transactions;
