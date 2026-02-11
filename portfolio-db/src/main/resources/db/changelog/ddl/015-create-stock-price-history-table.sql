--liquibase formatted sql

--changeset portfolio:015-create-stock-price-history-table
--comment: Create table for storing historical daily stock prices (5-year lookback for risk analytics)
CREATE TABLE stock_price_history (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticker      VARCHAR(50)    NOT NULL,
    trade_date  DATE           NOT NULL,
    open_price  DECIMAL(19,4),
    high_price  DECIMAL(19,4),
    low_price   DECIMAL(19,4),
    close_price DECIMAL(19,4)  NOT NULL,
    volume      BIGINT,
    fetched_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stock_price_ticker_date UNIQUE (ticker, trade_date)
);

CREATE INDEX idx_stock_price_ticker ON stock_price_history(ticker);
CREATE INDEX idx_stock_price_date ON stock_price_history(trade_date);
CREATE INDEX idx_stock_price_ticker_date ON stock_price_history(ticker, trade_date);

--rollback DROP TABLE IF EXISTS stock_price_history;
