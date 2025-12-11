# Chatbot Complete Fix & Implementation

## 🔴 Vấn Đề Hiện Tại

```
ERROR: column "id" cannot be cast automatically to type bigint
```

## ✅ Giải Pháp Hoàn Chỉnh

### Bước 1: Sửa Database Schema

**Chạy SQL migration:**

```bash
# Kết nối PostgreSQL
psql -U postgres -d computer_sell -f migration_fix_chatlogs_bigint.sql
```

**Hoặc chạy trực tiếp trong pgAdmin:**

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

-- Create indexes
CREATE INDEX idx_chatlogs_user_id ON tbl_chatlogs(user_id);
CREATE INDEX idx_chatlogs_timestamp ON tbl_chatlogs(timestamp DESC);
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

# Clean build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

### Bước 4: Tạo Frontend Chatbot Component

**Tạo service:**
```bash
ng generate service services/chatbot
```

**Tạo component:**
```bash
ng generate component components/chatbot
```

### Bước 5: Test API

```bash
# Test chatbot endpoint
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'
```

## 📋 Checklist

- [ ] Chạy migration SQL
- [ ] Cập nhật application.yaml
- [ ] Build backend
- [ ] Run backend
- [ ] Tạo frontend service
- [ ] Tạo frontend component
- [ ] Test API
- [ ] Test UI

## 🚀 Sau Khi Fix

Application sẽ chạy bình thường mà không có lỗi schema!

