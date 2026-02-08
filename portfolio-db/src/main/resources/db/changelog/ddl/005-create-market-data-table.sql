--liquibase formatted sql

--changeset portfolio:005-create-market-data-table
--comment: Create the market_data table for storing real-time and historical price data
CREATE TABLE market_data (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticker      VARCHAR(50)   NOT NULL,
    asset_type  VARCHAR(30)   NOT NULL,
    price       DECIMAL(19,4) NOT NULL,
    open_price  DECIMAL(19,4),
    high_price  DECIMAL(19,4),
    low_price   DECIMAL(19,4),
    close_price DECIMAL(19,4),
    volume      BIGINT,
    timestamp   TIMESTAMP     NOT NULL,
    source      VARCHAR(100)
);

CREATE INDEX idx_market_data_ticker ON market_data(ticker);
CREATE INDEX idx_market_data_timestamp ON market_data(timestamp);
CREATE INDEX idx_market_data_ticker_ts ON market_data(ticker, timestamp);

--rollback DROP TABLE IF EXISTS market_data;
