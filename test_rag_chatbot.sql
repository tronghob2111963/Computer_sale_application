-- =====================================================
-- TEST QUERIES FOR RAG CHATBOT
-- =====================================================

-- 1. Kiểm tra pgvector đã cài đặt
SELECT * FROM pg_extension WHERE extname = 'vector';

-- 2. Kiểm tra bảng product_vectors đã tạo
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'product_vectors';

-- 3. Đếm số sản phẩm có embedding
SELECT COUNT(*) as total_embeddings FROM product_vectors WHERE embedding IS NOT NULL;

-- 4. Xem danh sách sản phẩm đã có embedding
SELECT product_id, name, price, category, brand, stock 
FROM product_vectors 
ORDER BY updated_at DESC 
LIMIT 10;

-- 5. Test vector similarity search (cần có embedding query)
-- Ví dụ: Tìm 5 sản phẩm tương tự nhất
-- SELECT product_id, name, price, 
--        1 - (embedding <=> '[0.1, 0.2, ...]') as similarity
-- FROM product_vectors
-- ORDER BY embedding <=> '[0.1, 0.2, ...]'
-- LIMIT 5;

-- 6. Kiểm tra chat sessions
SELECT * FROM chat_sessions ORDER BY created_at DESC LIMIT 5;

-- 7. Kiểm tra chat messages
SELECT cm.id, cs.session_token, cm.role, 
       LEFT(cm.content, 100) as content_preview,
       cm.created_at
FROM chat_messages cm
JOIN chat_sessions cs ON cm.session_id = cs.id
ORDER BY cm.created_at DESC
LIMIT 10;

-- 8. Thống kê sản phẩm theo category
SELECT category, COUNT(*) as count, 
       AVG(price) as avg_price,
       MIN(price) as min_price,
       MAX(price) as max_price
FROM product_vectors
GROUP BY category
ORDER BY count DESC;

-- 9. Thống kê sản phẩm theo brand
SELECT brand, COUNT(*) as count
FROM product_vectors
GROUP BY brand
ORDER BY count DESC;

-- 10. Sản phẩm còn hàng
SELECT name, price, stock, category, brand
FROM product_vectors
WHERE stock > 0
ORDER BY price ASC
LIMIT 20;

-- 11. Xóa tất cả embeddings (nếu cần rebuild)
-- TRUNCATE TABLE product_vectors;

-- 12. Xóa chat history
-- TRUNCATE TABLE chat_messages;
-- TRUNCATE TABLE chat_sessions;
