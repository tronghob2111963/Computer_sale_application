# Admin Order Management Flow Diagram - Mô tả

## Tổng quan
Flow diagram này mô tả quy trình quản lý đơn hàng (hóa đơn) trong hệ thống admin.

## Các thành phần chính

### 1. Frontend Components
- **AdminOrdersComponent** (`admin-orders.component.ts`)
- Sử dụng service: AdminOrderService

### 2. Backend API Endpoints
| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/api/v1/admin/orders` | GET | Lấy danh sách đơn hàng |
| `/api/v1/admin/orders/{id}/status` | PUT | Cập nhật trạng thái đơn |
| `/api/v1/admin/orders/{id}/cancel-request` | PUT | Xử lý yêu cầu hủy đơn |

## Order Status Lifecycle (Vòng đời đơn hàng)

```
PENDING → CONFIRMED → SHIPPING → COMPLETED
    ↓
CANCEL_REQUEST → CANCELED
```

### Các trạng thái:
| Status | Mô tả |
|--------|-------|
| PENDING | Đơn mới tạo, chờ xác nhận |
| CONFIRMED | Đã xác nhận, kiểm tra tồn kho xong |
| SHIPPING | Đang giao hàng |
| COMPLETED | Giao hàng thành công |
| CANCEL_REQUEST | Khách yêu cầu hủy đơn |
| CANCELED | Đơn đã bị hủy |

## Các Flow chính

### Flow 1: Xem danh sách đơn hàng
1. Admin đăng nhập → Vào trang quản lý đơn hàng
2. Gọi `load()` → GET `/api/v1/admin/orders`
3. `buildSummary()` tính toán thống kê:
   - Total, Pending, Shipping, Completed
   - Cancel, Awaiting Payment

### Flow 2: Lọc đơn hàng
- Lọc theo Status
- Lọc theo khoảng thời gian (Start Date - End Date)

### Flow 3: Cập nhật trạng thái đơn hàng
1. Chọn đơn hàng → Click cập nhật trạng thái
2. Kiểm tra trạng thái hiện tại
3. Nếu COMPLETED/CANCELED → Không thể thay đổi
4. Chỉ cho phép chuyển trạng thái hợp lệ:
   - PENDING → CONFIRMED
   - CONFIRMED → SHIPPING
   - SHIPPING → COMPLETED
5. Hiển thị Modal xác nhận
6. PUT `/api/v1/admin/orders/{id}/status`

### Flow 4: Xử lý yêu cầu hủy đơn
1. Đơn có trạng thái CANCEL_REQUEST
2. Admin chọn Approve hoặc Reject
3. PUT `/api/v1/admin/orders/{id}/cancel-request?approve=true/false`
4. Nếu Approve → Đơn chuyển sang CANCELED

## Guard Rules (Quy tắc bảo vệ)
| Chuyển đổi | Lý do không cho phép |
|------------|---------------------|
| PENDING → SHIPPING | Phải confirm trước |
| SHIPPING → CONFIRMED | Chỉ đi tiến, không lùi |
| COMPLETED → * | Đơn hoàn thành không thể thay đổi |
| CANCELED → * | Đơn đã hủy không thể mở lại |
| CANCEL_REQUEST → CONFIRMED | Chỉ có thể approve/reject |

## Payment Sync (Đồng bộ thanh toán)
Khi đơn hàng chuyển sang COMPLETED:
- Tự động cập nhật UNPAID → SUCCESS
- paymentStatus → PAID

**Lưu ý COD**: Chỉ complete sau khi shipper xác nhận đã thu tiền

## Authorization
- Yêu cầu quyền: SysAdmin, Admin, hoặc Staff
