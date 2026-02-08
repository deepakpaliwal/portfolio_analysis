--liquibase formatted sql

--changeset portfolio:007-create-strategies-table
--comment: Create the strategies table for investment and trading strategies
CREATE TABLE strategies (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(200)  NOT NULL,
    type            VARCHAR(50)   NOT NULL,
    parameters      TEXT,
    description     TEXT,
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--rollback DROP TABLE IF EXISTS strategies;
