CREATE TABLE if not exists fingerprint
(
    id          UUID PRIMARY KEY,
    client_id   UUID        NOT NULL,
    fingerprint VARCHAR(32) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES client (id)
);