# Test Login - Hướng dẫn kiểm tra ngay

## Đã sửa gì?

### 1. Cookie Options đầy đủ hơn
```typescript
const cookieOptions = {
  path: '/',
  sameSite: 'Lax',
  secure: false
};
this.cookieService.set('username', tokenData.username, { expires: 1, ...cookieOptions });
```

### 2. Backup với localStorage
```typescript
// Lưu vào cả localStorage
localStorage.setItem('username', tokenData.username);
localStorage.setItem('role', role);
localStorage.setItem('userId', String(possibleId));
```

### 3. Fallback khi đọc username
```typescript
getUsername(): string {
  // Try cookie first
  let username = this.cookieService.get('username');
  
  // Fallback to localStorage
  if (!username || username === 'undefined') {
    username = localStorage.getItem('username') || '';
  }
  
  return username;
}
```

## Cách test

### Bước 1: Clear tất cả data cũ
1. Mở DevTools (F12)
2. Application tab > Clear site data
3. Hoặc chạy trong Console:
```javascript
document.cookie.split(";").forEach(c => {
  document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
});
localStorage.clear();
```

### Bước 2: Đăng nhập
1. Vào trang login
2. Nhập username/password
3. Click đăng nhập

### Bước 3: Kiểm tra Console
Bạn sẽ thấy các logs:
```
Saving tokens: {id: "...", username: "your_username", ...}
Username to save: your_username
Saved userId: xxx-xxx-xxx
Verifying cookies after save:
- username: your_username
- role: User
- userId: xxx-xxx-xxx
localStorage username: your_username
Auth state updated - username: your_username, role: User
```

### Bước 4: Kiểm tra Storage
**Cookies** (Application > Cookies > localhost:4200):
- ✅ username = "your_username"
- ✅ role = "User"
- ✅ userId = "uuid"
- ✅ accessToken = "eyJ..."

**LocalStorage** (Application > Local Storage > localhost:4200):
- ✅ username = "your_username"
- ✅ role = "User"
- ✅ userId = "uuid"

### Bước 5: Kiểm tra Header
Sau khi page reload (1.2s), header phải hiển thị:
```
Xin chào 👋
your_username
```

## Nếu vẫn hiển thị "undefined"

### Check 1: Backend response
Trong Network tab, check response của `/auth/access-token`:
```json
{
  "id": "uuid-here",
  "username": "your_username",  // ⚠️ Phải có!
  "accessToken": "...",
  "refreshToken": "...",
  "role": ["[User]"]
}
```

### Check 2: Console errors
Xem có lỗi nào trong console không?

### Check 3: Cookie domain
Nếu đang chạy trên domain khác localhost, cần config:
```typescript
const cookieOptions = {
  path: '/',
  domain: 'your-domain.com', // Thêm domain
  sameSite: 'Lax',
  secure: true // true nếu HTTPS
};
```

## Giải pháp cuối cùng

Nếu cookies vẫn không hoạt động, localStorage sẽ là backup:

1. ✅ Cookies được ưu tiên
2. ✅ Nếu cookies fail → dùng localStorage
3. ✅ Cả 2 đều được clear khi logout

## Test nhanh trong Console

Paste vào Console để test:
```javascript
// Check cookies
console.log('Cookie username:', document.cookie.split('; ').find(row => row.startsWith('username=')));

// Check localStorage
console.log('LocalStorage username:', localStorage.getItem('username'));

// Manual set để test
localStorage.setItem('username', 'test_user');
location.reload();
```

## Kết quả mong đợi

✅ Username hiển thị đúng trên header
✅ Không còn "undefined"
✅ Cookies + localStorage đều có data
✅ Logout xóa sạch cả 2
