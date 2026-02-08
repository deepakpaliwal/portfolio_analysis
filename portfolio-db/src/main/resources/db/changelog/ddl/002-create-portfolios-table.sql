--liquibase formatted sql

--changeset portfolio:002-create-portfolios-table
--comment: Create the portfolios table for portfolio management
CREATE TABLE portfolios (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    name            VARCHAR(200)  NOT NULL,
    description     VARCHAR(500),
    base_currency   VARCHAR(3)    NOT NULL DEFAULT 'USD',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_portfolios_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_portfolios_user_id ON portfolios(user_id);

--rollback DROP TABLE IF EXISTS portfolios;
