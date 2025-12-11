# AI Chatbot Setup Guide

## Tổng Quan
Chatbot AI được xây dựng sử dụng Gemini API để giúp khách hàng:
- Kiểm tra tính khả dụng sản phẩm
- Trả lời câu hỏi về giá cả
- Cung cấp thông tin sản phẩm

## Cài Đặt

### 1. Lấy Gemini API Key
1. Truy cập [Google AI Studio](https://aistudio.google.com/app/apikeys)
2. Tạo API key mới (chọn "Create API key")
3. Copy API key

### 2. Cấu Hình Environment

**Option A: Sử dụng Environment Variable (Recommended)**
```bash
# Windows CMD
set GEMINI_API_KEY=your-api-key-here

# Windows PowerShell
$env:GEMINI_API_KEY="your-api-key-here"

# Linux/Mac
export GEMINI_API_KEY=your-api-key-here
```

**Option B: Sử dụng application.yaml**
Cập nhật file `Computer-sell/src/main/resources/application.yaml`:
```yaml
gemini:
  api:
    key: "YOUR_GEMINI_API_KEY_HERE"
```

**Option C: Sử dụng .env file**
Tạo file `.env` trong thư mục `Computer-sell`:
```
GEMINI_API_KEY=your-api-key-here
```

### 3. Build Project
```bash
cd Computer-sell
mvn clean install -DskipTests
```

Nếu gặp lỗi cache Maven cũ:
```bash
mvn clean install -DskipTests -U
```

### 4. Chạy Application
```bash
mvn spring-boot:run
```

Hoặc chạy file JAR:
```bash
java -jar target/Computer-sell.jar
```

Ứng dụng sẽ chạy tại `http://localhost:8080`

### 5. Kiểm Tra Hoạt Động
```bash
# Health check
curl http://localhost:8080/actuator/health

# Test chatbot
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'
```

## API Endpoints

### 1. Chat với Chatbot
**POST** `/api/chatbot/chat`

Request:
```json
{
  "message": "Laptop Dell XPS 13 còn hàng không?"
}
```

Query Parameters:
- `userId` (required): ID của user

Response:
```json
{
  "message": "Sản phẩm Laptop Dell XPS 13 hiện có sẵn với 5 sản phẩm trong kho.",
  "timestamp": "2024-12-07T10:30:00"
}
```

### 2. Kiểm Tra Tính Khả Dụng Sản Phẩm
**GET** `/api/chatbot/product-availability`

Query Parameters:
- `productName` (required): Tên sản phẩm

Response:
```
Sản phẩm 'Laptop Dell XPS 13' hiện có sẵn với 5 sản phẩm trong kho.
```

### 3. Lấy Giá Sản Phẩm
**GET** `/api/chatbot/product-price`

Query Parameters:
- `productName` (required): Tên sản phẩm

Response:
```
Giá của Laptop Dell XPS 13: 25000000 VND
```

## Ví Dụ Sử Dụng

### cURL
```bash
# Chat
curl -X POST http://localhost:8080/api/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Laptop Dell XPS 13 bao nhiêu tiền?"}' \
  -G -d "userId=1"

# Kiểm tra tính khả dụng
curl "http://localhost:8080/api/chatbot/product-availability?productName=Laptop%20Dell%20XPS%2013"

# Lấy giá
curl "http://localhost:8080/api/chatbot/product-price?productName=Laptop%20Dell%20XPS%2013"
```

### JavaScript/Fetch
```javascript
// Chat
const response = await fetch('http://localhost:8080/api/chatbot/chat?userId=1', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    message: 'Laptop Dell XPS 13 còn hàng không?'
  })
});

const data = await response.json();
console.log(data.message);
```

## Cấu Trúc Database

Chatbot sử dụng bảng `tbl_chatlogs` để lưu lịch sử chat:
```sql
CREATE TABLE "tbl_chatlogs" (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT,
  message TEXT,
  response TEXT,
  timestamp TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES "tbl_users" (id)
);
```

## Tính Năng

### 1. Tích Hợp Gemini AI
- Sử dụng mô hình `gemini-pro` để xử lý ngôn ngữ tự nhiên
- Trả lời bằng tiếng Việt
- Hiểu ngữ cảnh từ database

### 2. Kiểm Tra Sản Phẩm
- Tìm kiếm sản phẩm theo tên
- Kiểm tra tồn kho
- Lấy thông tin giá

### 3. Lưu Lịch Sử
- Lưu tất cả các cuộc trò chuyện
- Liên kết với user ID
- Có timestamp

## Xử Lý Lỗi

Chatbot xử lý các lỗi sau:
- **Gemini API Error**: Trả về thông báo lỗi thân thiện
- **Product Not Found**: Thông báo sản phẩm không tìm thấy
- **Database Error**: Ghi log và trả về thông báo lỗi

## Tối Ưu Hóa

### 1. Caching
Có thể thêm caching cho các truy vấn sản phẩm thường xuyên:
```java
@Cacheable("products")
public List<ProductEntity> getProducts() { ... }
```

### 2. Rate Limiting
Thêm rate limiting để tránh abuse:
```java
@RateLimiter(limit = 10, duration = 1, unit = TimeUnit.MINUTES)
public ChatResponseDTO chat(...) { ... }
```

### 3. Async Processing
Xử lý chat không đồng bộ cho hiệu suất tốt hơn:
```java
@Async
public CompletableFuture<ChatResponseDTO> chatAsync(...) { ... }
```

## Troubleshooting

### 1. "API key not found"
- Kiểm tra environment variable `GEMINI_API_KEY`
- Kiểm tra file `application.yaml`

### 2. "Connection timeout"
- Kiểm tra kết nối internet
- Kiểm tra Gemini API status

### 3. "Product not found"
- Kiểm tra tên sản phẩm trong database
- Sử dụng tên chính xác hoặc từ khóa tương tự

## Phát Triển Tiếp Theo

1. **Thêm Tính Năng Gợi Ý**: Gợi ý sản phẩm dựa trên câu hỏi
2. **Tích Hợp Thanh Toán**: Cho phép đặt hàng trực tiếp từ chat
3. **Multi-language**: Hỗ trợ nhiều ngôn ngữ
4. **Analytics**: Phân tích các câu hỏi phổ biến
5. **Machine Learning**: Huấn luyện mô hình tùy chỉnh
