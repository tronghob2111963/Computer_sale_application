package com.trong.Computer_sell.common;

public enum NotificationType {
    // User notifications
    ORDER_STATUS_CHANGED,   // Đơn hàng thay đổi trạng thái
    COMMENT_REPLIED,        // Bình luận được trả lời
    PROMOTION_NEW,          // Khuyến mãi mới
    PAYMENT_CONFIRMED,      // Thanh toán được xác nhận
    ORDER_SHIPPED,          // Đơn hàng đang giao
    ORDER_COMPLETED,        // Đơn hàng hoàn thành
    
    // Admin notifications
    NEW_ORDER,              // Đơn hàng mới
    CANCEL_REQUEST,         // Yêu cầu hủy đơn
    NEW_COMMENT,            // Bình luận mới cần duyệt
    LOW_STOCK,              // Sản phẩm sắp hết hàng
    NEW_REVIEW,             // Đánh giá mới
    NEW_USER,               // Người dùng mới đăng ký
    PAYMENT_RECEIVED        // Nhận thanh toán mới
}
