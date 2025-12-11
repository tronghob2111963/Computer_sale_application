# Hệ thống Thông báo (Notification System)

## Tổng quan

Hệ thống thông báo cho phép gửi và nhận thông báo real-time cho cả User và Admin.

## Các loại thông báo

### Cho User:
- `ORDER_STATUS_CHANGED` - Đơn hàng thay đổi trạng thái
- `COMMENT_REPLIED` - Bình luận được trả lời
- `PROMOTION_NEW` - Khuyến mãi mới
- `PAYMENT_CONFIRMED` - Thanh toán được xác nhận
- `ORDER_SHIPPED` - Đơn hàng đang giao
- `ORDER_COMPLETED` - Đơn hàng hoàn thành

### Cho Admin:
- `NEW_ORDER` - Đơn hàng mới
- `CANCEL_REQUEST` - Yêu cầu hủy đơn
- `NEW_COMMENT` - Bình luận mới cần duyệt
- `LOW_STOCK` - Sản phẩm sắp hết hàng
- `NEW_REVIEW` - Đánh giá mới
- `NEW_USER` - Người dùng mới đăng ký
- `PAYMENT_RECEIVED` - Nhận thanh toán mới

## Cài đặt Database

Chạy file migration:
```sql
-- Chạy file: migration_notifications.sql
```

## API Endpoints

### Lấy danh sách thông báo
```
GET /api/notifications/user/{userId}?pageNo=0&pageSize=10
```

### Lấy thông báo chưa đọc
```
GET /api/notifications/user/{userId}/unread
```

### Đếm số thông báo chưa đọc
```
GET /api/notifications/user/{userId}/count
```

### Đánh dấu đã đọc
```
PUT /api/notifications/{notificationId}/read
```

### Đánh dấu tất cả đã đọc
```
PUT /api/notifications/user/{userId}/read-all
```

### Xóa thông báo
```
DELETE /api/notifications/{notificationId}
```

## Tích hợp tự động

Hệ thống đã được tích hợp tự động vào:

1. **OrderService** - Gửi thông báo khi:
   - Tạo đơn hàng mới → Admin nhận thông báo
   - Yêu cầu hủy đơn → Admin nhận thông báo

2. **AdminOrderService** - Gửi thông báo khi:
   - Thay đổi trạng thái đơn hàng → User nhận thông báo

3. **ProductCommentService** - Gửi thông báo khi:
   - Có bình luận mới → Admin nhận thông báo
   - Trả lời bình luận → User (người viết comment gốc) nhận thông báo

## Sử dụng trong code

### Backend - Gửi thông báo thủ công:

```java
@Autowired
private NotificationService notificationService;

// Gửi cho 1 user
notificationService.createNotification(
    userId,
    NotificationType.PROMOTION_NEW,
    "Khuyến mãi mới!",
    "Giảm 20% cho tất cả sản phẩm",
    promoId,
    "PROMOTION"
);

// Gửi cho tất cả Admin
notificationService.notifyAllAdmins(
    NotificationType.LOW_STOCK,
    "Cảnh báo tồn kho",
    "Sản phẩm XYZ chỉ còn 5 trong kho",
    productId,
    "PRODUCT"
);
```

### Frontend - Sử dụng NotificationService:

```typescript
import { NotificationService } from './services/notification.service';

constructor(private notificationService: NotificationService) {}

// Lấy số thông báo chưa đọc (reactive)
this.notificationService.unreadCount$.subscribe(count => {
  console.log('Unread:', count);
});

// Refresh thông báo
this.notificationService.refreshNotifications(userId);

// Đánh dấu đã đọc
this.notificationService.markAsRead(notificationId).subscribe();
```

## Files đã tạo

### Backend:
- `Computer-sell/src/main/java/com/trong/Computer_sell/model/NotificationEntity.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/common/NotificationType.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/repository/NotificationRepository.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/service/NotificationService.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/service/impl/NotificationServiceImpl.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/controller/NotificationController.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/DTO/response/notification/NotificationResponse.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/DTO/response/notification/NotificationCountResponse.java`

### Frontend:
- `Computer_Sell_FrontEnd/src/app/services/notification.service.ts`
- `Computer_Sell_FrontEnd/src/app/shared/notification-dropdown/` (component)
- `Computer_Sell_FrontEnd/src/app/pages/notifications/` (trang xem tất cả thông báo)
- `Computer_Sell_FrontEnd/src/app/pages/admin/admin-notifications/` (đã cập nhật)

### Database:
- `migration_notifications.sql`
