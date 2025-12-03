# Sửa lỗi kiểu dữ liệu trong chức năng xem chi tiết người dùng

## Vấn đề
Khi compile backend gặp 2 lỗi:
1. `incompatible types: java.util.List<java.lang.Object> cannot be converted to java.util.List<AddressResponseDTO>`
2. `incompatible types: java.util.UUID cannot be converted to java.lang.Long`

## Nguyên nhân

### Lỗi 1: Type inference với `.toList()`
- Method `.toList()` trong Java Stream API đôi khi không infer đúng kiểu generic
- Compiler không thể xác định kiểu trả về là `List<AddressResponseDTO>`

### Lỗi 2: Kiểu ID không khớp
- `AddressEntity` kế thừa từ `AbstractEntity` có ID kiểu `UUID`
- `AddressResponseDTO` ban đầu định nghĩa ID kiểu `Long`
- Không thể cast UUID sang Long

## Giải pháp

### 1. Sửa AddressResponseDTO.java
**Trước:**
```java
private Long id;
```

**Sau:**
```java
private java.util.UUID id;
```

### 2. Sửa UserServiceImpl.java
**Trước:**
```java
.toList();
```

**Sau:**
```java
.collect(java.util.stream.Collectors.toList());
```

Thay đổi cả 2 chỗ:
- Map addresses
- Map role names

### 3. Sửa user-admin.service.ts (Frontend)
**Trước:**
```typescript
export interface AddressDTO {
  id: number;
  ...
}
```

**Sau:**
```typescript
export interface AddressDTO {
  id: string;  // UUID được serialize thành string trong JSON
  ...
}
```

## Files đã sửa
1. `Computer-sell/src/main/java/com/trong/Computer_sell/DTO/response/User/AddressResponseDTO.java`
2. `Computer-sell/src/main/java/com/trong/Computer_sell/service/impl/UserServiceImpl.java`
3. `Computer_Sell_FrontEnd/src/app/services/user-admin.service.ts`

## Kết quả
- ✅ Backend compile thành công
- ✅ Không còn lỗi type mismatch
- ✅ Frontend interface khớp với backend response
- ✅ Build SUCCESS với Maven

## Ghi chú kỹ thuật

### Về `.toList()` vs `.collect(Collectors.toList())`
- `.toList()` (Java 16+): Trả về immutable list, đôi khi có vấn đề với type inference
- `.collect(Collectors.toList())`: Trả về mutable list, type inference rõ ràng hơn
- Trong trường hợp này, dùng `.collect()` để tránh lỗi compile

### Về UUID trong JSON
- Backend: UUID object
- JSON: String (format: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
- Frontend: string type trong TypeScript

### Database Schema
Bảng `tbl_address`:
```sql
CREATE TABLE "tbl_address" (
  id BIGSERIAL PRIMARY KEY,  -- PostgreSQL BIGSERIAL = Long
  ...
);
```

**Lưu ý:** Database dùng BIGSERIAL (Long) nhưng JPA Entity dùng UUID do kế thừa từ AbstractEntity. Cần kiểm tra lại migration hoặc entity definition nếu có vấn đề về persistence.

## Khuyến nghị
Nếu database thực sự dùng BIGSERIAL cho address ID, cần:
1. Tạo AddressEntity riêng không kế thừa AbstractEntity
2. Định nghĩa ID kiểu Long
3. Hoặc update database schema để dùng UUID

Hiện tại giải pháp trên hoạt động với giả định entity đã được config đúng với UUID.
