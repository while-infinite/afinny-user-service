ALTER TABLE verification
    DROP COLUMN if exists block_expiration,
    ALTER COLUMN user_block_expiration DROP NOT NULL;

ALTER TABLE sms_block_sending
    ALTER COLUMN sms_block_expiration DROP NOT NULL;