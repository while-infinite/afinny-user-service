CREATE TABLE IF NOT EXISTS passport_data
(
    identification_passport_number VARCHAR(255) PRIMARY KEY,
    issuance_date                  DATE         NOT NULL,
    expiry_date                    DATE,
    nationality                    VARCHAR(100) NOT NULL,
    birth_date                     DATE         NOT NULL
);

CREATE TABLE IF NOT EXISTS verification
(
    mobile_phone          VARCHAR(12) PRIMARY KEY,
    sms_verification_code CHAR(6),
    sms_code_expiration   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS client
(
    id_customer                    UUID PRIMARY KEY,
    first_name                     VARCHAR(30)              NOT NULL,
    last_name                      VARCHAR(30)              NOT NULL,
    sur_name                       VARCHAR(30),
    country_of_residence           VARCHAR(50)              NOT NULL,
    identification_passport_number VARCHAR(255)             NOT NULL,
    date_accession                 TIMESTAMP WITH TIME ZONE NOT NULL,
    client_status                  VARCHAR(50)              NOT NULL,
    mobile_phone                   VARCHAR(12) UNIQUE       NOT NULL,
    FOREIGN KEY (identification_passport_number) REFERENCES passport_data (identification_passport_number),
    FOREIGN KEY (mobile_phone) REFERENCES verification (mobile_phone)
);

CREATE TABLE IF NOT EXISTS user_profile
(
    id_customer           UUID PRIMARY KEY,
    sms_notification      BOOLEAN                  NOT NULL,
    push_notification     BOOLEAN                  NOT NULL,
    password_encoded      VARCHAR(255)             NOT NULL,
    email                 VARCHAR(50),
    security_question     VARCHAR(50)              NOT NULL,
    security_answer       VARCHAR(50)              NOT NULL,
    mobile_phone          VARCHAR(12) UNIQUE       NOT NULL,
    client_status         VARCHAR(50)              NOT NULL,
    date_app_registration TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (id_customer) REFERENCES client (id_customer)
);




