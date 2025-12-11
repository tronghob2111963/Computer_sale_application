-- =====================================================
-- RAG CHATBOT - PGVECTOR MIGRATION SCRIPT
-- =====================================================

-- 1. Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Create product_vectors table for storing embeddings
CREATE TABLE IF NOT EXISTS product_vectors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL UNIQUE,
    embedding vector(1536),  -- OpenAI text-embedding-3-large dimension
    name TEXT,
    price NUMERIC(12,2),
    category TEXT,
    brand TEXT,
    product_type TEXT,
    description TEXT,
    specs TEXT,
    stock INTEGER,
    warranty_period INTEGER,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES tbl_products(id) ON DELETE CASCADE
);

-- 3. Create index for vector similarity search (IVFFlat for better performance)
CREATE INDEX IF NOT EXISTS idx_product_vectors_embedding 
ON product_vectors USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- 4. Create index for product_id lookup
CREATE INDEX IF NOT EXISTS idx_product_vectors_product_id ON product_vectors(product_id);

-- 5. Create chat_sessions table for conversation history
CREATE TABLE IF NOT EXISTS chat_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT,
    session_token VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE SET NULL
);

-- 6. Create chat_messages table for storing conversation
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'user' or 'assistant'
    content TEXT NOT NULL,
    products_suggested JSONB, -- Store suggested product IDs
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
);

-- 7. Create index for chat messages
CREATE INDEX IF NOT EXISTS idx_chat_messages_session ON chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user ON chat_sessions(user_id);

-- 8. Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 9. Trigger for product_vectors
DROP TRIGGER IF EXISTS update_product_vectors_updated_at ON product_vectors;
CREATE TRIGGER update_product_vectors_updated_at
    BEFORE UPDATE ON product_vectors
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 10. Trigger for chat_sessions
DROP TRIGGER IF EXISTS update_chat_sessions_updated_at ON chat_sessions;
CREATE TRIGGER update_chat_sessions_updated_at
    BEFORE UPDATE ON chat_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- USEFUL QUERIES FOR TESTING
-- =====================================================

-- Query to check if pgvector is installed
-- SELECT * FROM pg_extension WHERE extname = 'vector';

-- Query to view all product vectors
-- SELECT id, product_id, name, price, category, brand FROM product_vectors;

-- Query to perform similarity search (example)
-- SELECT product_id, name, price, 1 - (embedding <=> '[your_embedding_here]') as similarity
-- FROM product_vectors
-- ORDER BY embedding <=> '[your_embedding_here]'
-- LIMIT 5;
