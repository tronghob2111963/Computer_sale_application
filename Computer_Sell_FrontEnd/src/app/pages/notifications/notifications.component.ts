import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationService, Notification, NotificationType, NotificationPageResponse } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { HeaderLayoutComponent } from '../../shared/header-layout/header-layout.component';

@Component({
    selector: 'app-notifications',
    standalone: true,
    imports: [CommonModule, HeaderLayoutComponent],
    templateUrl: './notifications.component.html',
    styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit, OnDestroy {
    notifications: Notification[] = [];
    isLoading = false;
    currentPage = 0;
    pageSize = 20;
    totalPages = 0;
    totalElements = 0;
    hasMore = true;

    private userId: string | null = null;
    private subscriptions: Subscription[] = [];

    constructor(
        private notificationService: NotificationService,
        private authService: AuthService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.userId = this.authService.getUserIdSafe();
        console.log('NotificationsPage - userId:', this.userId);
        if (!this.userId) {
            this.router.navigate(['/login']);
            return;
        }
        this.loadNotifications();
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
                this.router.navigate(['/orders'], { queryParams: { orderId: notification.referenceId } });
                break;
            case 'COMMENT':
            case 'PRODUCT':
                this.router.navigate(['/product', notification.referenceId]);
                break;
            default:
                break;
        }
    }

    // Xem chi tiết đơn hàng
    viewOrderDetail(notification: Notification, event: Event): void {
        event.stopPropagation();

        // Đánh dấu đã đọc nếu chưa đọc
        if (!notification.isRead) {
            this.notificationService.markAsRead(notification.id).subscribe({
                next: () => {
                    notification.isRead = true;
                }
            });
        }

        // Navigate đến trang chi tiết đơn hàng
        if (notification.referenceId) {
            this.router.navigate(['/order', notification.referenceId]);
        }
    }

    getIcon(type: NotificationType): string {
        return this.notificationService.getNotificationIcon(type);
    }

    getIconColor(type: NotificationType): string {
        return this.notificationService.getNotificationColor(type);
    }

    goBack(): void {
        window.history.back();
    }
}
