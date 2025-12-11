# ⚡ Quick Fix - ChatLogs Schema Error

## 🔴 Lỗi Gặp Phải

```
ERROR: column "id" cannot be cast automatically to type bigint
```

## ✅ Fix Nhanh (3 Bước)

### Bước 1: Mở PostgreSQL

**Windows:**
```bash
psql -U postgres
```

**Linux/Mac:**
```bash
psql -U postgres
```

### Bước 2: Chạy SQL

```sql
-- Kết nối database
\c computer_sell

-- Xóa bảng cũ
DROP TABLE IF EXISTS tbl_chatlogs CASCADE;

-- Tạo bảng mới
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

-- Thoát
\q
```

### Bước 3: Chạy Application

```bash
cd Computer-sell
mvn clean install -DskipTests
mvn spring-boot:run
```

## ✅ Done!

Application sẽ chạy bình thường.

---

**Nếu vẫn gặp lỗi, xem: FIX_CHATLOGS_SCHEMA_ERROR.md**
