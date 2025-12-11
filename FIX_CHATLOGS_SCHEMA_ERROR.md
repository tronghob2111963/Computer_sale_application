# Fix ChatLogs Schema Error

## 🔴 Lỗi

```
ERROR: column "id" cannot be cast automatically to type bigint
Hint: You might need to specify "USING id::bigint".
```

## 🔍 Nguyên Nhân

Hibernate đang cố gắng thay đổi kiểu dữ liệu của cột `id` và `user_id` từ `integer` sang `bigint`, nhưng PostgreSQL không cho phép điều này tự động.

## ✅ Giải Pháp

### Bước 1: Tắt Auto-Update Schema

Cập nhật `Computer-sell/src/main/resources/application.yaml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Bước 2: Xóa và Tạo Lại Bảng

Chạy SQL command sau trong PostgreSQL:

```sql
-- Xóa bảng cũ
DROP TABLE IF EXISTS tbl_chatlogs CASCADE;

-- Tạo bảng mới với schema đúng
CREATE TABLE tbl_chatlogs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT,
    response TEXT,
    timestamp TIMESTAMP,
    CONSTRAINT fk_chatlogs_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE
);

-- Tạo index
CREATE INDEX idx_chatlogs_user_id ON tbl_chatlogs(user_id);
CREATE INDEX idx_chatlogs_timestamp ON tbl_chatlogs(timestamp);
```

### Bước 3: Chạy Application

```bash
cd Computer-sell
mvn spring-boot:run
```

## 📋 Chi Tiết Các Bước

### Step 1: Kết Nối PostgreSQL

**Windows:**
```bash
psql -U postgres
```

**Linux/Mac:**
```bash
psql -U postgres
```

### Step 2: Chọn Database

```sql
\c computer_sell
```

Hoặc tạo database nếu chưa có:

```sql
CREATE DATABASE computer_sell;
\c computer_sell
```

### Step 3: Chạy Migration

Dán toàn bộ SQL từ bước 2 vào PostgreSQL

### Step 4: Verify

```sql
-- Kiểm tra bảng
\dt tbl_chatlogs

-- Kiểm tra schema
\d tbl_chatlogs

-- Kiểm tra index
\di tbl_chatlogs*
```

## 🔧 Alternative: Sử dụng File Migration

### Cách 1: Chạy File SQL

```bash
psql -U postgres -d computer_sell -f migration_fix_chatlogs_schema.sql
```

### Cách 2: Chạy từ pgAdmin

1. Mở pgAdmin
2. Kết nối PostgreSQL
3. Chọn database `computer_sell`
4. Mở Query Tool
5. Dán SQL từ file
6. Chạy

## 🚀 Sau Khi Fix

### 1. Xóa Cache Maven

```bash
cd Computer-sell
mvn clean
```

### 2. Build Lại

```bash
mvn clean install -DskipTests
```

### 3. Chạy Application

```bash
mvn spring-boot:run
```

## ✅ Kiểm Tra

### 1. Kiểm Tra Application Chạy

```
Started ComputerSellApplication in X seconds
```

### 2. Kiểm Tra API

```bash
curl http://localhost:8080/actuator/health
```

### 3. Kiểm Tra Chatbot

```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'
```

## 🐛 Nếu Vẫn Gặp Lỗi

### Lỗi: "database does not exist"

```bash
# Tạo database
psql -U postgres -c "CREATE DATABASE computer_sell;"

# Chạy migration
psql -U postgres -d computer_sell -f Computer_sellDB.sql
```

### Lỗi: "relation does not exist"

```bash
# Kiểm tra bảng
psql -U postgres -d computer_sell -c "\dt"

# Nếu không có, chạy lại migration
psql -U postgres -d computer_sell -f Computer_sellDB.sql
```

### Lỗi: "permission denied"

```bash
# Kiểm tra user
psql -U postgres -d computer_sell -c "\du"

# Cấp quyền
psql -U postgres -d computer_sell -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;"
```

## 📝 Configuration Đúng

### application.yaml

```yaml
server:
  port: 8080

spring:
  application:
    name: Computer-sell
  
  datasource:
    url: jdbc:postgresql://localhost:5432/computer_sell
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true

gemini:
  api:
    key: "${GEMINI_API_KEY}"
```

## 🔄 Quy Trình Hoàn Chỉnh

1. **Xóa bảng cũ**
   ```sql
   DROP TABLE IF EXISTS tbl_chatlogs CASCADE;
   ```

2. **Tạo bảng mới**
   ```sql
   CREATE TABLE tbl_chatlogs (
       id BIGSERIAL PRIMARY KEY,
       user_id BIGINT NOT NULL,
       message TEXT,
       response TEXT,
       timestamp TIMESTAMP,
       CONSTRAINT fk_chatlogs_user FOREIGN KEY (user_id) REFERENCES tbl_users(id)
   );
   ```

3. **Cập nhật application.yaml**
   ```yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: validate
   ```

4. **Build lại**
   ```bash
   mvn clean install -DskipTests
   ```

5. **Chạy application**
   ```bash
   mvn spring-boot:run
   ```

## ✅ Checklist

- [ ] Xóa bảng tbl_chatlogs cũ
- [ ] Tạo bảng tbl_chatlogs mới
- [ ] Cập nhật application.yaml
- [ ] Chạy mvn clean
- [ ] Build lại project
- [ ] Chạy application
- [ ] Test API
- [ ] Kiểm tra logs

## 📞 Support

Nếu vẫn gặp vấn đề:
1. Kiểm tra PostgreSQL chạy
2. Kiểm tra database tồn tại
3. Kiểm tra user có quyền
4. Xem logs chi tiết
5. Kiểm tra application.yaml

---

**Sau khi fix, application sẽ chạy bình thường!**
