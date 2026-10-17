--liquibase formatted sql

--changeset ksergey:004
CREATE TABLE scoring_result
(
    id                    UUID PRIMARY KEY,
    credit_application_id UUID           NOT NULL,
    score                 INTEGER        NOT NULL,
    decision              VARCHAR(32)    NOT NULL,
    recommended_limit     NUMERIC(15, 2) NOT NULL,
    CONSTRAINT uk_scoring_result_application UNIQUE (credit_application_id),
    CONSTRAINT fk_scoring_result_application
        FOREIGN KEY (credit_application_id) REFERENCES credit_applications (id)
);

CREATE TABLE snapshot_data
(
    id                              UUID PRIMARY KEY,
    scoring_result_id               UUID           NOT NULL,
    snapshot_age                    INTEGER        NOT NULL,
    snapshot_monthly_income         NUMERIC(15, 2) NOT NULL,
    snapshot_job_tenure_months      INTEGER        NOT NULL,
    snapshot_current_debt_load      DOUBLE PRECISION,
    snapshot_has_current_delinquency BOOLEAN,
    snapshot_requested_credit_limit NUMERIC(15, 2) NOT NULL,
    created_at                      TIMESTAMP      NOT NULL,
    CONSTRAINT uk_snapshot_data_scoring_result UNIQUE (scoring_result_id),
    CONSTRAINT fk_snapshot_data_scoring_result
        FOREIGN KEY (scoring_result_id) REFERENCES scoring_result (id) ON DELETE CASCADE
);

CREATE TABLE scoring_result_reason_codes
(
    scoring_result_id UUID        NOT NULL,
    reason_code       VARCHAR(64) NOT NULL,
    CONSTRAINT fk_scoring_reason_codes_result
        FOREIGN KEY (scoring_result_id) REFERENCES scoring_result (id) ON DELETE CASCADE
);
