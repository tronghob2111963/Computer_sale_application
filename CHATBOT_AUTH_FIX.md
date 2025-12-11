# ✅ Chatbot Auth Fix - User ID Integration

## 🔴 Vấn Đề

```
Failed to load resource: the server responded with a status of 403 ()
http://localhost:8080/api/chatbot/chat?userId=
```

**Nguyên nhân:** User ID không được set, nên API nhận `userId=` (rỗng)

## ✅ Giải Pháp

### Cập Nhật chatbot.component.ts

```typescript
// Thêm AuthService import
import { AuthService } from '../../services/auth.service';

// Constructor
constructor(
    private chatbotService: ChatbotService,
    private authService: AuthService
) { }

// ngOnInit
ngOnInit(): void {
    // Get user ID from auth service
    this.userId = this.authService.getUserIdSafe();
    console.log('✅ Chatbot initialized with userId:', this.userId);
    
    // Initialize with welcome message
    this.addBotMessage('Xin chào! 👋 ...');
}

// sendMessage
sendMessage(): void {
    if (!this.inputMessage.trim()) {
        return;
    }

    // Check if user ID is available
    if (!this.userId) {
        this.addBotMessage('❌ Lỗi: Không thể xác định người dùng. Vui lòng đăng nhập lại.');
        return;
    }

    // ... rest of code
}
```

## 🔧 Cách Hoạt Động

1. **Khi component init:**
   - Gọi `authService.getUserIdSafe()`
   - Lấy user ID từ cookie/localStorage
   - Nếu không có, extract từ JWT token

2. **Khi gửi message:**
   - Kiểm tra user ID có tồn tại
   - Nếu không, hiển thị lỗi
   - Nếu có, gửi API request với user ID

3. **API Request:**
   ```
   POST /api/chatbot/chat?userId=550e8400-e29b-41d4-a716-446655440000
   ```

## 🚀 Test

### 1. Đăng Nhập
- Mở http://localhost:4200
- Đăng nhập với tài khoản
- Kiểm tra console: `✅ Chatbot initialized with userId: ...`

### 2. Gửi Message
- Click chatbot icon
- Gửi tin nhắn
- Kiểm tra network tab: userId được gửi đúng

### 3. Kiểm Tra Console
```
✅ Chatbot initialized with userId: 550e8400-e29b-41d4-a716-446655440000
```

## 📊 Auth Service Methods

| Method | Purpose |
|--------|---------|
| `getUserId()` | Lấy user ID từ cookie/localStorage |
| `getUserIdSafe()` | Lấy user ID, fallback extract từ JWT |
| `getAccessToken()` | Lấy access token |
| `getUsername()` | Lấy username |

## ✅ Checklist

- [x] Import AuthService
- [x] Inject AuthService
- [x] Get user ID in ngOnInit
- [x] Check user ID in sendMessage
- [x] Show error if no user ID
- [x] Log user ID for debugging

## 🎯 Status

**FIXED** ✅

User ID giờ được lấy từ auth service và gửi đúng với API request.

## 📝 Notes

- User ID là UUID string
- Được lưu trong cookie và localStorage
- Fallback extract từ JWT token nếu cần
- Tự động refresh khi đăng nhập

---

**Date:** 2025-12-09  
**Version:** 3.1 (Auth Integration)  

