--liquibase formatted sql

--changeset ksergey:003
ALTER TABLE credit_applications
    ADD COLUMN current_debt_load DOUBLE PRECISION,
    ADD COLUMN has_current_delinquency BOOLEAN;
