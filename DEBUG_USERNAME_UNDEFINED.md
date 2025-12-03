# Debug: Username vẫn undefined

## Vấn đề hiện tại
Console logs cho thấy:
```
Getting username from cookie: undefined
Fallback to localStorage username: undefined
Updated auth state - username: undefined isLoggedIn: true
```

Cả cookie VÀ localStorage đều undefined → username chưa được lưu!

## Nguyên nhân có thể

### 1. Backend response không đúng format
Backend có thể trả về:
```json
{
  "data": {
    "id": "...",
    "username": "...",
    ...
  }
}
```

Thay vì:
```json
{
  "id": "...",
  "username": "...",
  ...
}
```

### 2. Response bị wrap trong ResponseData
Nếu backend dùng ResponseData wrapper, cần unwrap:
```typescript
// Thay vì
this.authService.saveTokens(res);

// Phải là
this.authService.saveTokens(res.data || res);
```

## Cách kiểm tra

### Bước 1: Check backend response
1. Mở DevTools (F12)
2. Vào tab Network
3. Đăng nhập
4. Tìm request `/auth/access-token`
5. Xem Response

**Expected response:**
```json
{
  "id": "uuid-here",
  "username": "your_username",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "role": ["[User]"]
}
```

**Nếu response có wrapper:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "uuid-here",
    "username": "your_username",
    ...
  }
}
```

### Bước 2: Check console logs khi login
Sau khi click "Đăng nhập", phải thấy:
```
Login response: {id: "...", username: "...", ...}
Response username: your_username
Saving tokens: {id: "...", username: "...", ...}
Username to save: your_username
```

**Nếu thấy:**
```
Login response: {status: 200, data: {...}}
Response username: undefined
```
→ Response bị wrap, cần unwrap!

## Giải pháp

### Nếu response có wrapper (data field)

Sửa `login.component.ts`:
```typescript
next: (res) => {
  this.isLoading = false;
  console.log('Login response:', res);
  
  // ✅ Unwrap response if needed
  const tokenData = res.data || res;
  console.log('Token data:', tokenData);
  console.log('Username:', tokenData.username);
  
  this.authService.saveTokens(tokenData);
  
  this.showNotification('Đăng nhập thành công 🎉', 'success');
  // ...
}
```

### Nếu backend không trả về username

Cần sửa backend `AuthenticationServiceImpl.java`:
```java
return TokenResponse.builder()
    .id(user.getId())
    .username(user.getUsername())  // ⚠️ Đảm bảo có field này!
    .accessToken(accessToken)
    .refreshToken(refreshToken)
    .role(authorities)
    .build();
```

## Test nhanh

### Test 1: Manual set username
Paste vào Console:
```javascript
localStorage.setItem('username', 'test_user');
location.reload();
```

Nếu header hiển thị "test_user" → Code frontend OK, vấn đề ở backend response!

### Test 2: Check backend directly
```bash
curl -X POST http://localhost:8080/auth/access-token \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "password": "your_password",
    "platform": "web",
    "version": "1.0.0",
    "deviceToken": "test"
  }'
```

Response phải có field `username`!

## Checklist

- [ ] Check Network tab - response có field `username`?
- [ ] Check Console - có log "Saving tokens"?
- [ ] Check Console - "Username to save" có giá trị?
- [ ] Check Application > Cookies - có cookie `username`?
- [ ] Check Application > Local Storage - có `username`?
- [ ] Test manual set username - có hoạt động?

## Nếu tất cả đều OK nhưng vẫn undefined

Có thể do timing issue. Thử:

```typescript
// login.component.ts
setTimeout(() => {
  const role = this.authService.getRole().toUpperCase();
  
  // ✅ Verify before redirect
  console.log('Before redirect - username:', this.authService.getUsername());
  console.log('Cookie:', document.cookie);
  console.log('LocalStorage:', localStorage.getItem('username'));
  
  if (role.includes('SYSADMIN') || role.includes('ADMIN')) {
    window.location.href = '/admin';
  } else {
    window.location.href = '/';
  }
}, 1200);
```
