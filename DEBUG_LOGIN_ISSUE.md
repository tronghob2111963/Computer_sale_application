# Debug Login Issue - Không ghi nhận đăng nhập

## Vấn đề
Sau khi đăng nhập thành công, header vẫn hiển thị "Tài khoản" thay vì tên người dùng.

## Nguyên nhân có thể
1. Cookie không được lưu đúng cách
2. AuthState không được cập nhật
3. Header component không re-render sau khi login
4. Backend không trả về đúng format dữ liệu

## Đã sửa

### 1. AuthService - Thêm debug logging
```typescript
saveTokens(tokenData: any): void {
  console.log('Saving tokens:', tokenData); // ✅ Debug log
  // ... save cookies
  console.log('Saved userId:', possibleId); // ✅ Debug log
  console.log('Auth state updated - username:', tokenData.username, 'role:', role); // ✅ Debug log
}
```

### 2. HeaderLayoutComponent - Force update auth state
```typescript
ngOnInit(): void {
  // ✅ Initial check
  this.updateAuthState();
  
  // ✅ Re-check on navigation
  this.router.events.subscribe(e => { 
    if (e instanceof NavigationEnd) { 
      this.updateAuthState();
    } 
  });
  
  // ✅ Subscribe to auth state changes
  this.sub = this.authState.username$.subscribe(name => {
    console.log('Auth state changed - username:', name);
    this.username = name || this.authService.getUsername();
    this.isLoggedIn = !!this.username;
  });
}

private updateAuthState(): void {
  this.username = this.authService.getUsername();
  this.isLoggedIn = !!this.username;
  console.log('Updated auth state - username:', this.username);
}
```

## Cách kiểm tra

### 1. Mở Browser Console (F12)
Khi đăng nhập, bạn sẽ thấy các log:
```
Saving tokens: {accessToken: "...", username: "...", role: "..."}
Saved userId: xxx-xxx-xxx
Auth state updated - username: your_username, role: User
Auth state changed - username: your_username
Updated auth state - username: your_username, isLoggedIn: true
```

### 2. Kiểm tra Cookies
Trong DevTools > Application > Cookies > localhost:4200
Phải có:
- ✅ accessToken
- ✅ refreshToken
- ✅ username
- ✅ role
- ✅ userId (nếu backend trả về)

### 3. Kiểm tra Backend Response
Trong Network tab, check response của `/auth/access-token`:
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "username": "your_username",
  "role": ["User"] hoặc "User",
  "userId": "uuid-here" // ⚠️ Quan trọng!
}
```

## Nếu vẫn không hoạt động

### Kiểm tra 1: Backend có trả về userId không?
Nếu console log hiển thị:
```
No userId found in token data
```
→ Backend cần thêm userId vào response

### Kiểm tra 2: Cookie có được set không?
Nếu không thấy cookies trong DevTools:
→ Có thể do SameSite policy hoặc CORS

### Kiểm tra 3: AuthState có được trigger không?
Nếu không thấy log "Auth state changed":
→ BehaviorSubject có thể không emit

## Giải pháp tạm thời
Nếu vẫn không hoạt động, thêm force reload sau login:

```typescript
// Trong login.component.ts
onSubmit(): void {
  this.authService.login(payload).subscribe({
    next: (res) => {
      this.authService.saveTokens(res);
      
      // ✅ Force reload page
      setTimeout(() => {
        window.location.href = '/';
      }, 1000);
    }
  });
}
```

## Kiểm tra Backend Response Format

Hãy check AuthenticationController để đảm bảo response có format:
```java
return ResponseData.builder()
    .status(200)
    .message("Login successful")
    .data(Map.of(
        "accessToken", accessToken,
        "refreshToken", refreshToken,
        "username", user.getUsername(),
        "role", user.getRoles(),
        "userId", user.getId() // ⚠️ Cần có field này!
    ))
    .build();
```
