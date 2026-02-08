--liquibase formatted sql

--changeset portfolio:009-create-subscriptions-table
--comment: Create the subscriptions table for freemium billing and plan management
CREATE TABLE subscriptions (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             BIGINT        NOT NULL,
    plan                VARCHAR(20)   NOT NULL DEFAULT 'FREE',
    status              VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    billing_cycle       VARCHAR(20)   DEFAULT 'MONTHLY',
    start_date          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date            TIMESTAMP,
    payment_provider    VARCHAR(50),
    external_sub_id     VARCHAR(255),
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);

--rollback DROP TABLE IF EXISTS subscriptions;
