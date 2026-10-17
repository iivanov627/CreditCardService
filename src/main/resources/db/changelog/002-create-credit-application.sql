--liquibase formatted sql

--changeset ksergey:002
CREATE TABLE credit_applications
(
    id                        UUID PRIMARY KEY,
    client_id                 UUID         NOT NULL,
    requested_credit_limit    NUMERIC(15, 2),
    monthly_income            NUMERIC(15, 2),
    place_of_work             VARCHAR(255),
    current_job_tenure_months INTEGER,
    client_comment            VARCHAR(255),
    application_status        VARCHAR(255) NOT NULL,
    create_date               DATE         NOT NULL,
    last_change               TIMESTAMP,
    submitted_at              TIMESTAMP,
    CONSTRAINT fk_credit_applications_client
        FOREIGN KEY (client_id) REFERENCES clients (id)
);
