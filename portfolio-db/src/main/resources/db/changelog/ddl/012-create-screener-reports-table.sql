--liquibase formatted sql

--changeset portfolio:012-create-screener-reports-table
--comment: Create the screener_reports table for saved stock and sector screening results
CREATE TABLE screener_reports (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    report_type     VARCHAR(20)   NOT NULL,
    target          VARCHAR(100)  NOT NULL,
    report_data     TEXT          NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_screener_reports_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_screener_reports_user_id ON screener_reports(user_id);
CREATE INDEX idx_screener_reports_target ON screener_reports(target);

--rollback DROP TABLE IF EXISTS screener_reports;
