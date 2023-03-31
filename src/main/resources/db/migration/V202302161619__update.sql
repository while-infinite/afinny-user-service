ALTER TABLE client
    DROP CONSTRAINT IF EXISTS passport_number_unique;
ALTER TABLE client
    ADD CONSTRAINT passport_number_unique UNIQUE (passport_number);