-- Expand reset_code column to 255 to store secure BCrypt hashes instead of plaintext codes
ALTER TABLE admins ALTER COLUMN reset_code TYPE VARCHAR(255);

-- Invalidate any existing plaintext reset codes so no legacy unhashed codes remain
UPDATE admins SET reset_code = NULL, reset_code_expires_at = NULL WHERE reset_code IS NOT NULL;
