ALTER TABLE user_profile
	ALTER COLUMN email DROP NOT NULL,
	ADD COLUMN if not exists email_subscription BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE passport_data
	ALTER COLUMN issuance_date DROP  NOT NULL,
	ALTER COLUMN expiry_date DROP  NOT NULL,
	ALTER COLUMN nationality DROP  NOT NULL,
	ALTER COLUMN birth_date DROP  NOT NULL;

