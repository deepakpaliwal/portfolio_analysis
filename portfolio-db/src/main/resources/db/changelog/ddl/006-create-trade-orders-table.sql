--liquibase formatted sql

--changeset portfolio:006-create-trade-orders-table
--comment: Create the trade_orders table for automated and manual trading
CREATE TABLE trade_orders (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    portfolio_id      BIGINT        NOT NULL,
    strategy_id       BIGINT,
    asset_type        VARCHAR(30)   NOT NULL,
    ticker            VARCHAR(50)   NOT NULL,
    action            VARCHAR(10)   NOT NULL,
    order_type        VARCHAR(20)   NOT NULL DEFAULT 'MARKET',
    quantity          DECIMAL(19,8) NOT NULL,
    limit_price       DECIMAL(19,4),
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    broker_order_id   VARCHAR(255),
    executed_price    DECIMAL(19,4),
    executed_at       TIMESTAMP,
    rationale         TEXT,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trade_orders_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    CONSTRAINT fk_trade_orders_strategy FOREIGN KEY (strategy_id) REFERENCES strategies(id)
);

CREATE INDEX idx_trade_orders_portfolio ON trade_orders(portfolio_id);
CREATE INDEX idx_trade_orders_status ON trade_orders(status);
CREATE INDEX idx_trade_orders_ticker ON trade_orders(ticker);

--rollback DROP TABLE IF EXISTS trade_orders;
