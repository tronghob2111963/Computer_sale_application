package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.notification.NotificationCountResponse;
import com.trong.Computer_sell.DTO.response.notification.NotificationResponse;
import com.trong.Computer_sell.common.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    // Tạo thông báo cho user
    NotificationResponse createNotification(UUID userId, NotificationType type, String title, String message, UUID referenceId, String referenceType);

    // Tạo thông báo cho tất cả admin
    void notifyAllAdmins(NotificationType type, String title, String message, UUID referenceId, String referenceType);

    // Lấy danh sách thông báo của user (có phân trang)
    PageResponse<List<NotificationResponse>> getNotifications(UUID userId, int pageNo, int pageSize);

    // Lấy thông báo chưa đọc
    List<NotificationResponse> getUnreadNotifications(UUID userId);

    // Đếm số thông báo chưa đọc
    NotificationCountResponse getNotificationCount(UUID userId);

    // Đánh dấu 1 thông báo đã đọc
    NotificationResponse markAsRead(UUID notificationId);

    // Đánh dấu tất cả thông báo đã đọc
    int markAllAsRead(UUID userId);

    // Xóa thông báo
    void deleteNotification(UUID notificationId);

    // === Helper methods để tạo thông báo từ các sự kiện ===

    // Thông báo khi đơn hàng thay đổi trạng thái
    void notifyOrderStatusChanged(UUID userId, UUID orderId, String oldStatus, String newStatus);

    // Thông báo khi có đơn hàng mới (cho admin)
    void notifyNewOrder(UUID orderId, String customerName, String totalAmount);

    // Thông báo khi có yêu cầu hủy đơn (cho admin)
    void notifyCancelRequest(UUID orderId, String customerName, String reason);

    // Thông báo khi bình luận được trả lời
    void notifyCommentReplied(UUID userId, UUID commentId, String productName, String replierName);

    // Thông báo khi có bình luận mới (cho admin)
    void notifyNewComment(UUID commentId, String productName, String userName);

    // Thông báo khi thanh toán được xác nhận
    void notifyPaymentConfirmed(UUID userId, UUID orderId, String amount);

    // Thông báo sản phẩm sắp hết hàng (cho admin)
    void notifyLowStock(UUID productId, String productName, int currentStock);
}
