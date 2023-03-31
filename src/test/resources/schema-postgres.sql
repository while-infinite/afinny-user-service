CREATE TABLE IF NOT EXISTS passport_data
(
    passport_number varchar(255) PRIMARY KEY,
    issuance_date   date,
    expiry_date     date,
    nationality     varchar(100),
    birth_date      date
    );

CREATE TABLE IF NOT EXISTS verification
(
    mobile_phone      varchar(11) PRIMARY KEY,
    verification_code char(6)        NOT NULL,
    code_expiration   timestamp      NOT NULL,
    wrong_attempts_counter INTEGER   NOT NULL,
    user_block_expiration TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS client
(
    id                             uuid PRIMARY KEY,
    first_name                     varchar(30)        NOT NULL,
    last_name                      varchar(30)        NOT NULL,
    middle_name                    varchar(30),
    country_of_residence           varchar(50)        NOT NULL,
    passport_number                varchar(255)       NOT NULL,
    accession_date                 date               NOT NULL,
    client_status                  varchar(50)        NOT NULL,
    mobile_phone                   varchar(11) UNIQUE NOT NULL,
    employer_identification_number CHAR(30),
    FOREIGN KEY (passport_number) REFERENCES passport_data (passport_number)
    );

CREATE TABLE IF NOT EXISTS user_profile
(
    id                    uuid PRIMARY KEY,
    sms_notification      boolean      NOT NULL,
    push_notification     boolean      NOT NULL,
    email_subscription    boolean      NOT NULL,
    password              varchar(255) NOT NULL,
    email                 varchar(50),
    security_question     varchar(50)  NOT NULL,
    security_answer       varchar(50)  NOT NULL,
    app_registration_date date         NOT NULL,
    client_id             UUID REFERENCES client (id)
    );

CREATE TABLE IF NOT EXISTS fingerprint
(
    id          UUID PRIMARY KEY,
    client_id   UUID        NOT NULL,
    fingerprint VARCHAR(32) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES client (id)
 );

CREATE TABLE IF NOT EXISTS sms_block_sending
(
    mobile_phone          varchar(11) PRIMARY KEY,
    sending_count         INTEGER NOT NULL,
    sms_block_expiration  TIME    NOT NULL
    );
