ALTER TABLE client
    ALTER COLUMN employer_identification_number TYPE VARCHAR(30);
ALTER TABLE client
    ALTER COLUMN mobile_phone TYPE VARCHAR(11);
ALTER TABLE client
    ALTER COLUMN country_of_residence DROP DEFAULT,
    ALTER COLUMN country_of_residence TYPE boolean
    USING country_of_residence::boolean,
    ALTER COLUMN country_of_residence SET DEFAULT FALSE;

ALTER TABLE verification
    DROP COLUMN if exists receiver;
ALTER TABLE verification
    DROP COLUMN if exists type;
ALTER TABLE verification
    ADD if not exists  mobile_phone VARCHAR(11) NOT NULL PRIMARY KEY ;
ALTER TABLE verification
    ADD if not exists wrong_attempts_counter INTEGER NOT NULL;
ALTER TABLE verification
    ADD if not exists user_block_expiration TIMESTAMP NOT NULL;

CREATE TABLE if not exists sms_block_sending
(
    mobile_phone          UUID PRIMARY KEY,
    sending_count         INTEGER NOT NULL,
    sms_block_expiration  TIME    NOT NULL
);