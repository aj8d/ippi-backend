-- Add custom_id and system_id columns to users table
ALTER TABLE users 
ADD COLUMN custom_id VARCHAR(50) UNIQUE,
ADD COLUMN system_id VARCHAR(50) NOT NULL UNIQUE DEFAULT '';

-- Update existing users with system IDs (if any exist)
-- This is a simple approach; in production you might want to use a procedure
UPDATE users SET system_id = SUBSTRING(MD5(RAND()) FROM 1 FOR 16) WHERE system_id = '';
