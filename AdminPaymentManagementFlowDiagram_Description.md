# Admin Payment Management Flow Diagram - Mô tả

## Tổng quan
Flow diagram này mô tả quy trình quản lý thanh toán trong hệ thống admin.

## Các thành phần chính

### 1. Frontend Components
- **AdminPaymentsComponent** (`admin-payments.component.ts`)
- Sử dụng service: PaymentService

### 2. Backend API Endpoints
| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/api/admin/payments/statistics/overview` | GET | Thống kê tổng quan |
| `/api/admin/payments/statistics/monthly/{year}` | GET | Doanh thu theo tháng |
| `/api/admin/payments/search` | GET | Tìm kiếm/lọc thanh toán |
| `/api/payments/{id}` | GET | Chi tiết giao dịch |
| `/api/payments/confirm/{id}` | POST | Xác nhận thanh toán |
| `/api/payments/vietqr/confirm/{id}` | POST | Xác nhận VietQR |
| `/api/payments/vietqr/reject/{id}` | POST | Từ chối VietQR |

## Payment Status (Trạng thái thanh toán)
| Status | Mô tả | Màu |
|--------|-------|-----|
| SUCCESS | Thanh toán thành công | Xanh lá |
| PAID | Đã thu tiền (COD) | Xanh lá |
| UNPAID | Chưa thanh toán | Vàng |
| PENDING | Đang chờ xử lý | Vàng |
| FAILED | Thanh toán thất bại | Đỏ |
| REFUNDED | Đã hoàn tiền | Xanh dương |

## Payment Methods (Phương thức thanh toán)
- **CASH (COD)**: Thanh toán khi nhận hàng
- **VNPAY**: Cổng thanh toán VNPay
- **VIETQR**: Chuyển khoản qua QR
- **MOMO**: Ví điện tử MoMo

## Các Flow chính

### Flow 1: Xem Dashboard thanh toán
1. Admin đăng nhập → Vào trang quản lý thanh toán
2. Parallel load 3 API:
   - `loadOverview()` → Thống kê tổng quan
   - `loadMonthly()` → Biểu đồ doanh thu 12 tháng
   - `loadPayments()` → Danh sách giao dịch

### Flow 2: Tìm kiếm/Lọc thanh toán
Các tiêu chí lọc:
- **Keyword**: Tìm theo mã đơn, tên khách
- **Status**: Lọc theo trạng thái
- **Start/End Date**: Khoảng thời gian
- **Sort By**: Sắp xếp theo ngày thanh toán

### Flow 3: Xem chi tiết giao dịch
1. Click vào giao dịch → `openDetail()`
2. GET `/api/payments/{id}`
3. Hiển thị Detail Panel:
   - Thông tin đơn hàng
   - Phương thức thanh toán
   - Số tiền
   - Transaction ID
   - Ảnh chứng từ (nếu VietQR)

### Flow 4: Xác nhận thanh toán thông thường
1. Kiểm tra `canConfirm()`: Status != SUCCESS/PAID/FAILED
2. Nếu hợp lệ → `confirmPayment()`
3. POST `/api/payments/confirm/{id}`
4. Cập nhật trạng thái → SUCCESS

### Flow 5: Xử lý thanh toán VietQR
1. Kiểm tra có phải VietQR payment
2. Xem ảnh chứng từ chuyển khoản
3. Quyết định:
   - **Approve**: `confirmVietQRPayment()` → SUCCESS
   - **Reject**: `rejectVietQRPayment()` → FAILED (kèm lý do)

## Overview Statistics (Thống kê tổng quan)
- Tổng doanh thu
- Số lượng giao dịch
- Doanh thu theo phương thức thanh toán

## Monthly Revenue Chart
- Biểu đồ cột hiển thị doanh thu 12 tháng
- Có thể chọn năm để xem

## Lưu ý quan trọng
- VietQR cần xác nhận thủ công dựa trên ảnh chứng từ
- COD được xác nhận khi shipper báo đã thu tiền
- VNPay/MoMo tự động cập nhật qua IPN callback
