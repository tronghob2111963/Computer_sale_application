-- Migration: Add VietQR proof image column to payments table
-- Run this SQL to add support for VietQR payment proof images

-- Add proof_image_url column to tbl_payments
ALTER TABLE tbl_payments 
ADD COLUMN IF NOT EXISTS proof_image_url VARCHAR(500) NULL;

-- Add comment for documentation
COMMENT ON COLUMN tbl_payments.proof_image_url IS 'URL ảnh xác nhận chuyển khoản VietQR';

-- Optional: Create index for faster queries on payment method
CREATE INDEX IF NOT EXISTS idx_payments_method ON tbl_payments(payment_method);

-- Verify the column was added
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'tbl_payments' AND column_name = 'proof_image_url';
