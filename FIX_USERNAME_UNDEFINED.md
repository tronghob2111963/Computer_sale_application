# Fix Username "undefined" trên Header

## Vấn đề
Header hiển thị "undefined" thay vì username sau khi đăng nhập.

## Cách kiểm tra

### 1. Mở Browser Console (F12)
Sau khi đăng nhập, check các logs:

```
Saving tokens: {id: "...", username: "your_username", accessToken: "...", ...}
Saved userId: xxx-xxx-xxx
Auth state updated - username: your_username, role: User
Getting username from cookie: your_username (hoặc undefined)
Updated auth state - username: your_username, isLoggedIn: true
```

### 2. Kiểm tra Cookies
DevTools > Application > Cookies > localhost:4200

Phải có:
- ✅ `username` = "your_username"
- ✅ `accessToken` = "eyJhbGc..."
- ✅ `role` = "User"
- ✅ `userId` = "uuid"

### 3. Nếu cookie `username` = undefined hoặc không tồn tại

**Nguyên nhân**: Backend không trả về field `username` trong response

**Giải pháp**: Kiểm tra backend response format

## Giải pháp nhanh

### Option 1: Force reload sau login (Recommended)

Sửa `login.component.ts`:

```typescript
onSubmit(): void {
  this.authService.login(payload).subscribe({
    next: (res) => {
      this.authService.saveTokens(res);
      this.showNotification('Đăng nhập thành công 🎉', 'success');

      setTimeout(() => {
        const role = this.authService.getRole().toUpperCase();
        
        // ✅ Force reload để cookies được đọc lại
        if (role.includes('SYSADMIN') || role.includes('ADMIN')) {
          window.location.href = '/admin';
        } else {
          window.location.href = '/';
        }
      }, 1200);
    }
  });
}
```

### Option 2: Manual trigger auth state

Sửa `header-layout.component.ts`:

```typescript
ngOnInit(): void {
  // ... existing code ...
  
  // ✅ Force check every 500ms for 3 seconds after init
  let attempts = 0;
  const checkInterval = setInterval(() => {
    attempts++;
    this.updateAuthState();
    
    if (this.isLoggedIn || attempts > 6) {
      clearInterval(checkInterval);
    }
  }, 500);
}
```

### Option 3: Sử dụng localStorage thay vì cookies

Sửa `auth.service.ts`:

```typescript
saveTokens(tokenData: any): void {
  // Save to both cookie and localStorage
  this.cookieService.set('username', tokenData.username, 1, '/');
  localStorage.setItem('username', tokenData.username);
  
  // ... rest of code
}

getUsername(): string {
  // Try cookie first, fallback to localStorage
  const cookieUsername = this.cookieService.get('username');
  if (cookieUsername && cookieUsername !== 'undefined') {
    return cookieUsername;
  }
  return localStorage.getItem('username') || '';
}
```

## Kiểm tra Backend Response

Backend phải trả về đúng format:

```json
{
  "id": "uuid-here",
  "username": "your_username",  // ⚠️ Phải có field này!
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "role": ["User"] hoặc "User"
}
```

Nếu backend không trả về `username`, cần sửa `AuthenticationService.java`:

```java
public TokenResponse getAccessToken(SignInRequest request) {
    // ... authentication logic ...
    
    return TokenResponse.builder()
        .id(user.getId())
        .username(user.getUsername())  // ✅ Phải có
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .role(roles)
        .build();
}
```

## Test lại

1. Clear cookies: DevTools > Application > Clear site data
2. Đăng nhập lại
3. Check console logs
4. Check cookies
5. Refresh page và xem username có hiển thị không

## Nếu vẫn không hoạt động

Thử clear cache và hard reload:
- Windows: `Ctrl + Shift + R`
- Mac: `Cmd + Shift + R`

Hoặc mở Incognito/Private window để test.
