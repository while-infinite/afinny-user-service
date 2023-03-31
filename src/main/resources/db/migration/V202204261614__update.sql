ALTER TABLE verification
    ADD COLUMN if not exists block_expiration TIMESTAMP;