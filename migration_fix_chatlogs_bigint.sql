-- Migration: Fix ChatLogs Schema - Use UUID for user_id
-- Date: 2025-12-09
-- Description: Fix the tbl_chatlogs table schema to use UUID for id and user_id to match tbl_users

-- Drop the old table if it exists
DROP TABLE IF EXISTS tbl_chatlogs CASCADE;

-- Create the new table with correct schema (UUID for both id and user_id)
CREATE TABLE tbl_chatlogs (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    message TEXT,
    response TEXT,
    timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_chatlogs_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_chatlogs_user_id ON tbl_chatlogs(user_id);
CREATE INDEX idx_chatlogs_timestamp ON tbl_chatlogs(timestamp DESC);

-- Add comments
COMMENT ON TABLE tbl_chatlogs IS 'Stores chatbot conversation logs';
COMMENT ON COLUMN tbl_chatlogs.id IS 'Unique identifier for chat log (UUID)';
COMMENT ON COLUMN tbl_chatlogs.user_id IS 'Reference to user who initiated the chat (UUID)';
COMMENT ON COLUMN tbl_chatlogs.message IS 'User message';
COMMENT ON COLUMN tbl_chatlogs.response IS 'Chatbot response';
COMMENT ON COLUMN tbl_chatlogs.timestamp IS 'When the chat occurred';
