# Chatbot Integration Guide

## 🎯 Tổng Quan

Hướng dẫn này giúp bạn tích hợp chatbot vào ứng dụng Angular.

## 📋 Các Bước Tích Hợp

### Bước 1: Sửa Database Schema

**Chạy migration SQL:**

```bash
psql -U postgres -d computer_sell -f migration_fix_chatlogs_bigint.sql
```

### Bước 2: Cập Nhật Backend Configuration

**File: `Computer-sell/src/main/resources/application.yaml`**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Thay từ create-drop sang validate
```

### Bước 3: Build & Run Backend

```bash
cd Computer-sell
mvn clean install -DskipTests
mvn spring-boot:run
```

### Bước 4: Tích Hợp Chatbot vào App Component

**File: `Computer_Sell_FrontEnd/src/app/app.component.ts`**

```typescript
import { Component, OnInit } from '@angular/core';
import { ChatbotComponent } from './components/chatbot/chatbot.component';
import { AuthService } from './services/auth.service'; // Your auth service

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ChatbotComponent, /* other imports */],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Chatbot sẽ tự động khởi tạo
  }
}
```

**File: `Computer_Sell_FrontEnd/src/app/app.component.html`**

```html
<!-- Your existing content -->
<router-outlet></router-outlet>

<!-- Add chatbot at the end -->
<app-chatbot></app-chatbot>
```

### Bước 5: Cập Nhật Environment Configuration

**File: `Computer_Sell_FrontEnd/src/app/enviroment.ts`**

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'  // Ensure this is set correctly
};
```

### Bước 6: Cập Nhật User ID (Optional)

Nếu bạn muốn cập nhật user ID từ auth service:

**File: `Computer_Sell_FrontEnd/src/app/app.component.ts`**

```typescript
import { Component, OnInit, ViewChild } from '@angular/core';
import { ChatbotComponent } from './components/chatbot/chatbot.component';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ChatbotComponent, /* other imports */],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  @ViewChild(ChatbotComponent) chatbotComponent!: ChatbotComponent;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // Get current user and set chatbot user ID
    this.authService.getCurrentUser().subscribe(user => {
      if (user && this.chatbotComponent) {
        this.chatbotComponent.setUserId(user.id);
      }
    });
  }
}
```

## 🧪 Test Chatbot

### 1. Test Backend API

```bash
# Test chatbot endpoint
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'

# Expected response:
# {
#   "message": "Xin chào! Tôi là trợ lý bán hàng...",
#   "timestamp": "2025-12-09T01:50:26.682+07:00"
# }
```

### 2. Test Frontend

1. Chạy frontend: `ng serve`
2. Mở browser: `http://localhost:4200`
3. Nhấp vào nút chatbot ở góc phải dưới
4. Gửi tin nhắn

## 📁 File Structure

```
Computer_Sell_FrontEnd/
├── src/
│   └── app/
│       ├── components/
│       │   └── chatbot/
│       │       ├── chatbot.component.ts
│       │       ├── chatbot.component.html
│       │       └── chatbot.component.scss
│       ├── services/
│       │   └── chatbot.service.ts
│       ├── app.component.ts
│       ├── app.component.html
│       ���── enviroment.ts
```

## 🔧 Troubleshooting

### Lỗi: "Cannot find module 'chatbot.service'"

**Giải pháp:** Đảm bảo file service được tạo trong đúng thư mục:
```
Computer_Sell_FrontEnd/src/app/services/chatbot.service.ts
```

### Lỗi: "CORS error"

**Giải pháp:** Thêm CORS configuration vào backend:

**File: `Computer-sell/src/main/java/com/trong/Computer_sell/config/CorsConfig.java`**

```java
package com.trong.Computer_sell.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### Lỗi: "API returns 404"

**Giải pháp:** Kiểm tra:
1. Backend chạy trên port 8080
2. API endpoint đúng: `/api/chatbot/chat`
3. Environment URL đúng: `http://localhost:8080`

### Lỗi: "Chatbot không hiển thị"

**Giải pháp:**
1. Kiểm tra `app.component.html` có `<app-chatbot></app-chatbot>`
2. Kiểm tra `app.component.ts` import `ChatbotComponent`
3. Kiểm tra z-index không bị che phủ

## 🚀 Production Deployment

### Backend

```bash
# Build JAR
cd Computer-sell
mvn clean package -DskipTests

# Run JAR
java -jar target/Computer-sell-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
# Build for production
ng build --configuration production

# Deploy dist folder to web server
```

## 📝 API Documentation

### POST /api/chatbot/chat

**Request:**
```json
{
  "message": "Xin chào"
}
```

**Query Parameters:**
- `userId` (required): User ID

**Response:**
```json
{
  "message": "Xin chào! Tôi là trợ lý bán hàng...",
  "timestamp": "2025-12-09T01:50:26.682+07:00"
}
```

### GET /api/chatbot/product-availability

**Query Parameters:**
- `productName` (required): Product name

**Response:**
```
"Sản phẩm 'Laptop' hiện có sẵn với 5 sản phẩm trong kho."
```

### GET /api/chatbot/product-price

**Query Parameters:**
- `productName` (required): Product name

**Response:**
```
"Giá của Laptop: 15000000 VND"
```

## ✅ Checklist

- [ ] Chạy migration SQL
- [ ] Cập nhật application.yaml
- [ ] Build backend
- [ ] Run backend
- [ ] Tạo chatbot service
- [ ] Tạo chatbot component
- [ ] Tích hợp vào app.component
- [ ] Cập nhật environment
- [ ] Test API
- [ ] Test UI
- [ ] Kiểm tra CORS
- [ ] Deploy

## 🎉 Hoàn Thành!

Chatbot đã sẵn sàng sử dụng!

