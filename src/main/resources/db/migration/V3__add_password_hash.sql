-- Add password hash column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Temporary default for existing seeded users (will be overwritten on next seed)
UPDATE users SET password_hash = '$2a$10$placeholder' WHERE password_hash IS NULL;

-- Now make it not-null
ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;