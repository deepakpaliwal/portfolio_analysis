--liquibase formatted sql

--changeset portfolio:003-create-holdings-table
--comment: Create the holdings table for individual asset positions within portfolios
CREATE TABLE holdings (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    portfolio_id    BIGINT        NOT NULL,
    asset_type      VARCHAR(30)   NOT NULL,
    ticker          VARCHAR(50)   NOT NULL,
    name            VARCHAR(200),
    quantity        DECIMAL(19,8) NOT NULL,
    purchase_price  DECIMAL(19,4) NOT NULL,
    purchase_date   DATE          NOT NULL,
    currency        VARCHAR(3)    NOT NULL DEFAULT 'USD',
    sector          VARCHAR(100),
    category        VARCHAR(100),
    metadata        TEXT,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_holdings_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
);

CREATE INDEX idx_holdings_portfolio_id ON holdings(portfolio_id);
CREATE INDEX idx_holdings_ticker ON holdings(ticker);
CREATE INDEX idx_holdings_asset_type ON holdings(asset_type);

--rollback DROP TABLE IF EXISTS holdings;
