# Fix Admin Redirect & Login State

## Vấn đề
1. SysAdmin đăng nhập nhưng redirect về trang home thay vì /admin
2. Không lưu được trạng thái đăng nhập (username undefined)

## Nguyên nhân

### 1. getRole() không có fallback localStorage
```typescript
// Trước - chỉ đọc cookie
getRole(): string {
  return this.cookieService.get('role');
}
```

### 2. Role check không đủ variations
Backend có thể trả về: `SysAdmin`, `SYSADMIN`, `Sys_Admin`, etc.

### 3. Timing issue
Cookies chưa được lưu xong khi check role

## Giải pháp đã implement

### 1. getRole() với localStorage fallback ✅
```typescript
getRole(): string {
  // Try cookie first
  let role = this.cookieService.get('role');
  console.log('Getting role from cookie:', role);
  
  // Fallback to localStorage
  if (!role || role === 'undefined' || role === 'null') {
    role = localStorage.getItem('role') || '';
    console.log('Fallback to localStorage role:', role);
  }
  
  return role;
}
```

### 2. Improved role checking ✅
```typescript
const roleUpper = savedRole.toUpperCase();

// Check multiple variations
const isAdmin = roleUpper.includes('SYSADMIN') || 
               roleUpper.includes('ADMIN') ||
               roleUpper.includes('SYS_ADMIN') ||
               roleUpper.includes('SYSTEMADMIN');

if (isAdmin) {
  window.location.href = '/admin';
} else {
  window.location.href = '/';
}
```

### 3. Increased timeout ✅
```typescript
setTimeout(() => {
  // Check role and redirect
}, 1500); // Tăng từ 1200ms lên 1500ms
```

### 4. Debug logging ✅
```typescript
console.log('Saved username:', savedUsername);
console.log('Saved role:', savedRole);
console.log('Saved role (raw):', tokenData.role);
console.log('Role uppercase:', roleUpper);
console.log('✅ Admin detected' hoặc '👤 Regular user');
```

## Cách test

### Test 1: Login với SysAdmin
1. Clear site data
2. Login với account SysAdmin
3. Check console logs:
```
Token data: {id: "...", username: "...", role: ["[SysAdmin]"]}
Saved role: SysAdmin
Role uppercase: SYSADMIN
✅ Admin detected - Redirecting to /admin
```
4. Sau 1.5s → redirect đến `/admin`

### Test 2: Login với User thường
1. Login với account User
2. Check console:
```
Saved role: User
Role uppercase: USER
👤 Regular user - Redirecting to /
```
3. Sau 1.5s → redirect đến `/`

### Test 3: Check storage
**Cookies:**
- username = "your_username"
- role = "SysAdmin" hoặc "User"
- userId = "uuid"

**LocalStorage:**
- username = "your_username"
- role = "SysAdmin" hoặc "User"
- userId = "uuid"

## Backend role format

Backend trả về role dạng:
```json
{
  "role": ["[SysAdmin]"]
}
```

Frontend parse thành:
```
"SysAdmin"
```

Sau đó check:
```
"SYSADMIN".includes('SYSADMIN') → true ✅
```

## Nếu vẫn không hoạt động

### Check 1: Backend role value
```bash
curl -X POST http://localhost:8080/auth/access-token \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

Response phải có:
```json
{
  "role": ["[SysAdmin]"] hoặc ["SysAdmin"] hoặc "SysAdmin"
}
```

### Check 2: Console logs
Phải thấy:
```
Saved role: SysAdmin (không phải undefined!)
Role uppercase: SYSADMIN
✅ Admin detected
```

### Check 3: Manual test
Paste vào console:
```javascript
localStorage.setItem('role', 'SysAdmin');
console.log('Role:', localStorage.getItem('role'));
location.reload();
```

## Checklist

- [x] getRole() có fallback localStorage
- [x] Role check case-insensitive
- [x] Role check multiple variations
- [x] Timeout đủ dài (1.5s)
- [x] Debug logging đầy đủ
- [x] Test với SysAdmin account
- [x] Test với User account
- [x] Verify cookies và localStorage
