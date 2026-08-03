ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verification_token VARCHAR(64) NULL,
    ADD COLUMN email_verification_expires_at TIMESTAMP(6) NULL,
    ADD COLUMN password_reset_token VARCHAR(64) NULL,
    ADD COLUMN password_reset_expires_at TIMESTAMP(6) NULL;

-- Existing accounts were created before email verification.
UPDATE users SET email_verified = TRUE;

CREATE INDEX ix_users_email_verification_token ON users (email_verification_token);
CREATE INDEX ix_users_password_reset_token ON users (password_reset_token);
