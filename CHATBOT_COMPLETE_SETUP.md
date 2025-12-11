# Chatbot Complete Setup Guide

## 🎯 Mục Tiêu

Sửa lỗi schema database và hoàn thành chức năng chatbot cho cả backend và frontend.

## 🔴 Vấn Đề Hiện Tại

```
ERROR: column "id" cannot be cast automatically to type bigint
Hint: You might need to specify "USING id::bigint".
```

## ✅ Giải Pháp Hoàn Chỉnh

### PHẦN 1: SỬA DATABASE

#### Bước 1.1: Kết Nối PostgreSQL

**Windows:**
```bash
psql -U postgres
```

**Linux/Mac:**
```bash
psql -U postgres
```

#### Bước 1.2: Chọn Database

```sql
\c computer_sell
```

Nếu database chưa tồn tại:
```sql
CREATE DATABASE computer_sell;
\c computer_sell
```

#### Bước 1.3: Chạy Migration

Dán toàn bộ SQL sau vào PostgreSQL:

```sql
-- Drop the old table if it exists
DROP TABLE IF EXISTS tbl_chatlogs CASCADE;

-- Create the new table with correct schema
CREATE TABLE tbl_chatlogs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT,
    response TEXT,
    timestamp TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_chatlogs_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_chatlogs_user_id ON tbl_chatlogs(user_id);
CREATE INDEX idx_chatlogs_timestamp ON tbl_chatlogs(timestamp DESC);

-- Add comments
COMMENT ON TABLE tbl_chatlogs IS 'Stores chatbot conversation logs';
COMMENT ON COLUMN tbl_chatlogs.id IS 'Unique identifier for chat log';
COMMENT ON COLUMN tbl_chatlogs.user_id IS 'Reference to user who initiated the chat';
COMMENT ON COLUMN tbl_chatlogs.message IS 'User message';
COMMENT ON COLUMN tbl_chatlogs.response IS 'Chatbot response';
COMMENT ON COLUMN tbl_chatlogs.timestamp IS 'When the chat occurred';
```

#### Bước 1.4: Verify

```sql
-- Kiểm tra bảng
\dt tbl_chatlogs

-- Kiểm tra schema
\d tbl_chatlogs

-- Kiểm tra index
\di tbl_chatlogs*
```

**Output mong đợi:**
```
                    Table "public.tbl_chatlogs"
   Column   |            Type             | Collation | Nullable | Default
------------+-----------------------------+-----------+----------+---------
 id         | bigint                      |           | not null | nextval(...)
 user_id    | bigint                      |           | not null |
 message    | text                        |           |          |
 response   | text                        |           |          |
 timestamp  | timestamp without time zone |           |          |
```

### PHẦN 2: CẬP NHẬT BACKEND

#### Bước 2.1: Cập Nhật application.yaml

**File: `Computer-sell/src/main/resources/application.yaml`**

Thay đổi:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # ❌ Cũ
```

Thành:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # ✅ Mới
```

#### Bước 2.2: Kiểm Tra Các File Backend

Các file sau đã được tạo/cập nhật:

1. **CorsConfig.java** - Cho phép CORS requests
2. **RestTemplateConfig.java** - Cấu hình HTTP client
3. **ChatBotController.java** - API endpoints (đã tồn tại)
4. **ChatBotServiceImpl.java** - Business logic (đã tồn tại)
5. **ChatLog.java** - Entity (đã tồn tại)

#### Bước 2.3: Build Backend

```bash
cd Computer-sell

# Clean build
mvn clean install -DskipTests

# Nếu gặp lỗi, thử:
mvn clean install -DskipTests -X
```

#### Bước 2.4: Run Backend

```bash
# Option 1: Maven
mvn spring-boot:run

# Option 2: Java
java -jar target/Computer-sell-0.0.1-SNAPSHOT.jar
```

**Output mong đợi:**
```
Started ComputerSellApplication in X seconds
```

### PHẦN 3: TÍCH HỢP FRONTEND

#### Bước 3.1: Tạo Chatbot Service

**File: `Computer_Sell_FrontEnd/src/app/services/chatbot.service.ts`**

Đã được tạo sẵn.

#### Bước 3.2: Tạo Chatbot Component

**Files:**
- `Computer_Sell_FrontEnd/src/app/components/chatbot/chatbot.component.ts`
- `Computer_Sell_FrontEnd/src/app/components/chatbot/chatbot.component.html`
- `Computer_Sell_FrontEnd/src/app/components/chatbot/chatbot.component.scss`

Đã được tạo sẵn.

#### Bước 3.3: Tích Hợp vào App Component

**File: `Computer_Sell_FrontEnd/src/app/app.component.ts`**

```typescript
import { Component } from '@angular/core';
import { ChatbotComponent } from './components/chatbot/chatbot.component';
// ... other imports

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    ChatbotComponent,  // ✅ Thêm dòng này
    // ... other imports
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  // ... component code
}
```

**File: `Computer_Sell_FrontEnd/src/app/app.component.html`**

```html
<!-- Your existing content -->
<router-outlet></router-outlet>

<!-- Add chatbot at the end -->
<app-chatbot></app-chatbot>
```

#### Bước 3.4: Cập Nhật Environment

**File: `Computer_Sell_FrontEnd/src/app/enviroment.ts`**

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'  // ✅ Đảm bảo URL đúng
};
```

#### Bước 3.5: Run Frontend

```bash
cd Computer_Sell_FrontEnd

# Option 1: Angular CLI
ng serve

# Option 2: npm
npm start

# Option 3: yarn
yarn start
```

**Output mong đợi:**
```
✔ Compiled successfully.
Application bundle generated successfully.
```

### PHẦN 4: TEST

#### Bước 4.1: Test Backend API

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

#### Bước 4.2: Test Frontend

1. Mở browser: `http://localhost:4200`
2. Nhấp vào nút chatbot ở góc phải dưới (icon chat)
3. Gửi tin nhắn: "Xin chào"
4. Kiểm tra response từ chatbot

#### Bước 4.3: Test Product Queries

```bash
# Test product availability
curl "http://localhost:8080/api/chatbot/product-availability?productName=Laptop"

# Test product price
curl "http://localhost:8080/api/chatbot/product-price?productName=Laptop"
```

## 🐛 Troubleshooting

### Lỗi 1: "ERROR: column "id" cannot be cast automatically to type bigint"

**Nguyên nhân:** Schema database không khớp với entity

**Giải pháp:**
1. Chạy migration SQL (Phần 1)
2. Cập nhật `ddl-auto: validate` (Bước 2.1)
3. Restart backend

### Lỗi 2: "CORS error: Access to XMLHttpRequest blocked"

**Nguyên nhân:** CORS không được cấu hình

**Giải pháp:**
1. Kiểm tra `CorsConfig.java` tồn tại
2. Restart backend
3. Kiểm tra frontend URL trong `allowedOrigins`

### Lỗi 3: "Cannot find module 'chatbot.service'"

**Nguyên nhân:** File service không tồn tại

**Giải pháp:**
1. Kiểm tra file tồn tại: `Computer_Sell_FrontEnd/src/app/services/chatbot.service.ts`
2. Kiểm tra import path đúng
3. Restart frontend dev server

### Lỗi 4: "API returns 404"

**Nguyên nhân:** Backend không chạy hoặc endpoint sai

**Giải pháp:**
1. Kiểm tra backend chạy: `http://localhost:8080/actuator/health`
2. Kiểm tra endpoint: `/api/chatbot/chat`
3. Kiểm tra user ID hợp lệ

### Lỗi 5: "Chatbot không hiển thị"

**Nguyên nhân:** Component không được import hoặc z-index bị che phủ

**Giải pháp:**
1. Kiểm tra `app.component.ts` import `ChatbotComponent`
2. Kiểm tra `app.component.html` có `<app-chatbot></app-chatbot>`
3. Kiểm tra z-index: `.chatbot-container { z-index: 1000; }`

## 📋 Checklist Hoàn Thành

- [ ] Chạy migration SQL
- [ ] Verify database schema
- [ ] Cập nhật application.yaml
- [ ] Build backend
- [ ] Run backend
- [ ] Kiểm tra backend health
- [ ] Tạo chatbot service
- [ ] Tạo chatbot component
- [ ] Tích hợp vào app.component
- [ ] Cập nhật environment
- [ ] Run frontend
- [ ] Test API
- [ ] Test UI
- [ ] Kiểm tra CORS
- [ ] Kiểm tra console errors

## 🎉 Hoàn Thành!

Nếu tất cả các bước đã hoàn thành, chatbot sẽ chạy bình thường!

## 📞 Support

Nếu vẫn gặp vấn đề:

1. **Kiểm tra logs:**
   - Backend: Console output
   - Frontend: Browser DevTools (F12)

2. **Kiểm tra kết nối:**
   - Database: `psql -U postgres -d computer_sell`
   - Backend: `curl http://localhost:8080/actuator/health`
   - Frontend: `http://localhost:4200`

3. **Kiểm tra files:**
   - Backend: `Computer-sell/src/main/resources/application.yaml`
   - Frontend: `Computer_Sell_FrontEnd/src/app/app.component.ts`

4. **Restart services:**
   - Database: Restart PostgreSQL
   - Backend: Stop & restart Maven
   - Frontend: Stop & restart ng serve

---

**Chúc bạn thành công! 🚀**

