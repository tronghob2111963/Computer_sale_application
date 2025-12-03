# Chức năng xem chi tiết người dùng

## Tổng quan
Đã thêm chức năng xem chi tiết thông tin người dùng bao gồm cả địa chỉ trong trang quản lý người dùng.

## Các thay đổi

### Backend (Java Spring Boot)

#### 1. AddressResponseDTO.java (Mới)
- DTO để trả về thông tin địa chỉ
- Các trường: id, apartmentNumber, streetNumber, ward, city, addressType
- Method `getFullAddress()`: Ghép địa chỉ đầy đủ thành chuỗi

#### 2. UserDetailResponseDTO.java (Mới)
- DTO mở rộng để trả về thông tin chi tiết người dùng
- Bao gồm tất cả thông tin cơ bản + addresses + roles
- Các trường bổ sung:
  - `userType`: Loại tài khoản (CUSTOMER, STAFF, ADMIN)
  - `status`: Trạng thái (ACTIVE, INACTIVE)
  - `createdAt`, `updatedAt`: Thời gian tạo và cập nhật
  - `addresses`: Danh sách địa chỉ
  - `roles`: Danh sách vai trò

#### 3. UserService.java & UserServiceImpl.java
- Thêm method `findDetailById(UUID id)`: Lấy thông tin chi tiết người dùng
- Logic:
  - Lấy thông tin user từ database
  - Map addresses từ entity sang DTO
  - Lấy danh sách role names
  - Trả về UserDetailResponseDTO đầy đủ

#### 4. UserController.java
- Thêm endpoint mới: `GET /user/detail/{id}`
- Authorization: `@PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff')")`
- Response: UserDetailResponseDTO với tất cả thông tin

### Frontend (Angular)

#### 1. user-admin.service.ts
- Thêm interface `AddressDTO`: Định nghĩa cấu trúc địa chỉ
- Thêm interface `UserDetailDTO`: Định nghĩa cấu trúc user detail
- Thêm method `findDetailById(id: string)`: Gọi API lấy chi tiết user

#### 2. admin-users.component.ts
- Thêm biến `showDetailModal: boolean`: Điều khiển hiển thị modal
- Thêm biến `userDetail: any`: Lưu thông tin chi tiết user
- Thêm method `openDetail(u: UserDTO)`: Mở modal xem chi tiết
- Thêm method `closeDetail()`: Đóng modal
- Thêm method `getAddressTypeLabel(type: string)`: Chuyển đổi label địa chỉ
- Thêm method `getFullAddress(addr: any)`: Ghép địa chỉ đầy đủ

#### 3. admin-users.component.html
- **Thêm button "Chi tiết"** (màu xanh lá) bên cạnh button "Sửa"
- **Modal xem chi tiết** với các phần:
  - **Header**: Gradient xanh với tiêu đề và nút đóng
  - **Avatar & Basic Info**: 
    - Avatar tròn lớn với chữ cái đầu
    - Tên đầy đủ và username
    - Badges cho roles và status
  - **Thông tin cá nhân**:
    - Email, SĐT, Giới tính, Ngày sinh
    - Loại tài khoản, Ngày tạo
    - Layout grid 2 cột với background màu xám nhạt
  - **Địa chỉ**:
    - Hiển thị tất cả địa chỉ của user
    - Badge cho loại địa chỉ (Nhà riêng/Công ty)
    - Địa chỉ đầy đủ và chi tiết từng phần
    - Empty state khi không có địa chỉ
  - **Footer**: Buttons "Đóng" và "Chỉnh sửa"

## Cách sử dụng

### Xem chi tiết người dùng
1. Vào trang "Quản lý người dùng"
2. Click button **"Chi tiết"** (màu xanh lá) ở cột "Hành động"
3. Modal sẽ hiển thị với đầy đủ thông tin:
   - Thông tin cá nhân
   - Vai trò (roles)
   - Trạng thái tài khoản
   - Danh sách địa chỉ (nếu có)
4. Click "Chỉnh sửa" để chuyển sang chế độ edit
5. Click "Đóng" để đóng modal

### API Endpoint
```
GET /user/detail/{userId}
Authorization: Bearer {token}
```

### Response Format
```json
{
  "code": 200,
  "message": "User detail found successfully",
  "data": {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "gender": "MALE|FEMALE|OTHER",
    "dateOfBirth": "2000-01-01",
    "phoneNumber": "string",
    "userType": "CUSTOMER|STAFF|ADMIN",
    "status": "ACTIVE|INACTIVE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00",
    "addresses": [
      {
        "id": 1,
        "apartmentNumber": "123",
        "streetNumber": "Nguyen Trai",
        "ward": "Phuong 1",
        "city": "Ho Chi Minh",
        "addressType": "HOME|WORK"
      }
    ],
    "roles": ["User", "Admin"]
  }
}
```

## Tính năng nổi bật

### UI/UX
- **Modal đẹp mắt**: Gradient header, rounded corners, shadows
- **Avatar động**: Hiển thị chữ cái đầu của username
- **Color coding**: 
  - Roles: Purple badges
  - Status: Green (Active) / Red (Inactive)
  - Address type: Blue badges
- **Responsive**: Layout grid tự động điều chỉnh
- **Empty states**: Icon và message khi không có dữ liệu
- **Smooth animations**: Fade in/out cho modal

### Chức năng
- Xem đầy đủ thông tin user không cần edit
- Hiển thị tất cả địa chỉ với chi tiết
- Chuyển nhanh sang chế độ edit từ modal detail
- Loading state khi fetch data
- Error handling với thông báo rõ ràng

## Database Schema
Sử dụng bảng:
- `tbl_users`: Thông tin người dùng
- `tbl_address`: Địa chỉ của người dùng
- `tbl_userrole`: Liên kết user với role
- `tbl_role`: Danh sách vai trò

## Ghi chú
- Chỉ SysAdmin, Admin, Staff mới có quyền xem chi tiết
- Địa chỉ được phân loại: HOME (Nhà riêng) và WORK (Công ty)
- Modal có thể scroll khi nội dung dài
- Button "Chỉnh sửa" trong modal sẽ đóng modal detail và mở modal edit
