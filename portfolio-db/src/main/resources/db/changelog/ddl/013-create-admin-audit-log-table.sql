--liquibase formatted sql

--changeset portfolio:013-create-admin-audit-log-table
--comment: Create the admin_audit_log table for tracking administrative actions
CREATE TABLE admin_audit_log (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_user_id   BIGINT        NOT NULL,
    action          VARCHAR(100)  NOT NULL,
    target_entity   VARCHAR(100),
    target_id       VARCHAR(255),
    details         TEXT,
    timestamp       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_log_admin FOREIGN KEY (admin_user_id) REFERENCES users(id)
);

CREATE INDEX idx_audit_log_admin ON admin_audit_log(admin_user_id);
CREATE INDEX idx_audit_log_action ON admin_audit_log(action);
CREATE INDEX idx_audit_log_timestamp ON admin_audit_log(timestamp);

--rollback DROP TABLE IF EXISTS admin_audit_log;
