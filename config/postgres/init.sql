DROP SCHEMA IF EXISTS company CASCADE;

CREATE SCHEMA company;

CREATE TABLE company.user
(
    id  varchar primary key,
    user_name  varchar NOT NULL
);

CREATE TABLE company.user_bookmark
(
    collection varchar primary key,
    token  varchar NOT NULL,
    token_time_stamp numeric
);

