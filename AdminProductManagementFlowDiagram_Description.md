# Admin Product Management Flow Diagram - Mô tả

## Tổng quan
Flow diagram này mô tả quy trình quản lý sản phẩm trong hệ thống admin của ứng dụng bán máy tính.

## Các thành phần chính

### 1. Frontend Components
- **AdminProductsComponent** (`admin-products.component.ts`)
- Sử dụng các service: ProductService, BrandService, ProductTypeService, CategoryService

### 2. Backend API Endpoints
| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/product/list` | GET | Lấy danh sách sản phẩm (phân trang) |
| `/product/create` | POST | Tạo sản phẩm mới |
| `/product/update` | POST | Cập nhật sản phẩm |
| `/product/detail/{id}` | GET | Xem chi tiết sản phẩm |
| `/product/delete/{id}` | DELETE | Xóa cứng sản phẩm |
| `/product/soft-delete/{id}` | PUT | Xóa mềm sản phẩm |
| `/product/restore/{id}` | PUT | Khôi phục sản phẩm |
| `/product/status/{id}` | PUT | Cập nhật trạng thái |
| `/product/list/brand/{id}` | GET | Lọc theo thương hiệu |
| `/product/list/category/{id}` | GET | Lọc theo danh mục |
| `/product/filter/product-type/{id}` | GET | Lọc theo loại sản phẩm |

## Các Flow chính

### Flow 1: Xem danh sách sản phẩm
1. Admin đăng nhập → Vào trang quản lý sản phẩm
2. Gọi `loadProducts()` → GET `/product/list`
3. Hiển thị danh sách với phân trang

### Flow 2: Thêm sản phẩm mới
1. Click "Thêm mới" → `openCreate()`
2. Load options (Brands, Categories, ProductTypes)
3. Nhập thông tin: Tên, Giá, Mô tả, Bảo hành, Upload ảnh
4. Submit → POST `/product/create`
5. Thành công → Toast + Reload danh sách

### Flow 3: Chỉnh sửa sản phẩm
1. Click "Chỉnh sửa" → `openEdit(item)`
2. GET `/product/detail/{id}` để lấy dữ liệu
3. Populate form với dữ liệu hiện tại
4. Chỉnh sửa (Lưu ý: Stock chỉ thay đổi qua phiếu nhập kho)
5. Submit → POST `/product/update`

### Flow 4: Xóa sản phẩm
- **Xóa mềm (Khuyến khích)**: PUT `/product/soft-delete/{id}` → Status = DELETED
- **Xóa cứng**: DELETE `/product/delete/{id}` (Không khuyến khích)

### Flow 5: Quản lý trạng thái
- **ACTIVE**: Đang bán
- **INACTIVE**: Tạm ngưng bán
- **DELETED**: Đã xóa (soft delete)

### Flow 6: Khôi phục sản phẩm
- Sản phẩm đã xóa mềm có thể khôi phục
- PUT `/product/restore/{id}` → Status = ACTIVE

## Lưu ý quan trọng
- **Stock (Tồn kho)**: Không được chỉnh sửa trực tiếp, chỉ thay đổi qua phiếu nhập kho
- **Soft Delete**: Ưu tiên sử dụng xóa mềm để bảo toàn dữ liệu
- **Authorization**: Yêu cầu quyền Admin hoặc SysAdmin
