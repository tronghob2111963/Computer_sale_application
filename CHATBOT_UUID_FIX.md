# Chatbot UUID Fix - Updated Schema

## 🔧 Thay Đổi

Tôi đã cập nhật schema để sử dụng UUID thay vì BIGINT, phù hợp với bảng `tbl_users` của bạn.

## 📋 Files Được Cập Nhật

### Backend

1. **ChatLog.java** - Entity
   - Thay `Long id` → `UUID id`
   - Thay `Long userId` → `UUID userId`
   - Thêm `@UuidGenerator` annotation

2. **ChatBotService.java** - Interface
   - Thay `Long userId` → `String userId`

3. **ChatBotServiceImpl.java** - Implementation
   - Thay `Long userId` → `String userId`
   - Convert String to UUID: `java.util.UUID.fromString(userId)`

4. **ChatBotController.java** - Controller
   - Thay `@RequestParam Long userId` → `@RequestParam String userId`

### Frontend

1. **chatbot.service.ts** - Service
   - Thay `userId: number` → `userId: string`

2. **chatbot.component.ts** - Component
   - Thay `userId: number = 1` → `userId: string = ''`
   - Cập nhật `setUserId()` method

### Database

1. **migration_fix_chatlogs_bigint.sql** - Migration
   - Thay `BIGSERIAL` → `UUID DEFAULT gen_random_uuid()`
   - Thay `BIGINT` → `UUID`
   - Foreign key tới `tbl_users(id)` (UUID)

## 🚀 Cách Sử Dụng

### Bước 1: Chạy Migration SQL

```bash
psql -U postgres -d computer_sell
```

Dán SQL này:

```sql
DROP TABLE IF EXISTS tbl_chatlogs CASCADE;

CREATE TABLE tbl_chatlogs (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    message TEXT,
    response TEXT,
    timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_chatlogs_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE
);

CREATE INDEX idx_chatlogs_user_id ON tbl_chatlogs(user_id);
CREATE INDEX idx_chatlogs_timestamp ON tbl_chatlogs(timestamp DESC);
```

### Bước 2: Build Backend

```bash
cd Computer-sell
mvn clean install -DskipTests
mvn spring-boot:run
```

### Bước 3: Run Frontend

```bash
cd Computer_Sell_FrontEnd
ng serve
```

### Bước 4: Test

```bash
# Test with UUID (example)
curl -X POST http://localhost:8080/api/chatbot/chat?userId=550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'
```

## 📊 Schema Comparison

### Cũ (BIGINT)
```sql
CREATE TABLE tbl_chatlogs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ...
);
```

### Mới (UUID)
```sql
CREATE TABLE tbl_chatlogs (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chatlogs_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE
);
```

## ✅ Checklist

- [x] Cập nhật ChatLog entity
- [x] Cập nhật ChatBotService interface
- [x] Cập nhật ChatBotServiceImpl
- [x] Cập nhật ChatBotController
- [x] Cập nhật frontend service
- [x] Cập nhật frontend component
- [x] Cập nhật migration SQL

## 🎯 Lợi Ích

✅ UUID khớp với schema `tbl_users`  
✅ Tự động generate UUID  
✅ Tốt hơn BIGINT cho distributed systems  
✅ Foreign key constraint hoạt động đúng  

## 📝 Ghi Chú

- User ID giờ là UUID string khi gửi từ frontend
- Backend tự động convert string → UUID
- Database tự động generate UUID cho id
- Foreign key constraint bảo vệ dữ liệu

---

**Status: READY** ✅

