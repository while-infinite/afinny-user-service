DO
$$
begin
ALTER TABLE client
RENAME COLUMN  id_customer TO id;
ALTER TABLE client
RENAME COLUMN  sur_name TO middle_name;
ALTER TABLE client
RENAME COLUMN  date_accession TO accession_date;
ALTER TABLE client
RENAME COLUMN identification_passport_number TO passport_number;
ALTER TABLE client
DROP CONSTRAINT if exists client_mobile_phone_fkey,
ADD if not exists employer_identification_number CHAR(11),--TODO:ask about the required length
ALTER COLUMN accession_date TYPE DATE;
EXCEPTION
        WHEN undefined_column THEN
END;
$$;

DO
$$
begin
ALTER TABLE passport_data
RENAME COLUMN identification_passport_number TO passport_number;
EXCEPTION
        WHEN undefined_column THEN
END;
$$;

DO
$$
begin
ALTER TABLE verification
RENAME COLUMN  mobile_phone TO receiver;
ALTER TABLE verification
RENAME COLUMN  sms_verification_code TO verification_code;
ALTER TABLE verification
RENAME COLUMN  sms_code_expiration TO code_expiration;
ALTER TABLE verification
ADD COLUMN if not exists type VARCHAR(30) NOT NULL,
ALTER COLUMN receiver TYPE varchar(30);
EXCEPTION
        WHEN undefined_column THEN
END;
$$;

DO
$$
begin
ALTER TABLE user_profile
RENAME COLUMN id_customer TO id;
ALTER TABLE user_profile
RENAME COLUMN  password_encoded TO password;
ALTER TABLE user_profile
RENAME COLUMN  date_app_registration TO app_registration_date;
EXCEPTION
        WHEN undefined_column THEN
END;
$$;

ALTER TABLE user_profile
DROP CONSTRAINT if exists  user_profile_id_customer_fkey,
DROP COLUMN if exists mobile_phone,
DROP COLUMN if exists client_status,
ALTER COLUMN app_registration_date TYPE DATE,
ADD COLUMN if not exists client_id UUID REFERENCES client(id);
