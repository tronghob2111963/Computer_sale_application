import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationService, Notification, NotificationType, NotificationPageResponse } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-notifications.component.html',
  styleUrls: ['./admin-notifications.component.scss']
})
export class AdminNotificationsComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  isLoading = false;
  currentPage = 0;
  pageSize = 20;
  totalPages = 0;
  totalElements = 0;
  hasMore = true;
  unreadCount = 0;

  private userId: string | null = null;
  private subscriptions: Subscription[] = [];

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.userId = this.authService.getUserId();
    if (this.userId) {
      this.loadNotifications();

      // Subscribe to unread count
      this.subscriptions.push(
        this.notificationService.unreadCount$.subscribe(count => {
          this.unreadCount = count;
        })
      );
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  loadNotifications(): void {
    if (!this.userId || this.isLoading) return;

    this.isLoading = true;
    this.notificationService.getNotifications(this.userId, this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        if (response.data) {
          const pageData = response.data as NotificationPageResponse;
          if (this.currentPage === 0) {
            this.notifications = pageData.items;
          } else {
            this.notifications = [...this.notifications, ...pageData.items];
          }
          this.totalPages = pageData.totalPages;
          this.totalElements = pageData.totalElements;
          this.hasMore = !pageData.last;
        }
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  loadMore(): void {
    if (this.hasMore && !this.isLoading) {
      this.currentPage++;
      this.loadNotifications();
    }
  }

  refresh(): void {
    this.currentPage = 0;
    this.notifications = [];
    this.loadNotifications();
  }

  markAsRead(notification: Notification): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.isRead = true;
        }
      });
    }
    this.navigateToReference(notification);
  }

  markAllAsRead(): void {
    if (!this.userId) return;
    this.notificationService.markAllAsRead(this.userId).subscribe({
      next: () => {
        this.notifications = this.notifications.map(n => ({ ...n, isRead: true }));
      }
    });
  }

  deleteNotification(notification: Notification, event: Event): void {
    event.stopPropagation();
    this.notificationService.deleteNotification(notification.id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== notification.id);
        this.totalElements--;
      }
    });
  }

  navigateToReference(notification: Notification): void {
    if (!notification.referenceId) return;

    switch (notification.referenceType) {
      case 'ORDER':
        this.router.navigate(['/admin/orders'], { queryParams: { orderId: notification.referenceId } });
        break;
      case 'COMMENT':
        this.router.navigate(['/admin/feedback']);
        break;
      case 'PRODUCT':
        this.router.navigate(['/admin/products'], { queryParams: { productId: notification.referenceId } });
        break;
      default:
        break;
    }
  }

  getIcon(type: NotificationType): string {
    return this.notificationService.getNotificationIcon(type);
  }

  getIconColor(type: NotificationType): string {
    return this.notificationService.getNotificationColor(type);
  }

  getTypeBadge(type: NotificationType): { label: string; class: string } {
    const badges: Record<NotificationType, { label: string; class: string }> = {
      NEW_ORDER: { label: 'Đơn hàng mới', class: 'bg-blue-100 text-blue-800' },
      CANCEL_REQUEST: { label: 'Yêu cầu hủy', class: 'bg-red-100 text-red-800' },
      NEW_COMMENT: { label: 'Bình luận mới', class: 'bg-cyan-100 text-cyan-800' },
      LOW_STOCK: { label: 'Cảnh báo kho', class: 'bg-yellow-100 text-yellow-800' },
      NEW_REVIEW: { label: 'Đánh giá mới', class: 'bg-amber-100 text-amber-800' },
      NEW_USER: { label: 'Người dùng mới', class: 'bg-indigo-100 text-indigo-800' },
      PAYMENT_RECEIVED: { label: 'Thanh toán', class: 'bg-green-100 text-green-800' },
      ORDER_STATUS_CHANGED: { label: 'Đơn hàng', class: 'bg-blue-100 text-blue-800' },
      COMMENT_REPLIED: { label: 'Trả lời', class: 'bg-green-100 text-green-800' },
      PROMOTION_NEW: { label: 'Khuyến mãi', class: 'bg-purple-100 text-purple-800' },
      PAYMENT_CONFIRMED: { label: 'Thanh toán', class: 'bg-emerald-100 text-emerald-800' },
      ORDER_SHIPPED: { label: 'Giao hàng', class: 'bg-orange-100 text-orange-800' },
      ORDER_COMPLETED: { label: 'Hoàn thành', class: 'bg-green-100 text-green-800' }
    };
    return badges[type] || { label: type, class: 'bg-gray-100 text-gray-800' };
  }
}
