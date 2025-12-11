# 📁 Danh Sách Files Đã Tạo Cho RAG Chatbot

## ✅ Backend (Spring Boot)

### Config
- `Computer-sell/src/main/java/com/trong/Computer_sell/config/OpenAIConfig.java`

### Controllers
- `Computer-sell/src/main/java/com/trong/Computer_sell/controller/RAGChatController.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/controller/EmbeddingController.java`

### Services (Interface)
- `Computer-sell/src/main/java/com/trong/Computer_sell/service/OpenAIService.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/service/VectorService.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/service/RAGService.java`

### Services (Implementation)
- `Computer-sell/src/main/java/com/trong/Computer_sell/service/impl/OpenAIServiceImpl.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/service/impl/VectorServiceImpl.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/service/impl/RAGServiceImpl.java`

### Repositories
- `Computer-sell/src/main/java/com/trong/Computer_sell/repository/ProductVectorRepository.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/repository/ChatSessionRepository.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/repository/ChatMessageRepository.java`

### Entities
- `Computer-sell/src/main/java/com/trong/Computer_sell/model/ProductVectorEntity.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/model/ChatSessionEntity.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/model/ChatMessageEntity.java`

### DTOs
- `Computer-sell/src/main/java/com/trong/Computer_sell/DTO/request/RAGChatRequest.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/DTO/response/RAGChatResponse.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/DTO/response/EmbeddingRebuildResponse.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/DTO/response/ProductVectorDTO.java`

### Resources
- `Computer-sell/src/main/resources/prompts/rag_system_prompt.txt`

### Config Updates
- `Computer-sell/src/main/resources/application.yaml` (thêm OpenAI config)
- `Computer-sell/src/main/resources/application-dev.yaml` (thêm OpenAI config)
- `Computer-sell/src/main/java/com/trong/Computer_sell/config/AppConfig.java` (thêm whitelist endpoints)

---

## ✅ Frontend (Angular)

### Services
- `Computer_Sell_FrontEnd/src/app/services/rag-chatbot.service.ts`

### Components (Updated)
- `Computer_Sell_FrontEnd/src/app/components/chatbot/chatbot.component.ts`
- `Computer_Sell_FrontEnd/src/app/components/chatbot/chatbot.component.html`
- `Computer_Sell_FrontEnd/src/app/components/chatbot/chatbot.component.scss`

---

## ✅ SQL Scripts

- `migration_rag_chatbot.sql` - Tạo bảng product_vectors, chat_sessions, chat_messages
- `test_rag_chatbot.sql` - Các query test

---

## ✅ Documentation

- `RAG_CHATBOT_SETUP_GUIDE.md` - Hướng dẫn cài đặt chi tiết

---

## 🚀 Quick Start

```bash
# 1. Chạy migration SQL
psql -U postgres -d Computer_sale_aplication -f migration_rag_chatbot.sql

# 2. Set OpenAI API Key
set OPENAI_API_KEY=sk-your-key-here

# 3. Chạy Backend
cd Computer-sell
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Build Embeddings
curl -X POST http://localhost:8080/api/embeddings/rebuild

# 5. Chạy Frontend
cd Computer_Sell_FrontEnd
ng serve

# 6. Test chatbot tại http://localhost:4200
```

---

## 📊 API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/chat/ask` | Gửi câu hỏi cho RAG chatbot |
| POST | `/api/chat/session` | Tạo phiên chat mới |
| POST | `/api/embeddings/rebuild` | Rebuild tất cả embeddings |
| GET | `/api/embeddings/status` | Kiểm tra trạng thái embeddings |
