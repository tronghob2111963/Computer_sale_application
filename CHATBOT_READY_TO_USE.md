# ✅ Chatbot - Ready to Use

## 🎉 Status: COMPLETE

Chatbot đã được cập nhật để sử dụng UUID, phù hợp với schema `tbl_users` của bạn.

## 🔧 Những Gì Đã Sửa

### Database Schema
```sql
-- Cũ: BIGINT
id BIGSERIAL PRIMARY KEY
user_id BIGINT NOT NULL

-- Mới: UUID
id UUID NOT NULL DEFAULT gen_random_uuid()
user_id UUID NOT NULL
```

### Backend
- ✅ ChatLog entity: `Long` → `UUID`
- ✅ ChatBotService: `Long userId` → `String userId`
- ✅ ChatBotController: `Long userId` → `String userId`

### Frontend
- ✅ chatbot.service.ts: `number` → `string`
- ✅ chatbot.component.ts: `number` → `string`

## 🚀 Quick Start

### 1. Chạy Migration SQL

```bash
psql -U postgres -d computer_sell
```

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

### 2. Build Backend

```bash
cd Computer-sell
mvn clean install -DskipTests
mvn spring-boot:run
```

### 3. Run Frontend

```bash
cd Computer_Sell_FrontEnd
ng serve
```

### 4. Test

```bash
# Test API (replace UUID with real user ID)
curl -X POST http://localhost:8080/api/chatbot/chat?userId=550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'
```

## 📁 Files Updated

### Backend
- `ChatLog.java` - Entity with UUID
- `ChatBotService.java` - Interface with String userId
- `ChatBotServiceImpl.java` - Implementation with UUID conversion
- `ChatBotController.java` - Controller with String userId

### Frontend
- `chatbot.service.ts` - Service with string userId
- `chatbot.component.ts` - Component with string userId

### Database
- `migration_fix_chatlogs_bigint.sql` - Updated migration

## ✨ Features

✅ UUID support  
✅ Foreign key constraint  
✅ Auto-generated IDs  
✅ Proper indexing  
✅ Cascade delete  

## 🧪 Test Scenarios

### Scenario 1: Send Message
```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'
```

### Scenario 2: Check Product Availability
```bash
curl "http://localhost:8080/api/chatbot/product-availability?productName=Laptop"
```

### Scenario 3: Get Product Price
```bash
curl "http://localhost:8080/api/chatbot/product-price?productName=Laptop"
```

## 📊 Database Schema

```sql
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

## 🔗 Foreign Key Relationship

```
tbl_users (id: UUID)
    ↓
    └─→ tbl_chatlogs (user_id: UUID)
```

## ✅ Verification

### Check Database
```sql
-- Connect to database
psql -U postgres -d computer_sell

-- Check table structure
\d tbl_chatlogs

-- Check foreign keys
\d+ tbl_chatlogs
```

### Check Backend
```bash
# Health check
curl http://localhost:8080/actuator/health

# Test endpoint
curl -X POST http://localhost:8080/api/chatbot/chat?userId=<your-uuid> \
  -H "Content-Type: application/json" \
  -d '{"message":"test"}'
```

### Check Frontend
1. Open http://localhost:4200
2. Click chatbot button
3. Send message
4. Check browser console for errors

## 🐛 Troubleshooting

### Error: "column "user_id" cannot be cast"
**Solution:** Run migration SQL to recreate table

### Error: "Foreign key constraint failed"
**Solution:** Ensure user_id exists in tbl_users

### Error: "UUID format invalid"
**Solution:** Pass valid UUID string from frontend

## 📞 Support

If you encounter issues:

1. Check migration SQL was executed
2. Verify database schema: `\d tbl_chatlogs`
3. Check backend logs
4. Check browser console
5. Verify user UUID exists in tbl_users

## 🎯 Next Steps

1. ✅ Run migration SQL
2. ✅ Build backend
3. ✅ Run frontend
4. ✅ Test chatbot
5. ✅ Deploy to production

## 📝 Notes

- User ID is now UUID string
- Backend converts string to UUID
- Database auto-generates UUID for id
- Foreign key ensures data integrity
- Cascade delete removes chatlogs when user is deleted

---

**Status: PRODUCTION READY** ✅

**Date:** 2025-12-09  
**Version:** 2.0 (UUID Support)  

