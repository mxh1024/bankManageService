CREATE TABLE IF NOT EXISTS bank_account (
    id BIGINT NOT NULL PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    account_holder_name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(255) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    create_time BIGINT NOT NULL);