-- Migration: Switch from OpenAI to Gemini embeddings
-- Run this if you're switching from OpenAI to Gemini
-- Gemini embedding-001 uses 768 dimensions vs OpenAI's 1536

-- Clear existing product vectors (they have wrong dimensions)
TRUNCATE TABLE product_vectors;

-- Update vector column to 768 dimensions for Gemini
ALTER TABLE product_vectors 
ALTER COLUMN embedding TYPE vector(768);

-- Note: After running this, restart the backend and 
-- call the /api/rag/admin/sync-products endpoint to regenerate embeddings

-- If you get dimension mismatch errors, also run:
-- DROP TABLE IF EXISTS product_vectors;
-- Then re-run the original migration_rag_chatbot.sql with vector(768) instead of vector(1536)
