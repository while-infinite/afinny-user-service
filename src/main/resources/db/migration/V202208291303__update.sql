ALTER TABLE sms_block_sending
    ALTER COLUMN sms_block_expiration TYPE TIMESTAMP without time zone using current_date