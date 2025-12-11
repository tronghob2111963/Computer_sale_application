-- Migration: Create notifications table
-- Run this SQL to add notification system

CREATE TABLE "tbl_notifications" (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  type VARCHAR(50) NOT NULL,
  title VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  reference_id UUID,
  reference_type VARCHAR(50),
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE
);

-- Index for faster queries
CREATE INDEX idx_notifications_user_id ON tbl_notifications(user_id);
CREATE INDEX idx_notifications_is_read ON tbl_notifications(is_read);
CREATE INDEX idx_notifications_created_at ON tbl_notifications(created_at DESC);

-- Notification types:
-- USER notifications:
--   ORDER_STATUS_CHANGED: Đơn hàng thay đổi trạng thái
--   COMMENT_REPLIED: Bình luận được trả lời
--   PROMOTION_NEW: Khuyến mãi mới
--   PAYMENT_CONFIRMED: Thanh toán được xác nhận
--
-- ADMIN notifications:
--   NEW_ORDER: Đơn hàng mới
--   CANCEL_REQUEST: Yêu cầu hủy đơn
--   NEW_COMMENT: Bình luận mới cần duyệt
--   LOW_STOCK: Sản phẩm sắp hết hàng
--   NEW_REVIEW: Đánh giá mới
