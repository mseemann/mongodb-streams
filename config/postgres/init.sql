DROP SCHEMA IF EXISTS lbl CASCADE;

CREATE SCHEMA lbl;

CREATE TABLE lbl.tbl_labels
(
    id  varchar primary key,
    labelcode  varchar NOT NULL,
    short_name varchar
);

CREATE TABLE lbl.tbl_labels_bookmark
(
    collection varchar primary key,
    token  varchar NOT NULL
);

