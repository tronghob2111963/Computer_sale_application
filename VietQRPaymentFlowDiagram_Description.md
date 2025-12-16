# VietQR Payment Flow Diagram - Mô tả chi tiết

## Tổng quan
Sơ đồ này mô tả luồng thanh toán qua VietQR trong hệ thống Computer Sell. VietQR là phương thức thanh toán chuyển khoản ngân hàng thông qua mã QR, yêu cầu xác nhận thủ công từ Admin.

## Các bước trong luồng thanh toán

### 1. Khởi tạo đơn hàng
- Đơn hàng được tạo với `Status = PENDING`, `PaymentStatus = UNPAID`

### 2. Chọn phương thức thanh toán
- User chọn phương thức thanh toán **VietQR** (Chuyển khoản ngân hàng)

### 3. Tạo Payment Record
- **API**: `POST /api/payments/vietqr/{orderId}`
- Hệ thống tạo bản ghi Payment với `PaymentStatus = PENDING`
- Sinh `transactionId` duy nhất cho giao dịch

### 4. Sinh mã QR
- Hệ thống sinh mã VietQR chứa:
  - Thông tin tài khoản ngân hàng
  - Số tiền cần thanh toán
  - Mã giao dịch (Transaction ID)
- Trả về `qrCodeUrl` cho Frontend

### 5. Hiển thị Modal QR
- Frontend hiển thị modal chứa:
  - Mã QR để quét
  - Thông tin ngân hàng
  - Số tiền cần chuyển
- User quét mã QR bằng ứng dụng ngân hàng

### 6. User thực hiện chuyển khoản
- User hoàn tất chuyển khoản qua ứng dụng Banking

### 7. Upload ảnh xác nhận
- **API**: `POST /api/payments/vietqr/{paymentId}/proof`
- User upload ảnh chụp màn hình xác nhận chuyển khoản thành công
- File được lưu và `proofImageUrl` được cập nhật

### 8. Chờ Admin xác nhận
- `PaymentStatus = PENDING`
- Hệ thống chờ Admin kiểm tra và xác nhận

### 9. Admin Review
Admin có 2 lựa chọn:

#### 9a. Xác nhận thanh toán (Confirm)
- **API**: `PUT /api/payments/vietqr/{paymentId}/confirm`
- Cập nhật:
  - `Payment Status = SUCCESS`
  - `Order Status = CONFIRMED`
  - `Order PaymentStatus = PAID`
- Gửi thông báo cho User
- **Kết thúc thành công**

#### 9b. Từ chối thanh toán (Reject)
- **API**: `PUT /api/payments/vietqr/{paymentId}/reject`
- Có thể kèm lý do từ chối
- Cập nhật:
  - `Payment Status = FAILED`
  - `Order PaymentStatus = UNPAID`
- User được thông báo về việc từ chối

### 10. Retry (Thử lại)
- Nếu bị từ chối, User có thể:
  - **Yes**: Quay lại bước 5, thực hiện lại quy trình
  - **No**: Hủy thanh toán, kết thúc

## API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/payments/vietqr/{orderId}` | Tạo VietQR Payment |
| POST | `/api/payments/vietqr/{paymentId}/proof` | Upload ảnh xác nhận |
| PUT | `/api/payments/vietqr/{paymentId}/confirm` | Admin xác nhận thanh toán |
| PUT | `/api/payments/vietqr/{paymentId}/reject` | Admin từ chối thanh toán |

## Response Data Structure

```typescript
interface VietQRPaymentResponse {
  id: string;
  orderId: string;
  transactionId: string;
  paymentMethod: string;
  amount: number;
  paymentStatus: string;
  paymentDate: string;
  qrCodeUrl: string;
  proofImageUrl?: string;
}
```

## Trạng thái Payment

| Status | Mô tả |
|--------|-------|
| PENDING | Đang chờ thanh toán / xác nhận |
| SUCCESS | Thanh toán thành công |
| FAILED | Thanh toán thất bại / bị từ chối |

## Ghi chú
- VietQR yêu cầu xác nhận thủ công từ Admin (khác với VNPay tự động callback)
- User phải upload ảnh chứng minh đã chuyển khoản
- Admin có thể từ chối nếu ảnh không hợp lệ hoặc không khớp số tiền
