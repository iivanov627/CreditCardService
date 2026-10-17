--liquibase formatted sql

--changeset codex:003-h2
ALTER TABLE credit_applications ADD COLUMN current_debt_load DOUBLE PRECISION;
ALTER TABLE credit_applications ADD COLUMN has_current_delinquency BOOLEAN;
