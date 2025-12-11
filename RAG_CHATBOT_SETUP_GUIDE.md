# 🤖 Hướng Dẫn Cài Đặt RAG Chatbot Tư Vấn Máy Tính

## 📋 Tổng Quan

Hệ thống AI Chatbot sử dụng kiến trúc **RAG (Retrieval-Augmented Generation)** để tư vấn mua máy tính dựa trên dữ liệu sản phẩm thực tế từ PostgreSQL.

### Công nghệ sử dụng:
- **Backend**: Spring Boot 3 + Maven
- **Database**: PostgreSQL + pgvector extension
- **AI Provider**: OpenAI API (GPT-4o-mini + text-embedding-3-large)
- **Frontend**: Angular 18+

---

## 🚀 Bước 1: Cài Đặt pgvector Extension

### Trên PostgreSQL (Windows/Linux/Mac):

```sql
-- Kết nối vào database
psql -U postgres -d Computer_sale_aplication

-- Cài đặt extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Kiểm tra đã cài thành công
SELECT * FROM pg_extension WHERE extname = 'vector';
```

### Nếu chưa có pgvector:

**Windows (với PostgreSQL installer):**
```powershell
# Download pgvector từ https://github.com/pgvector/pgvector/releases
# Copy file .dll vào thư mục lib của PostgreSQL
# Restart PostgreSQL service
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt install postgresql-16-pgvector
# hoặc build từ source
git clone https://github.com/pgvector/pgvector.git
cd pgvector
make
sudo make install
```

**Docker:**
```yaml
# Sử dụng image có sẵn pgvector
image: pgvector/pgvector:pg16
```

---

## 🗄️ Bước 2: Chạy Migration Script

Chạy file `migration_rag_chatbot.sql` để tạo các bảng cần thiết:

```sql
-- Chạy trong psql hoặc pgAdmin
\i migration_rag_chatbot.sql
```

Script này sẽ tạo:
- `product_vectors` - Lưu embedding vectors của sản phẩm
- `chat_sessions` - Quản lý phiên chat
- `chat_messages` - Lưu lịch sử hội thoại

---

## 🔑 Bước 3: Cấu Hình OpenAI API Key

### Cách 1: Environment Variable (Khuyến nghị)

**Windows PowerShell:**
```powershell
$env:OPENAI_API_KEY = "sk-your-api-key-here"
```

**Windows CMD:**
```cmd
set OPENAI_API_KEY=sk-your-api-key-here
```

**Linux/Mac:**
```bash
export OPENAI_API_KEY="sk-your-api-key-here"
```

### Cách 2: File .env (Development)

Tạo file `.env` trong thư mục `Computer-sell`:
```properties
OPENAI_API_KEY=sk-your-api-key-here
```

### Cách 3: application-dev.yaml

```yaml
openai:
  api-key: "sk-your-api-key-here"
  embedding-model: "text-embedding-3-large"
  chat-model: "gpt-4o-mini"
```

---

## 🏃 Bước 4: Chạy Backend

```bash
cd Computer-sell

# Build project
./mvnw clean install -DskipTests

# Chạy với profile dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend sẽ chạy tại: `http://localhost:8080`

---

## 📊 Bước 5: Build Embeddings

Sau khi backend chạy, gọi API để tạo embeddings cho tất cả sản phẩm:

```bash
# Sử dụng curl
curl -X POST http://localhost:8080/api/embeddings/rebuild

# Hoặc sử dụng Swagger UI
# Truy cập: http://localhost:8080/swagger-ui.html
# Tìm endpoint POST /api/embeddings/rebuild
```

**Response mẫu:**
```json
{
  "status": "COMPLETED",
  "totalProducts": 50,
  "successCount": 50,
  "failedCount": 0,
  "durationMs": 15234,
  "timestamp": "2024-12-10T10:30:00"
}
```

---

## 🎨 Bước 6: Chạy Frontend

```bash
cd Computer_Sell_FrontEnd

# Cài dependencies
npm install

# Chạy development server
ng serve
```

Frontend sẽ chạy tại: `http://localhost:4200`

---

## 🧪 Bước 7: Test Chatbot

### Câu hỏi mẫu để test:

1. **Tư vấn theo ngân sách:**
   - "Tôi cần laptop gaming dưới 20 triệu"
   - "Gợi ý PC văn phòng khoảng 10-15 triệu"

2. **Tư vấn theo nhu cầu:**
   - "Laptop nào phù hợp cho sinh viên học lập trình?"
   - "Máy tính để render video 4K"
   - "Laptop nhẹ, pin trâu để đi công tác"

3. **So sánh sản phẩm:**
   - "So sánh laptop ASUS và Dell trong tầm giá 25 triệu"

4. **Hỏi thông số:**
   - "RAM 16GB có đủ cho đồ họa không?"
   - "Card RTX 4060 chơi game gì được?"

---

## 📁 Cấu Trúc Files Đã Tạo

### Backend (Spring Boot):

```
Computer-sell/src/main/java/com/trong/Computer_sell/
├── config/
│   └── OpenAIConfig.java           # Cấu hình OpenAI
├── controller/
│   ├── RAGChatController.java      # API chat RAG
│   └── EmbeddingController.java    # API quản lý embeddings
├── service/
│   ├── OpenAIService.java          # Interface OpenAI
│   ├── VectorService.java          # Interface Vector DB
│   ├── RAGService.java             # Interface RAG
│   └── impl/
│       ├── OpenAIServiceImpl.java  # Gọi OpenAI API
│       ├── VectorServiceImpl.java  # Xử lý vector search
│       └── RAGServiceImpl.java     # Pipeline RAG
├── repository/
│   ├── ProductVectorRepository.java
│   ├── ChatSessionRepository.java
│   └── ChatMessageRepository.java
├── model/
│   ├── ProductVectorEntity.java
│   ├── ChatSessionEntity.java
│   └── ChatMessageEntity.java
└── DTO/
    ├── request/
    │   └── RAGChatRequest.java
    └── response/
        ├── RAGChatResponse.java
        └── EmbeddingRebuildResponse.java
```

### Frontend (Angular):

```
Computer_Sell_FrontEnd/src/app/
├── services/
│   └── rag-chatbot.service.ts      # Service gọi API RAG
└── components/
    └── chatbot/
        ├── chatbot.component.ts    # Logic component
        ├── chatbot.component.html  # Template với product cards
        └── chatbot.component.scss  # Styles modern
```

### SQL:

```
migration_rag_chatbot.sql           # Script tạo bảng vector
```

---

## 🔧 API Endpoints

### Chat API:

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/chat/ask` | Gửi câu hỏi cho chatbot |
| POST | `/api/chat/session` | Tạo phiên chat mới |

### Embedding API:

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/embeddings/rebuild` | Rebuild tất cả embeddings |
| GET | `/api/embeddings/status` | Kiểm tra trạng thái |

### Request/Response Examples:

**POST /api/chat/ask**
```json
// Request
{
  "message": "Tư vấn laptop gaming dưới 25 triệu",
  "sessionId": "abc-123-xyz"  // optional
}

// Response
{
  "answer": "Với ngân sách 25 triệu cho gaming, tôi gợi ý...",
  "products": [
    {
      "id": "uuid-1",
      "name": "ASUS TUF Gaming F15",
      "price": 22990000,
      "category": "Laptop Gaming",
      "brand": "ASUS",
      "stock": 5,
      "warrantyPeriod": 24,
      "similarityScore": 0.89
    }
  ],
  "sessionId": "abc-123-xyz",
  "timestamp": "2024-12-10T10:30:00"
}
```

---

## ⚠️ Troubleshooting

### 1. Lỗi "vector type not found"
```
Nguyên nhân: pgvector chưa được cài đặt
Giải pháp: Chạy CREATE EXTENSION vector;
```

### 2. Lỗi OpenAI API rate limit
```
Nguyên nhân: Gọi API quá nhanh
Giải pháp: Code đã có delay 100ms giữa các request
```

### 3. Lỗi CORS
```
Nguyên nhân: Frontend gọi từ domain khác
Giải pháp: Đã cấu hình trong AppConfig.java
```

### 4. Embedding không tạo được
```
Nguyên nhân: API key không hợp lệ
Giải pháp: Kiểm tra OPENAI_API_KEY environment variable
```

---

## 💰 Chi Phí OpenAI API

| Model | Giá | Sử dụng |
|-------|-----|---------|
| text-embedding-3-large | $0.00013/1K tokens | Tạo embeddings |
| gpt-4o-mini | $0.15/1M input tokens | Chat completion |

**Ước tính:**
- Rebuild 100 sản phẩm: ~$0.05
- 1000 câu hỏi chat: ~$0.50

---

## 🎯 Tính Năng Chính

1. ✅ **Vector Search**: Tìm sản phẩm tương tự bằng cosine similarity
2. ✅ **Price Extraction**: Tự động nhận diện ngân sách từ câu hỏi
3. ✅ **Conversation History**: Lưu và sử dụng lịch sử hội thoại
4. ✅ **Product Cards**: Hiển thị sản phẩm gợi ý với hình ảnh
5. ✅ **Quick Actions**: Gợi ý câu hỏi nhanh
6. ✅ **Markdown Support**: Format câu trả lời đẹp

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề, kiểm tra:
1. Logs backend: `Computer-sell/logs/`
2. Browser console: F12 → Console
3. Network tab: Kiểm tra API calls

Happy coding! 🚀
