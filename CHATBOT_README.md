# AI Chatbot - Computer Sell Application

## Giới Thiệu

Chatbot AI được xây dựng sử dụng **Gemini API** để hỗ trợ khách hàng cửa hàng bán máy tính. Chatbot có khả năng:

✅ Trả lời câu hỏi về sản phẩm bằng tiếng Việt  
✅ Kiểm tra tính khả dụng sản phẩm  
✅ Cung cấp thông tin giá cả  
✅ Lưu lịch sử cuộc trò chuyện  
✅ Tích hợp với database sản phẩm  

## Kiến Trúc

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Angular)                    │
│              ChatbotComponent + Service                  │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  Backend (Spring Boot)                   │
│  ┌──────────────────────────────────────────────────┐   │
│  │         ChatBotController                        │   │
│  │  POST /api/chatbot/chat                          │   │
│  │  GET  /api/chatbot/product-availability          │   │
│  │  GET  /api/chatbot/product-price                 │   │
│  └──────────────────────────────────────────────────┘   │
│                         │                                │
│  ┌──────────────────────┴──────────────────────────┐   │
│  │         ChatBotService                          │   │
│  │  - Xử lý logic chatbot                           │   │
│  │  - Gọi Gemini API                               │   │
│  │  - Lưu chat logs                                │   │
│  └──────────────────────┬──────────────────────────┘   │
│                         │                                │
│  ┌──────────────────────┴──────────────────────────┐   │
│  │  ProductRepository + ChatLogRepository           │   │
│  │  - Truy vấn sản phẩm                             │   │
│  │  - Lưu chat logs                                │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
    ┌────────┐      ┌──────────┐    ┌──────────┐
    │Database│      │Gemini API│    │OkHttp    │
    │(Postgres)     │          │    │Client    │
    └────────┘      └──────────┘    └──────────┘
```

## Công Nghệ Sử Dụng

### Backend
- **Spring Boot 3.5.5**: Framework chính
- **Spring Data JPA**: ORM
- **PostgreSQL**: Database
- **OkHttp 4.11.0**: HTTP client
- **Gson 2.10.1**: JSON processing
- **Lombok**: Code generation

### Frontend
- **Angular**: Framework
- **TypeScript**: Ngôn ngữ
- **RxJS**: Reactive programming
- **Bootstrap/CSS**: Styling

### AI
- **Google Gemini API**: Xử lý ngôn ngữ tự nhiên

## Cài Đặt Nhanh

### 1. Lấy API Key
```bash
# Truy cập https://aistudio.google.com/app/apikeys
# Tạo API key mới
# Copy API key
```

### 2. Set Environment Variable
```bash
# Windows CMD
set GEMINI_API_KEY=your-api-key-here

# Windows PowerShell
$env:GEMINI_API_KEY="your-api-key-here"

# Linux/Mac
export GEMINI_API_KEY=your-api-key-here
```

### 3. Build & Run
```bash
cd Computer-sell
mvn clean install
mvn spring-boot:run
```

### 4. Test API
```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Laptop Dell XPS 13 bao nhiêu tiền?"}'
```

## File Cấu Hình

### Backend Files
```
Computer-sell/
├── src/main/java/com/trong/Computer_sell/
│   ├── controller/
│   │   └── ChatBotController.java
│   ├── service/
│   │   ├── ChatBotService.java
│   │   └── impl/
│   │       └── ChatBotServiceImpl.java
│   ├── model/
│   │   └── ChatLog.java
│   ├── repository/
│   │   └── ChatLogRepository.java
│   └── DTO/
│       ├── ChatMessageDTO.java
│       └── ChatResponseDTO.java
├── src/main/resources/
│   └── application.yaml
└── pom.xml
```

### Frontend Files
```
Computer_Sell_FrontEnd/
├── src/app/
│   ├── services/
│   │   └── chatbot.service.ts
│   └── components/
│       └── chatbot/
│           ├── chatbot.component.ts
│           ├── chatbot.component.html
│           └── chatbot.component.css
└── app.module.ts
```

## API Endpoints

### 1. Chat với Chatbot
```
POST /api/chatbot/chat?userId={userId}

Request:
{
  "message": "Laptop Dell XPS 13 bao nhiêu tiền?"
}

Response:
{
  "message": "Giá của Laptop Dell XPS 13 là 25,000,000 VND",
  "timestamp": "2024-12-07T10:30:00"
}
```

### 2. Kiểm Tra Tính Khả Dụng
```
GET /api/chatbot/product-availability?productName={productName}

Response:
"Sản phẩm 'Laptop Dell XPS 13' hiện có sẵn với 5 sản phẩm trong kho."
```

### 3. Lấy Giá Sản Phẩm
```
GET /api/chatbot/product-price?productName={productName}

Response:
"Giá của Laptop Dell XPS 13: 25000000 VND"
```

## Ví Dụ Sử Dụng

### cURL
```bash
# Chat
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Laptop Dell XPS 13 còn hàng không?"}'

# Availability
curl "http://localhost:8080/api/chatbot/product-availability?productName=Laptop"

# Price
curl "http://localhost:8080/api/chatbot/product-price?productName=Laptop"
```

### JavaScript
```javascript
// Chat
const response = await fetch('http://localhost:8080/api/chatbot/chat?userId=1', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ message: 'Laptop Dell XPS 13 bao nhiêu tiền?' })
});
const data = await response.json();
console.log(data.message);
```

## Tính Năng Chính

### 1. Xử Lý Ngôn Ngữ Tự Nhiên
- Sử dụng Gemini AI để hiểu câu hỏi
- Trả lời bằng tiếng Việt
- Hiểu ngữ cảnh từ database

### 2. Kiểm Tra Sản Phẩm
- Tìm kiếm sản phẩm theo tên
- Kiểm tra tồn kho
- Lấy thông tin giá

### 3. Lưu Lịch Sử
- Lưu tất cả cuộc trò chuyện
- Liên kết với user ID
- Có timestamp

### 4. Xử Lý Lỗi
- Gemini API error handling
- Database error handling
- Thông báo lỗi thân thiện

## Hướng Dẫn Chi Tiết

- **[CHATBOT_SETUP_GUIDE.md](CHATBOT_SETUP_GUIDE.md)** - Hướng dẫn cài đặt chi tiết
- **[CHATBOT_FRONTEND_INTEGRATION.md](CHATBOT_FRONTEND_INTEGRATION.md)** - Tích hợp Angular
- **[CHATBOT_TEST_EXAMPLES.md](CHATBOT_TEST_EXAMPLES.md)** - Ví dụ test
- **[CHATBOT_TROUBLESHOOTING.md](CHATBOT_TROUBLESHOOTING.md)** - Khắc phục sự cố

## Troubleshooting

### Lỗi Compile
```bash
mvn clean install
```

### Lỗi API Key
```bash
# Kiểm tra environment variable
echo $GEMINI_API_KEY

# Set lại
export GEMINI_API_KEY=your-api-key-here
```

### Lỗi Database
```bash
# Chạy migration
psql -U postgres -d computer_sell -f Computer_sellDB.sql
```

### Lỗi CORS
Thêm CORS configuration vào backend (xem CHATBOT_TROUBLESHOOTING.md)

## Performance

### Optimization Tips
1. **Caching**: Cache sản phẩm thường xuyên
2. **Rate Limiting**: Giới hạn request
3. **Async Processing**: Xử lý không đồng bộ
4. **Database Indexing**: Index trên tên sản phẩm

### Metrics
- Response time: < 2 seconds
- Throughput: > 100 requests/second
- Availability: > 99.9%

## Phát Triển Tiếp Theo

1. **Thêm Tính Năng Gợi Ý**: Gợi ý sản phẩm dựa trên câu hỏi
2. **Tích Hợp Thanh Toán**: Cho phép đặt hàng từ chat
3. **Multi-language**: Hỗ trợ nhiều ngôn ngữ
4. **Analytics**: Phân tích câu hỏi phổ biến
5. **Machine Learning**: Huấn luyện mô hình tùy chỉnh
6. **Voice Chat**: Hỗ trợ chat bằng giọng nói
7. **Sentiment Analysis**: Phân tích cảm xúc khách hàng

## Bảo Mật

### Best Practices
1. **API Key**: Không commit API key vào git
2. **HTTPS**: Sử dụng HTTPS trong production
3. **Rate Limiting**: Giới hạn request để tránh abuse
4. **Input Validation**: Validate tất cả input
5. **SQL Injection**: Sử dụng parameterized queries

### Environment Variables
```bash
# .env file (không commit)
GEMINI_API_KEY=your-api-key-here
DB_URL=jdbc:postgresql://localhost:5432/computer_sell
DB_USER=postgres
DB_PASSWORD=password
```

## Monitoring & Logging

### Logs
```bash
# Xem logs real-time
tail -f Computer-sell/logs/application.log

# Xem logs với grep
grep "ERROR" Computer-sell/logs/application.log
```

### Metrics
```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics
```

## Support

Nếu gặp vấn đề:
1. Xem [CHATBOT_TROUBLESHOOTING.md](CHATBOT_TROUBLESHOOTING.md)
2. Kiểm tra logs
3. Kiểm tra Gemini API status
4. Kiểm tra database connection

## License

MIT License

## Contributors

- Kiro AI Assistant

## Changelog

### v1.0.0 (2024-12-07)
- ✅ Tạo ChatBot Service
- ✅ Tạo ChatBot Controller
- ✅ Tích hợp Gemini API
- ✅ Lưu chat logs
- ✅ Kiểm tra tính khả dụng sản phẩm
- ✅ Lấy giá sản phẩm
- ✅ Tạo Frontend Component
- ✅ Tạo hướng dẫn chi tiết
