package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.notification.NotificationCountResponse;
import com.trong.Computer_sell.DTO.response.notification.NotificationResponse;
import com.trong.Computer_sell.common.NotificationType;
import com.trong.Computer_sell.model.NotificationEntity;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.NotificationRepository;
import com.trong.Computer_sell.repository.UserHasRoleRepository;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserHasRoleRepository userHasRoleRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationResponse createNotification(UUID userId, NotificationType type, String title, 
                                                   String message, UUID referenceId, String referenceType) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            NotificationEntity notification = NotificationEntity.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .message(message)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .isRead(false)
                    .build();

            notification = notificationRepository.save(notification);
            log.info("Created notification for user {}: {} - ID: {}", userId, title, notification.getId());
            
            return NotificationResponse.fromEntity(notification);
        } catch (Exception e) {
            log.error("Failed to create notification for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void notifyAllAdmins(NotificationType type, String title, String message, 
                                UUID referenceId, String referenceType) {
        List<UUID> adminIds = userHasRoleRepository.findUserIdsByRoleName("ADMIN");
        
        for (UUID adminId : adminIds) {
            createNotification(adminId, type, title, message, referenceId, referenceType);
        }
        log.info("Notified {} admins: {}", adminIds.size(), title);
    }


    @Override
    public PageResponse<List<NotificationResponse>> getNotifications(UUID userId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<NotificationEntity> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationResponse> items = page.getContent().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.<List<NotificationResponse>>builder()
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .items(items)
                .build();
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationCountResponse getNotificationCount(UUID userId) {
        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return NotificationCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        
        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        
        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // === Helper methods ===

    @Override
    public void notifyOrderStatusChanged(UUID userId, UUID orderId, String oldStatus, String newStatus) {
        // Lấy title và message theo trạng thái mới (chuẩn thương mại điện tử)
        String[] notification = getOrderStatusNotification(newStatus.toUpperCase());
        String title = notification[0];
        String message = notification[1];
        
        // Chọn NotificationType phù hợp
        NotificationType type = getNotificationTypeForStatus(newStatus.toUpperCase());
        
        createNotification(userId, type, title, message, orderId, "ORDER");
    }

    /**
     * Lấy nội dung thông báo theo trạng thái đơn hàng (chuẩn thương mại điện tử)
     */
    private String[] getOrderStatusNotification(String status) {
        return switch (status) {
            case "CONFIRMED" -> new String[]{
                "Đơn hàng đã được xác nhận ✓",
                "Đơn hàng của bạn đã được xác nhận và đang được chuẩn bị. Chúng tôi sẽ sớm giao hàng cho đơn vị vận chuyển."
            };
            case "PROCESSING" -> new String[]{
                "Đơn hàng đang được xử lý",
                "Đơn hàng của bạn đang được đóng gói và chuẩn bị giao cho đơn vị vận chuyển."
            };
            case "SHIPPING" -> new String[]{
                "Đơn hàng đang được giao 🚚",
                "Đơn hàng của bạn đã được giao cho đơn vị vận chuyển và đang trên đường đến bạn. Vui lòng chú ý điện thoại để nhận hàng."
            };
            case "COMPLETED" -> new String[]{
                "Giao hàng thành công ✓",
                "Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã mua sắm tại THComputer! Hãy đánh giá sản phẩm để nhận ưu đãi cho lần mua tiếp theo."
            };
            case "CANCELED" -> new String[]{
                "Đơn hàng đã bị hủy",
                "Đơn hàng của bạn đã bị hủy. Nếu bạn đã thanh toán, số tiền sẽ được hoàn lại trong 3-5 ngày làm việc."
            };
            case "CANCEL_REQUEST" -> new String[]{
                "Yêu cầu hủy đơn đang chờ xử lý",
                "Yêu cầu hủy đơn hàng của bạn đã được ghi nhận và đang chờ xử lý. Chúng tôi sẽ thông báo kết quả sớm nhất."
            };
            default -> new String[]{
                "Cập nhật đơn hàng",
                "Đơn hàng của bạn đã được cập nhật trạng thái mới."
            };
        };
    }

    /**
     * Lấy NotificationType phù hợp theo trạng thái
     */
    private NotificationType getNotificationTypeForStatus(String status) {
        return switch (status) {
            case "SHIPPING" -> NotificationType.ORDER_SHIPPED;
            case "COMPLETED" -> NotificationType.ORDER_COMPLETED;
            default -> NotificationType.ORDER_STATUS_CHANGED;
        };
    }

    @Override
    public void notifyNewOrder(UUID orderId, String customerName, String totalAmount) {
        String title = "🛒 Đơn hàng mới";
        String message = String.format("Khách hàng %s vừa đặt đơn hàng trị giá %s. Vui lòng xác nhận đơn hàng.", customerName, totalAmount);
        
        notifyAllAdmins(NotificationType.NEW_ORDER, title, message, orderId, "ORDER");
    }

    @Override
    public void notifyCancelRequest(UUID orderId, String customerName, String reason) {
        String title = "⚠️ Yêu cầu hủy đơn";
        String message = String.format("Khách hàng %s yêu cầu hủy đơn hàng. Lý do: %s. Vui lòng xử lý yêu cầu này.", customerName, reason);
        
        notifyAllAdmins(NotificationType.CANCEL_REQUEST, title, message, orderId, "ORDER");
    }

    @Override
    public void notifyCommentReplied(UUID userId, UUID commentId, String productName, String replierName) {
        String title = "💬 Bình luận được trả lời";
        String message = String.format("%s đã trả lời bình luận của bạn về sản phẩm '%s'. Xem ngay!", replierName, productName);
        
        createNotification(userId, NotificationType.COMMENT_REPLIED, title, message, commentId, "COMMENT");
    }

    @Override
    public void notifyNewComment(UUID commentId, String productName, String userName) {
        String title = "💬 Bình luận mới cần duyệt";
        String message = String.format("%s vừa bình luận về sản phẩm '%s'. Vui lòng kiểm tra và phản hồi.", userName, productName);
        
        notifyAllAdmins(NotificationType.NEW_COMMENT, title, message, commentId, "COMMENT");
    }

    @Override
    public void notifyPaymentConfirmed(UUID userId, UUID orderId, String amount) {
        String title = "💳 Thanh toán thành công";
        String message = String.format("Chúng tôi đã nhận được thanh toán %s cho đơn hàng của bạn. Đơn hàng sẽ sớm được xử lý.", amount);
        
        createNotification(userId, NotificationType.PAYMENT_CONFIRMED, title, message, orderId, "ORDER");
    }

    @Override
    public void notifyLowStock(UUID productId, String productName, int currentStock) {
        String title = "⚠️ Cảnh báo tồn kho thấp";
        String message = String.format("Sản phẩm '%s' chỉ còn %d trong kho. Vui lòng nhập thêm hàng.", productName, currentStock);
        
        notifyAllAdmins(NotificationType.LOW_STOCK, title, message, productId, "PRODUCT");
    }
}
