--liquibase formatted sql

--changeset portfolio:014-create-feature-toggles-table
--comment: Create the feature_toggles table for runtime feature flag management
CREATE TABLE feature_toggles (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    feature_key     VARCHAR(100)  NOT NULL UNIQUE,
    is_enabled      BOOLEAN       NOT NULL DEFAULT FALSE,
    allowed_tiers   VARCHAR(100)  DEFAULT 'FREE,PRO,PREMIUM',
    updated_by      BIGINT,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feature_toggles_user FOREIGN KEY (updated_by) REFERENCES users(id)
);

CREATE UNIQUE INDEX idx_feature_toggles_key ON feature_toggles(feature_key);

--rollback DROP TABLE IF EXISTS feature_toggles;
