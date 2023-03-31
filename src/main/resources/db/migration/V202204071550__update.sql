ALTER TABLE user_profile
    ALTER COLUMN push_notification DROP  NOT NULL,
    ALTER COLUMN password DROP  NOT NULL,
    ALTER COLUMN email_subscription DROP NOT NULL,
    ALTER COLUMN app_registration_date DROP  NOT NULL;