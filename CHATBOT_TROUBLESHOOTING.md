# Hướng Dẫn Khắc Phục Sự Cố Chatbot

## Lỗi Compile

### 1. "package com.google.ai.client.generativeai does not exist"

**Nguyên nhân**: Dependency không đúng hoặc chưa được tải về

**Giải pháp**:
```bash
# Xóa cache Maven
mvn clean

# Tải lại dependencies
mvn dependency:resolve

# Build lại
mvn clean install
```

### 2. "Cannot find symbol: class OkHttpClient"

**Nguyên nhân**: OkHttp dependency chưa được thêm

**Giải pháp**: Kiểm tra pom.xml có chứa:
```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.11.0</version>
</dependency>
```

## Lỗi Runtime

### 1. "API key not found" hoặc "401 Unauthorized"

**Nguyên nhân**: API key không được cấu hình đúng

**Giải pháp**:
```bash
# Kiểm tra environment variable
# Windows CMD
echo %GEMINI_API_KEY%

# Windows PowerShell
$env:GEMINI_API_KEY

# Linux/Mac
echo $GEMINI_API_KEY
```

Nếu không có output, hãy set lại:
```bash
# Windows CMD
set GEMINI_API_KEY=your-actual-api-key

# Windows PowerShell
$env:GEMINI_API_KEY="your-actual-api-key"

# Linux/Mac
export GEMINI_API_KEY=your-actual-api-key
```

### 2. "Connection timeout" hoặc "Unable to connect to API"

**Nguyên nhân**: 
- Không có kết nối internet
- Gemini API bị chặn
- API key không hợp lệ

**Giải pháp**:
1. Kiểm tra kết nối internet
2. Kiểm tra API key tại https://aistudio.google.com/app/apikeys
3. Thử lại sau vài phút

### 3. "Product not found"

**Nguyên nhân**: Sản phẩm không tồn tại trong database

**Giải pháp**:
1. Kiểm tra tên sản phẩm chính xác
2. Chạy seed data:
```bash
psql -U postgres -d computer_sell -f seed_products.sql
```

### 4. "NullPointerException in ChatBotServiceImpl"

**Nguyên nhân**: userId là null hoặc message rỗng

**Giải pháp**: Kiểm tra request:
```bash
# Đúng
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Laptop Dell XPS 13 bao nhiêu tiền?"}'

# Sai - thiếu userId
curl -X POST http://localhost:8080/api/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Laptop Dell XPS 13 bao nhiêu tiền?"}'
```

## Lỗi Database

### 1. "Table tbl_chatlogs does not exist"

**Nguyên nhân**: Migration chưa được chạy

**Giải pháp**:
```sql
-- Chạy migration
psql -U postgres -d computer_sell -f Computer_sellDB.sql
```

### 2. "Foreign key constraint violation"

**Nguyên nhân**: userId không tồn tại trong tbl_users

**Giải pháp**:
```sql
-- Kiểm tra user tồn tại
SELECT * FROM tbl_users WHERE id = 1;

-- Nếu không có, tạo user test
INSERT INTO tbl_users (username, password, email, created_at, updated_at)
VALUES ('testuser', 'password123', 'test@example.com', NOW(), NOW());
```

## Lỗi CORS (Frontend)

### "Access to XMLHttpRequest blocked by CORS policy"

**Nguyên nhân**: Backend không cho phép request từ frontend

**Giải pháp**: Thêm CORS configuration vào backend

Tạo file `Computer-sell/src/main/java/com/trong/Computer_sell/config/CorsConfig.java`:

```java
package com.trong.Computer_sell.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173", "http://localhost:4200", "http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

## Lỗi API Response

### 1. "Invalid JSON response"

**Nguyên nhân**: Response từ Gemini API không đúng format

**Giải pháp**: Kiểm tra logs:
```bash
# Xem logs chi tiết
tail -f logs/application.log

# Hoặc chạy với debug mode
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
```

### 2. "Empty response from Gemini"

**Nguyên nhân**: Prompt quá dài hoặc API rate limit

**Giải pháp**:
1. Giảm độ dài prompt
2. Thêm delay giữa các request
3. Kiểm tra rate limit tại https://aistudio.google.com/app/apikeys

## Lỗi Performance

### 1. "Chatbot response very slow"

**Nguyên nhân**: 
- Gemini API chậm
- Database query chậm
- Network chậm

**Giải pháp**:
1. Thêm caching:
```java
@Cacheable("products")
public List<ProductEntity> getProducts() { ... }
```

2. Tối ưu database query:
```java
@Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) LIMIT 10")
Page<ProductEntity> searchUserByKeyword(String keyword, Pageable pageable);
```

3. Thêm timeout:
```yaml
gemini:
  api:
    key: "${GEMINI_API_KEY}"
    timeout: 30000  # 30 seconds
```

## Testing

### Test API với Postman

1. **Import Collection**:
   - Tạo request POST: `http://localhost:8080/api/chatbot/chat?userId=1`
   - Headers: `Content-Type: application/json`
   - Body: `{"message":"Laptop Dell XPS 13 bao nhiêu tiền?"}`

2. **Test Availability**:
   - GET: `http://localhost:8080/api/chatbot/product-availability?productName=Laptop`

3. **Test Price**:
   - GET: `http://localhost:8080/api/chatbot/product-price?productName=Laptop`

### Test với cURL

```bash
# Test chat
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Laptop Dell XPS 13 bao nhiêu tiền?"}' \
  -v

# Test availability
curl -v "http://localhost:8080/api/chatbot/product-availability?productName=Laptop"

# Test price
curl -v "http://localhost:8080/api/chatbot/product-price?productName=Laptop"
```

## Debug Mode

### Bật logging chi tiết

Cập nhật `application.yaml`:
```yaml
logging:
  level:
    root: INFO
    com.trong.Computer_sell: DEBUG
    okhttp3: DEBUG
```

### Xem request/response

Thêm interceptor vào OkHttpClient:

```java
@Configuration
public class HttpClientConfig {
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();
    }
}
```

## Liên Hệ Hỗ Trợ

Nếu vẫn gặp vấn đề:
1. Kiểm tra logs: `Computer-sell/logs/application.log`
2. Kiểm tra Gemini API status: https://status.cloud.google.com/
3. Kiểm tra database connection
4. Thử restart application
