-- Migration: Fix tbl_chatlogs schema
-- Purpose: Fix column type issues in tbl_chatlogs table
-- Date: 2024-12-07

-- Drop the table if it exists and recreate it with correct schema
DROP TABLE IF EXISTS tbl_chatlogs CASCADE;

-- Create tbl_chatlogs with correct schema
CREATE TABLE tbl_chatlogs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT,
    response TEXT,
    timestamp TIMESTAMP,
    CONSTRAINT fk_chatlogs_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE
);

-- Create index for better query performance
CREATE INDEX idx_chatlogs_user_id ON tbl_chatlogs(user_id);
CREATE INDEX idx_chatlogs_timestamp ON tbl_chatlogs(timestamp);

-- Add comments
COMMENT ON TABLE tbl_chatlogs IS 'Stores chatbot conversation logs';
COMMENT ON COLUMN tbl_chatlogs.id IS 'Primary key';
COMMENT ON COLUMN tbl_chatlogs.user_id IS 'Reference to tbl_users';
COMMENT ON COLUMN tbl_chatlogs.message IS 'User message';
COMMENT ON COLUMN tbl_chatlogs.response IS 'Bot response';
COMMENT ON COLUMN tbl_chatlogs.timestamp IS 'Conversation timestamp';
