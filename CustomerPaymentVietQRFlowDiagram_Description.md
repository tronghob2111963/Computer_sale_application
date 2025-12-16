# Customer Payment Flow Diagram (COD & VietQR) - Mô tả chi tiết

## Tổng quan

After an order is created with PENDING status, users need to complete the payment. This algorithm describes the steps to process payment.

- User selects a payment method for the order (COD or VIETQR).

- If **COD payment** is selected (Thanh toán khi nhận hàng):
  - The system creates a COD payment record.
  - The order status is updated to CONFIRMED, payment status remains UNPAID (waiting for delivery).
  - Shipper delivers order to customer.
  - Customer pays cash upon receiving goods.
  - The payment status is updated to SUCCESS.

- If **VIETQR payment** is selected (Chuyển khoản ngân hàng):
  - The system generates a VietQR code and payment status is set to PENDING.
  - The user completes bank transfer and uploads transfer proof (screenshot).
  - The system waits for Admin verification.
  - Admin reviews the payment proof:
    - If Admin **confirms**: the payment is marked as SUCCESS, order status changes to CONFIRMED, and payment status changes to PAID.
    - If Admin **rejects**: the payment status is updated to FAILED, and user is notified of rejection.

- If payment is successful, a notification is sent to the user about the payment confirmation. A success message is displayed to confirm the payment is complete.
