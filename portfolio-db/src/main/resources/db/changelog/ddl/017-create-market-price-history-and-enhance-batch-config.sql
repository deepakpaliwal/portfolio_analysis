--liquibase formatted sql

--changeset portfolio:017-create-market-price-history-and-enhance-batch-config
--comment: Add unified market history table (equity/crypto/option) and enrich batch ticker config metadata

CREATE TABLE market_price_history (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticker              VARCHAR(50)   NOT NULL,
    asset_class         VARCHAR(20)   NOT NULL DEFAULT 'EQUITY',
    trade_date          DATE          NOT NULL,
    open_price          DECIMAL(19,4),
    high_price          DECIMAL(19,4),
    low_price           DECIMAL(19,4),
    close_price         DECIMAL(19,4) NOT NULL,
    volume              BIGINT,
    option_contract     VARCHAR(120),
    option_type         VARCHAR(10),
    option_strike       DECIMAL(19,4),
    option_expiry       DATE,
    fetched_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_market_price_history UNIQUE (ticker, asset_class, trade_date, option_contract)
);

CREATE INDEX idx_market_price_ticker ON market_price_history(ticker);
CREATE INDEX idx_market_price_asset_date ON market_price_history(asset_class, trade_date);

ALTER TABLE batch_ticker_config ADD COLUMN asset_class VARCHAR(20) NOT NULL DEFAULT 'EQUITY';
ALTER TABLE batch_ticker_config ADD COLUMN market_source VARCHAR(50) NOT NULL DEFAULT 'YAHOO';
ALTER TABLE batch_ticker_config ADD COLUMN option_contract VARCHAR(120);
ALTER TABLE batch_ticker_config ADD COLUMN option_type VARCHAR(10);
ALTER TABLE batch_ticker_config ADD COLUMN option_strike DECIMAL(19,4);
ALTER TABLE batch_ticker_config ADD COLUMN option_expiry DATE;

CREATE INDEX idx_batch_ticker_asset_class ON batch_ticker_config(asset_class);

--rollback DROP INDEX IF EXISTS idx_batch_ticker_asset_class; ALTER TABLE batch_ticker_config DROP COLUMN IF EXISTS option_expiry; ALTER TABLE batch_ticker_config DROP COLUMN IF EXISTS option_strike; ALTER TABLE batch_ticker_config DROP COLUMN IF EXISTS option_type; ALTER TABLE batch_ticker_config DROP COLUMN IF EXISTS option_contract; ALTER TABLE batch_ticker_config DROP COLUMN IF EXISTS market_source; ALTER TABLE batch_ticker_config DROP COLUMN IF EXISTS asset_class; DROP TABLE IF EXISTS market_price_history;
