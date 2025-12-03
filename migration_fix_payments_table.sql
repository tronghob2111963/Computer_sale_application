-- Migration: Fix tbl_payments table structure
-- Date: 2025-11-25
-- Description: Add missing columns to tbl_payments table

-- Add missing columns if they don't exist
ALTER TABLE tbl_payments 
ADD COLUMN IF NOT EXISTS payment_method varchar(50),
ADD COLUMN IF NOT EXISTS provider varchar(50),
ADD COLUMN IF NOT EXISTS bank_code varchar(50),
ADD COLUMN IF NOT EXISTS payment_content varchar(255),
ADD COLUMN IF NOT EXISTS note varchar(255);

-- Update payment_method to NOT NULL after adding default values for existing records
UPDATE tbl_payments SET payment_method = 'CASH' WHERE payment_method IS NULL;
ALTER TABLE tbl_payments ALTER COLUMN payment_method SET NOT NULL;

-- Ensure other columns have proper constraints
ALTER TABLE tbl_payments ALTER COLUMN order_id SET NOT NULL;
ALTER TABLE tbl_payments ALTER COLUMN amount SET NOT NULL;
ALTER TABLE tbl_payments ALTER COLUMN payment_date SET NOT NULL;

-- Update column lengths if needed
ALTER TABLE tbl_payments ALTER COLUMN transaction_id TYPE varchar(255);
ALTER TABLE tbl_payments ALTER COLUMN payment_status TYPE varchar(20);

-- Verify the changes
SELECT column_name, data_type, character_maximum_length, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'tbl_payments' 
ORDER BY ordinal_position;
