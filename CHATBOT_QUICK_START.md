# Chatbot Quick Start

## 🚀 Bắt Đầu Nhanh (5 Phút)

### 1️⃣ Sửa Database (1 phút)

```bash
psql -U postgres -d computer_sell
```

Dán SQL này:
```sql
DROP TABLE IF EXISTS tbl_chatlogs CASCADE;

CREATE TABLE tbl_chatlogs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT,
    response TEXT,
    timestamp TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_chatlogs_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE
);

CREATE INDEX idx_chatlogs_user_id ON tbl_chatlogs(user_id);
CREATE INDEX idx_chatlogs_timestamp ON tbl_chatlogs(timestamp DESC);
```

### 2️⃣ Cập Nhật Backend (1 phút)

**File: `Computer-sell/src/main/resources/application.yaml`**

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Thay create-drop thành validate
```

### 3️⃣ Build Backend (2 phút)

```bash
cd Computer-sell
mvn clean install -DskipTests
mvn spring-boot:run
```

### 4️⃣ Run Frontend (1 phút)

```bash
cd Computer_Sell_FrontEnd
ng serve
```

### 5️⃣ Tích Hợp Chatbot

**File: `Computer_Sell_FrontEnd/src/app/app.component.ts`**

```typescript
import { ChatbotComponent } from './components/chatbot/chatbot.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ChatbotComponent, /* ... */],
  // ...
})
export class AppComponent {}
```

**File: `Computer_Sell_FrontEnd/src/app/app.component.html`**

```html
<router-outlet></router-outlet>
<app-chatbot></app-chatbot>
```

## ✅ Xong!

Mở `http://localhost:4200` và nhấp vào nút chatbot ở góc phải dưới.

## 🧪 Test

```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'
```

## 📁 Files Được Tạo

- ✅ `migration_fix_chatlogs_bigint.sql` - Migration database
- ✅ `Computer-sell/src/main/java/com/trong/Computer_sell/config/CorsConfig.java` - CORS config
- ✅ `Computer-sell/src/main/java/com/trong/Computer_sell/config/RestTemplateConfig.java` - HTTP config
- ✅ `Computer_Sell_FrontEnd/src/app/services/chatbot.service.ts` - Chatbot service
- ✅ `Computer_Sell_FrontEnd/src/app/components/chatbot/chatbot.component.ts` - Chatbot component
- ✅ `Computer_Sell_FrontEnd/src/app/components/chatbot/chatbot.component.html` - Chatbot template
- ✅ `Computer_Sell_FrontEnd/src/app/components/chatbot/chatbot.component.scss` - Chatbot styles

## 🐛 Nếu Gặp Lỗi

1. **Database error:** Chạy lại migration SQL
2. **CORS error:** Restart backend
3. **API 404:** Kiểm tra backend chạy trên port 8080
4. **Chatbot không hiển thị:** Kiểm tra import trong app.component.ts

## 📖 Chi Tiết

Xem `CHATBOT_COMPLETE_SETUP.md` để hướng dẫn chi tiết.

---

**Hoàn thành! 🎉**

