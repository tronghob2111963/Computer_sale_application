# Hệ Thống Quản Lý Kho Hàng - E-commerce Standard

## Tổng Quan

Hệ thống quản lý kho hàng đã được cập nhật theo chuẩn e-commerce với các tính năng:

### 1. Quy Trình Nhập Kho

#### A. Tạo sản phẩm mới
- Sản phẩm mới tạo có `stock = 0`
- Trạng thái mặc định: `ACTIVE`
- Phải nhập kho mới có tồn kho

#### B. Nhập kho (Stock In)
```
POST /api/import-receipts
{
  "employeeId": "uuid",
  "note": "Nhập hàng đợt tháng 12",
  "items": [
    {
      "productId": "uuid",
      "quantity": 20,
      "importPrice": 5000000
    }
  ]
}
```

**Hệ thống tự động:**
- Tăng `stock` của sản phẩm
- Ghi lịch sử vào `tbl_stock_history`
- Sinh mã phiếu: `PN-2025-0001`

### 2. Quy Trình Đặt Hàng & Xử Lý Kho

#### Flow trạng thái đơn hàng:
```
PENDING → CONFIRMED → PROCESSING → SHIPPING → COMPLETED
    ↓         ↓           ↓           ↓
CANCEL_REQUEST → CANCELED (hoàn kho nếu đã trừ)
```

#### Quy tắc trừ/hoàn kho:

| Chuyển trạng thái | Xử lý kho |
|-------------------|-----------|
| PENDING → CONFIRMED | **TRỪ KHO** |
| CONFIRMED/PROCESSING/SHIPPING → CANCELED | **HOÀN KHO** |
| PENDING → CANCELED | Không hoàn (chưa trừ) |

**Lý do không trừ kho khi PENDING:**
- Tránh bom hàng
- Tránh hủy đơn
- Tránh lỗi thanh toán

### 3. Soft Delete Sản Phẩm

Thay vì xóa cứng, sản phẩm được chuyển trạng thái:

| Status | Mô tả |
|--------|-------|
| ACTIVE | Đang bán |
| INACTIVE | Tạm ngưng bán |
| DELETED | Đã xóa (soft delete) |

**API:**
```
PUT /product/soft-delete/{id}     # Xóa mềm
PUT /product/restore/{id}         # Khôi phục
PUT /product/status/{id}?status=INACTIVE  # Cập nhật trạng thái
```

### 4. Lịch Sử Kho (Stock History)

Mọi biến động kho đều được ghi lại:

| Movement Type | Mô tả |
|---------------|-------|
| IMPORT | Nhập kho từ nhà cung cấp |
| EXPORT | Xuất kho (bán hàng) |
| RETURN | Hoàn trả (hủy đơn) |
| ADJUSTMENT | Điều chỉnh (kiểm kê) |

**API xem lịch sử:**
```
GET /api/stock/history/product/{productId}
GET /api/stock/history/type/{IMPORT|EXPORT|RETURN|ADJUSTMENT}
GET /api/stock/history/date-range?start=...&end=...
GET /api/stock/check/{productId}
```

### 5. API Endpoints

#### Import Receipt (Phiếu nhập kho)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | /api/import-receipts | Tạo phiếu nhập |
| GET | /api/import-receipts | Danh sách phiếu nhập |
| GET | /api/import-receipts/{id} | Chi tiết phiếu nhập |
| PUT | /api/import-receipts/{id}/cancel | Hủy phiếu nhập |
| GET | /api/import-receipts/filter/status/{status} | Lọc theo trạng thái |
| GET | /api/import-receipts/filter/date-range | Lọc theo thời gian |
| GET | /api/import-receipts/filter/employee/{id} | Lọc theo nhân viên |

#### Stock Management (Quản lý kho)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | /api/stock/history/product/{id} | Lịch sử kho theo SP |
| GET | /api/stock/check/{id} | Kiểm tra tồn kho |
| GET | /api/stock/check-available/{id}?quantity=5 | Kiểm tra đủ kho |
| POST | /api/stock/adjust | Điều chỉnh kho |

### 6. Database Migration

Chạy file `migration_inventory_management.sql` để:
- Thêm cột `status` cho `tbl_products`
- Tạo bảng `tbl_stock_history`
- Tạo bảng `tbl_import_receipts` (nếu chưa có)
- Tạo bảng `tbl_import_receipt_details` (nếu chưa có)

### 7. Ví Dụ Thực Tế

#### Scenario 1: Nhập kho sản phẩm mới
1. Tạo sản phẩm → stock = 0
2. Tạo phiếu nhập với 20 cái, giá 5tr
3. Hệ thống: stock = 20, ghi history IMPORT

#### Scenario 2: Khách đặt hàng
1. Khách đặt 2 cái → Đơn PENDING (chưa trừ kho)
2. Admin xác nhận → CONFIRMED (trừ kho: 20 - 2 = 18)
3. Giao hàng → SHIPPING → COMPLETED

#### Scenario 3: Hủy đơn hàng
1. Đơn đang CONFIRMED (đã trừ kho)
2. Khách yêu cầu hủy → CANCEL_REQUEST
3. Admin duyệt hủy → CANCELED (hoàn kho: 18 + 2 = 20)

#### Scenario 4: Xóa sản phẩm
1. Sản phẩm không bán nữa
2. Gọi API soft-delete → status = DELETED
3. Sản phẩm không hiển thị nhưng vẫn còn trong DB
4. Có thể restore nếu cần
