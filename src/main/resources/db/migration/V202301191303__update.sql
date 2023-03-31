ALTER TABLE client
ALTER COLUMN mobile_phone TYPE VARCHAR(12);
ALTER TABLE sms_block_sending
ALTER COLUMN mobile_phone TYPE VARCHAR(12);
ALTER TABLE verification
ALTER COLUMN mobile_phone TYPE VARCHAR(12);