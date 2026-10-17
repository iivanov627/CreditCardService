--liquibase formatted sql

--changeset ksergey:001
CREATE TABLE clients
(
    id              UUID PRIMARY KEY,
    name            VARCHAR(255),
    surname         VARCHAR(255),
    middle_name     VARCHAR(255),
    birthdate       DATE         NOT NULL,
    phone_number    VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    series          VARCHAR(4)   NOT NULL,
    passport_number VARCHAR(6)   NOT NULL,
    create_date     DATE,
    CONSTRAINT uk_clients_series_passport_number UNIQUE (series, passport_number)
);
