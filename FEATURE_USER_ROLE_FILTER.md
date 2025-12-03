# Chức năng lọc người dùng theo Role

## Tổng quan
Đã thêm chức năng lọc người dùng theo vai trò (Role) trong trang quản lý người dùng và cải thiện giao diện người dùng.

## Các thay đổi

### Backend (Java Spring Boot)

#### 1. UserRepository.java
- Thêm method `searchUserByKeywordAndRole()`: Tìm kiếm người dùng theo keyword và roleId
- Thêm method `findAllByRoleId()`: Lấy tất cả người dùng theo roleId

#### 2. UserService.java & UserServiceImpl.java
- Cập nhật method `findAll()` để nhận thêm tham số `roleId`
- Logic lọc:
  - Nếu có `roleId`: Lọc theo role (có hoặc không có keyword)
  - Nếu chỉ có `keyword`: Tìm kiếm theo keyword
  - Nếu không có gì: Lấy tất cả

#### 3. UserController.java
- Thêm tham số `@RequestParam(required = false) Integer roleId` vào endpoint `/user/list`

### Frontend (Angular)

#### 1. user-admin.service.ts
- Thêm `roleId?: number` vào interface `UserListParams`
- Cập nhật method `buildParams()` để gửi roleId lên server

#### 2. admin-users.component.ts
- Thêm biến `roleId: number | null = null`
- Thêm danh sách roles:
  ```typescript
  roles = [
    { id: null, name: 'Tất cả' },
    { id: 1, name: 'SysAdmin' },
    { id: 2, name: 'Admin' },
    { id: 3, name: 'Staff' },
    { id: 4, name: 'User' }
  ]
  ```
- Cập nhật method `load()` để gửi roleId khi gọi API

#### 3. admin-users.component.html
- **Header mới**: Tiêu đề lớn hơn với mô tả
- **Filter Section**: 
  - Dropdown lọc theo vai trò
  - Tìm kiếm nâng cao
  - Sắp xếp và phân trang
- **Table cải tiến**:
  - Avatar tròn với chữ cái đầu
  - Badge màu sắc cho giới tính
  - Hover effects
  - Icon và button đẹp hơn
- **Loading state**: Spinner animation
- **Empty state**: Icon và message khi không có dữ liệu
- **Pagination**: Buttons với disable state

#### 4. admin-users.component.scss
- Animation cho loading spinner
- Hover effects cho table rows
- Custom scrollbar
- Button ripple effect
- Modal backdrop animation

## Cách sử dụng

### Lọc theo Role
1. Mở trang "Quản lý người dùng"
2. Chọn vai trò từ dropdown "Vai trò":
   - **Tất cả**: Hiển thị tất cả người dùng
   - **SysAdmin**: Chỉ hiển thị SysAdmin (roleId = 1)
   - **Admin**: Chỉ hiển thị Admin (roleId = 2)
   - **Staff**: Chỉ hiển thị Staff (roleId = 3)
   - **User**: Chỉ hiển thị User/Customer (roleId = 4)
3. Kết hợp với tìm kiếm keyword để lọc chính xác hơn

### API Endpoint
```
GET /user/list?keyword=&page=0&size=10&sortBy=createdAt:desc&roleId=4
```

## Database Schema
Dựa trên bảng `tbl_role`:
- id = 1: SysAdmin
- id = 2: Admin
- id = 3: Staff
- id = 4: User (Customer)

## Ghi chú
- Chức năng lọc hoạt động với cả tìm kiếm và sắp xếp
- UI responsive và tương thích với mobile
- Có animation và transition mượt mà
- Pagination được cải thiện với disable state
