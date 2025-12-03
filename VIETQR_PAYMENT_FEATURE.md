# Chức năng thanh toán VietQR

## Tổng quan
Đã thêm chức năng thanh toán qua VietQR cho phép khách hàng chuyển khoản ngân hàng và gửi ảnh xác nhận. Admin có thể xem ảnh và xác nhận/từ chối thanh toán.

## Cấu hình VietQR (Backend)

Chỉnh sửa file `Computer-sell/src/main/resources/application.yaml`:

```yaml
vietqr:
  bank-id: "970422"                    # Mã ngân hàng (xem danh sách bên dưới)
  account-no: "0123456789"             # Số tài khoản ngân hàng của bạn
  account-name: "CONG TY TNHH THCOMPUTER"  # Tên chủ tài khoản
  template: "compact2"                 # Template QR
```

### Danh sách mã ngân hàng phổ biến:
- MB Bank: `970422`
- Vietcombank: `970436`
- Techcombank: `970407`
- BIDV: `970418`
- VietinBank: `970415`
- ACB: `970416`
- Sacombank: `970403`
- TPBank: `970423`

## Migration Database

Chạy file SQL `migration_add_vietqr_proof_column.sql` để thêm cột lưu ảnh xác nhận:

```sql
ALTER TABLE tbl_payments 
ADD COLUMN IF NOT EXISTS proof_image_url VARCHAR(500) NULL;
```

## Luồng hoạt động

### Phía khách hàng:
1. Chọn "Thanh toán chuyển khoản (VietQR)" tại trang checkout
2. Đặt hàng → Hiển thị modal với mã QR
3. Quét mã QR bằng app ngân hàng để chuyển khoản
4. Upload ảnh xác nhận chuyển khoản
5. Chờ admin xác nhận

### Phía Admin:
1. Vào trang Quản lý thanh toán
2. Xem chi tiết giao dịch VietQR
3. Xem ảnh xác nhận chuyển khoản
4. Xác nhận hoặc từ chối thanh toán

## API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/payments/vietqr/{orderId}` | Tạo thanh toán VietQR |
| POST | `/api/payments/vietqr/{paymentId}/proof` | Upload ảnh xác nhận |
| PUT | `/api/payments/vietqr/{paymentId}/confirm` | Admin xác nhận |
| PUT | `/api/payments/vietqr/{paymentId}/reject` | Admin từ chối |

## Files đã thay đổi

### Backend:
- `PaymentMethod.java` - Thêm VIETQR enum
- `PaymentEntity.java` - Thêm trường proofImageUrl
- `PaymentResponse.java` - Thêm proofImageUrl, qrCodeUrl
- `VietQRConfig.java` - Cấu hình VietQR (mới)
- `VietQRService.java` - Interface service (mới)
- `VietQRServiceImpl.java` - Implementation (mới)
- `VietQRController.java` - REST API (mới)
- `LocalImageService.java` - Thêm method uploadImage
- `application.yaml` - Thêm cấu hình vietqr

### Frontend:
- `payment.service.ts` - Thêm VietQR methods
- `checkout.component.html/ts` - Thêm option VietQR và modal
- `order-detail.component.html/ts/scss` - Thêm VietQR section
- `admin-payments.component.html/ts/scss` - Thêm xem ảnh và xác nhận

## Lưu ý
- Ảnh xác nhận được lưu tại `/uploads/vietqr-proofs/`
- Mã QR được tạo từ API VietQR.io (miễn phí)
- Nội dung chuyển khoản tự động tạo theo format: `DH` + 8 ký tự đầu của orderId
