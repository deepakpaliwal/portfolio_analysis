--liquibase formatted sql

--changeset portfolio:001-create-users-table
--comment: Create the users table for user management
CREATE TABLE users (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email           VARCHAR(255)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    first_name      VARCHAR(100)  NOT NULL,
    last_name       VARCHAR(100)  NOT NULL,
    role            VARCHAR(20)   NOT NULL DEFAULT 'VIEWER',
    mfa_secret      VARCHAR(255),
    mfa_enabled     BOOLEAN       NOT NULL DEFAULT FALSE,
    email_verified  BOOLEAN       NOT NULL DEFAULT FALSE,
    auth_provider   VARCHAR(20)   NOT NULL DEFAULT 'LOCAL',
    provider_id     VARCHAR(255),
    failed_login_attempts INT     NOT NULL DEFAULT 0,
    locked_until    TIMESTAMP,
    subscription_tier VARCHAR(20) NOT NULL DEFAULT 'FREE',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_auth_provider ON users(auth_provider, provider_id);

--rollback DROP TABLE IF EXISTS users;
