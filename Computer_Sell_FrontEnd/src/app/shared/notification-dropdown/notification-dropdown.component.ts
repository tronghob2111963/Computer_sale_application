import { Component, OnInit, OnDestroy, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationService, Notification, NotificationType } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-notification-dropdown',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './notification-dropdown.component.html',
    styleUrls: ['./notification-dropdown.component.scss']
})
export class NotificationDropdownComponent implements OnInit, OnDestroy {
    isOpen = false;
    unreadCount = 0;
    notifications: Notification[] = [];
    isLoading = false;

    private subscriptions: Subscription[] = [];
    private userId: string | null = null;

    constructor(
        private notificationService: NotificationService,
        private authService: AuthService,
        private router: Router,
        private elementRef: ElementRef
    ) { }

    ngOnInit(): void {
        this.userId = this.authService.getUserIdSafe();
        console.log('🔔 NotificationDropdown - userId:', this.userId);

        if (this.userId) {
            // Subscribe to unread count
            this.subscriptions.push(
                this.notificationService.unreadCount$.subscribe(count => {
                    this.unreadCount = count;
                    console.log('🔔 Unread count updated:', count);
                })
            );

            // Subscribe to notifications list
            this.subscriptions.push(
                this.notificationService.notifications$.subscribe(notifications => {
                    this.notifications = notifications;
                    console.log('🔔 Notifications updated:', notifications.length);
                })
            );

            // Initial load
            this.loadNotifications();

            // Start polling
            this.subscriptions.push(
                this.notificationService.startPolling(this.userId).subscribe({
                    error: (err) => console.error('❌ Polling error:', err)
                })
            );
        } else {
            console.warn('⚠️ NotificationDropdown - No userId available');
        }
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(sub => sub.unsubscribe());
    }

    // Click outside to close
    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {
        if (!this.elementRef.nativeElement.contains(event.target)) {
            this.isOpen = false;
        }
    }

    toggleDropdown(): void {
        this.isOpen = !this.isOpen;
        if (this.isOpen && this.userId) {
            this.loadNotifications();
        }
    }

    loadNotifications(): void {
        if (!this.userId) {
            console.warn('⚠️ loadNotifications - No userId available');
            return;
        }

        this.isLoading = true;
        console.log('📥 Loading notifications for userId:', this.userId);
        this.notificationService.getUnreadNotifications(this.userId).subscribe({
            next: () => {
                this.isLoading = false;
                console.log('✅ Notifications loaded');
            },
            error: (err) => {
                this.isLoading = false;
                console.error('❌ Error loading notifications:', err);
            }
        });
    }

    markAsRead(notification: Notification, event: Event): void {
        event.stopPropagation();

        if (!notification.isRead) {
            this.notificationService.markAsRead(notification.id).subscribe();
        }

        // Navigate to related page
        this.navigateToReference(notification);
        this.isOpen = false;
    }

    markAllAsRead(): void {
        if (!this.userId) return;
        this.notificationService.markAllAsRead(this.userId).subscribe();
    }

    deleteNotification(notification: Notification, event: Event): void {
        event.stopPropagation();
        this.notificationService.deleteNotification(notification.id).subscribe();
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
            this.notificationService.markAsRead(notification.id).subscribe();
        }

        // Navigate đến trang chi tiết đơn hàng
        if (notification.referenceId) {
            this.router.navigate(['/order', notification.referenceId]);
        }
        this.isOpen = false;
    }

    getIcon(type: NotificationType): string {
        return this.notificationService.getNotificationIcon(type);
    }

    getIconColor(type: NotificationType): string {
        return this.notificationService.getNotificationColor(type);
    }

    viewAllNotifications(): void {
        this.router.navigate(['/notifications']);
        this.isOpen = false;
    }
}
