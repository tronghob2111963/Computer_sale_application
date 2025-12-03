# PC Builder Feature - Hướng dẫn sử dụng

## Tổng quan
Chức năng PC Builder cho phép người dùng tự xây dựng cấu hình máy tính của riêng mình bằng cách chọn các linh kiện từ các danh mục khác nhau.

## Backend API Endpoints

### 1. Tạo Build mới
```
POST /builds/create
Body: {
  "userId": "uuid",
  "name": "Tên cấu hình"
}
```

### 2. Thêm sản phẩm vào Build
```
POST /builds/{buildId}/add-product?productId={productId}&quantity={quantity}
```

### 3. Xóa sản phẩm khỏi Build
```
DELETE /builds/{buildId}/remove-product/{productId}
```

### 4. Cập nhật số lượng sản phẩm
```
PUT /builds/{buildId}/update-quantity?productId={productId}&quantity={quantity}
```

### 5. Lấy danh sách Build của user
```
GET /builds/user/{userId}
```

### 6. Lấy chi tiết Build
```
GET /builds/{buildId}
```

### 7. Xóa Build
```
DELETE /builds/{buildId}
```

## Frontend Components

### PC Builder Component
- **Path**: `/pc-builder`
- **Component**: `PcBuilderComponent`
- **Location**: `src/app/pages/pc-builder/`

### Các danh mục linh kiện:
1. **CPU** - Bộ vi xử lý
2. **MAINBOARD** - Bo mạch chủ
3. **RAM** - Bộ nhớ
4. **CARD ĐỒ HỌA** - Card màn hình
5. **Ổ CỨNG** - Ổ lưu trữ

## Cách sử dụng

### 1. Truy cập trang PC Builder
Điều hướng đến `/pc-builder` trong ứng dụng

### 2. Chọn linh kiện
- Click vào nút "Chọn [Tên danh mục]" cho mỗi loại linh kiện
- Modal sẽ hiển thị danh sách sản phẩm có sẵn
- Click vào sản phẩm để chọn

### 3. Điều chỉnh số lượng
- Sau khi chọn sản phẩm, có thể thay đổi số lượng (1-10)
- Tổng giá sẽ tự động cập nhật

### 4. Xóa sản phẩm
- Click nút "Xóa" để loại bỏ sản phẩm khỏi cấu hình

### 5. Lưu hoặc thêm vào giỏ hàng
- Click "Thêm vào giỏ hàng" để mua cấu hình
- Click "Lưu cấu hình" để lưu lại cho sau

## Cấu trúc Database

### Table: tbl_user_builds
```sql
- id (UUID, PK)
- user_id (UUID, FK)
- name (VARCHAR)
- total_price (DECIMAL)
- is_public (BOOLEAN)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Table: tbl_user_build_details
```sql
- id (UUID, PK)
- build_id (UUID, FK)
- product_id (UUID, FK)
- quantity (INTEGER)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

## Mapping Categories

Để chức năng hoạt động đúng, cần đảm bảo các category trong database có tên tương ứng:
- CPU
- Mainboard
- RAM
- VGA (Card đồ họa)
- Ổ cứng

Component sẽ tự động map các category này với build categories.

## Cải tiến trong tương lai

1. **Kiểm tra tương thích**: Kiểm tra các linh kiện có tương thích với nhau không
2. **Gợi ý cấu hình**: Đề xuất cấu hình dựa trên ngân sách
3. **Chia sẻ cấu hình**: Cho phép chia sẻ cấu hình với người khác
4. **So sánh cấu hình**: So sánh nhiều cấu hình khác nhau
5. **Đánh giá hiệu năng**: Hiển thị điểm hiệu năng ước tính

## Testing

### Backend
```bash
cd Computer-sell
mvn test
```

### Frontend
```bash
cd Computer_Sell_FrontEnd
ng serve
# Truy cập http://localhost:4200/pc-builder
```

## Troubleshooting

### Không load được sản phẩm
- Kiểm tra category mapping trong database
- Đảm bảo products có category_id đúng

### Không tạo được build
- Kiểm tra user đã đăng nhập chưa
- Xem console log để biết lỗi cụ thể

### Tổng giá không cập nhật
- Kiểm tra backend có tính toán đúng không
- Refresh lại trang
