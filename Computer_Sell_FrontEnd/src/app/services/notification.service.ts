import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, interval } from 'rxjs';
import { tap, switchMap, startWith } from 'rxjs/operators';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface Notification {
    id: string;
    type: NotificationType;
    title: string;
    message: string;
    referenceId?: string;
    referenceType?: string;
    isRead: boolean;
    createdAt: string;
    timeAgo: string;
}

export type NotificationType =
    | 'ORDER_STATUS_CHANGED'
    | 'COMMENT_REPLIED'
    | 'PROMOTION_NEW'
    | 'PAYMENT_CONFIRMED'
    | 'ORDER_SHIPPED'
    | 'ORDER_COMPLETED'
    | 'NEW_ORDER'
    | 'CANCEL_REQUEST'
    | 'NEW_COMMENT'
    | 'LOW_STOCK'
    | 'NEW_REVIEW'
    | 'NEW_USER'
    | 'PAYMENT_RECEIVED';

export interface NotificationCount {
    unreadCount: number;
    totalCount?: number;
}

export interface NotificationPageResponse {
    pageNo: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
    items: Notification[];
}

export interface ResponseEnvelope<T = any> {
    code: number;
    message: string;
    data: T;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
    private readonly API = `${environment.apiUrl}/api/notifications`;

    // BehaviorSubject để theo dõi số thông báo chưa đọc
    private unreadCountSubject = new BehaviorSubject<number>(0);
    public unreadCount$ = this.unreadCountSubject.asObservable();

    // BehaviorSubject để theo dõi danh sách thông báo
    private notificationsSubject = new BehaviorSubject<Notification[]>([]);
    public notifications$ = this.notificationsSubject.asObservable();

    constructor(
        private http: HttpClient,
        private cookies: CookieService
    ) { }

    // Bắt đầu polling để cập nhật số thông báo (mỗi 30 giây)
    startPolling(userId: string): Observable<ResponseEnvelope<NotificationCount>> {
        return interval(30000).pipe(
            startWith(0),
            switchMap(() => this.getNotificationCount(userId)),
            tap(response => {
                if (response.data) {
                    this.unreadCountSubject.next(response.data.unreadCount);
                }
            })
        );
    }

    // Lấy danh sách thông báo
    getNotifications(userId: string, pageNo = 0, pageSize = 10): Observable<ResponseEnvelope<NotificationPageResponse>> {
        const params = new HttpParams()
            .set('pageNo', pageNo.toString())
            .set('pageSize', pageSize.toString());

        return this.http.get<ResponseEnvelope<NotificationPageResponse>>(
            `${this.API}/user/${userId}`,
            { params, headers: this.authHeaders() }
        ).pipe(
            tap(response => {
                if (response.data?.items) {
                    this.notificationsSubject.next(response.data.items);
                }
            })
        );
    }


    // Lấy thông báo chưa đọc
    getUnreadNotifications(userId: string): Observable<ResponseEnvelope<Notification[]>> {
        return this.http.get<ResponseEnvelope<Notification[]>>(
            `${this.API}/user/${userId}/unread`,
            { headers: this.authHeaders() }
        ).pipe(
            tap(response => {
                if (response.data) {
                    this.notificationsSubject.next(response.data);
                    this.unreadCountSubject.next(response.data.length);
                }
            })
        );
    }

    // Đếm số thông báo chưa đọc
    getNotificationCount(userId: string): Observable<ResponseEnvelope<NotificationCount>> {
        return this.http.get<ResponseEnvelope<NotificationCount>>(
            `${this.API}/user/${userId}/count`,
            { headers: this.authHeaders() }
        ).pipe(
            tap(response => {
                if (response.data) {
                    this.unreadCountSubject.next(response.data.unreadCount);
                }
            })
        );
    }

    // Đánh dấu 1 thông báo đã đọc
    markAsRead(notificationId: string): Observable<ResponseEnvelope<Notification>> {
        return this.http.put<ResponseEnvelope<Notification>>(
            `${this.API}/${notificationId}/read`,
            {},
            { headers: this.authHeaders() }
        ).pipe(
            tap(() => {
                // Giảm số unread
                const currentCount = this.unreadCountSubject.value;
                if (currentCount > 0) {
                    this.unreadCountSubject.next(currentCount - 1);
                }
                // Cập nhật trạng thái trong list
                const notifications = this.notificationsSubject.value.map(n =>
                    n.id === notificationId ? { ...n, isRead: true } : n
                );
                this.notificationsSubject.next(notifications);
            })
        );
    }

    // Đánh dấu tất cả đã đọc
    markAllAsRead(userId: string): Observable<ResponseEnvelope<{ updatedCount: number }>> {
        return this.http.put<ResponseEnvelope<{ updatedCount: number }>>(
            `${this.API}/user/${userId}/read-all`,
            {},
            { headers: this.authHeaders() }
        ).pipe(
            tap(() => {
                this.unreadCountSubject.next(0);
                const notifications = this.notificationsSubject.value.map(n => ({ ...n, isRead: true }));
                this.notificationsSubject.next(notifications);
            })
        );
    }

    // Xóa thông báo
    deleteNotification(notificationId: string): Observable<ResponseEnvelope<null>> {
        return this.http.delete<ResponseEnvelope<null>>(
            `${this.API}/${notificationId}`,
            { headers: this.authHeaders() }
        ).pipe(
            tap(() => {
                const notifications = this.notificationsSubject.value.filter(n => n.id !== notificationId);
                this.notificationsSubject.next(notifications);
            })
        );
    }

    // Cập nhật unread count thủ công
    updateUnreadCount(count: number): void {
        this.unreadCountSubject.next(count);
    }

    // Refresh notifications
    refreshNotifications(userId: string): void {
        this.getUnreadNotifications(userId).subscribe();
    }

    // Lấy icon theo loại thông báo
    getNotificationIcon(type: NotificationType): string {
        const icons: Record<NotificationType, string> = {
            ORDER_STATUS_CHANGED: 'fa-box',
            COMMENT_REPLIED: 'fa-comment',
            PROMOTION_NEW: 'fa-tag',
            PAYMENT_CONFIRMED: 'fa-credit-card',
            ORDER_SHIPPED: 'fa-truck',
            ORDER_COMPLETED: 'fa-check-circle',
            NEW_ORDER: 'fa-shopping-cart',
            CANCEL_REQUEST: 'fa-times-circle',
            NEW_COMMENT: 'fa-comments',
            LOW_STOCK: 'fa-exclamation-triangle',
            NEW_REVIEW: 'fa-star',
            NEW_USER: 'fa-user-plus',
            PAYMENT_RECEIVED: 'fa-money-bill'
        };
        return icons[type] || 'fa-bell';
    }

    // Lấy màu theo loại thông báo
    getNotificationColor(type: NotificationType): string {
        const colors: Record<NotificationType, string> = {
            ORDER_STATUS_CHANGED: 'text-blue-500',
            COMMENT_REPLIED: 'text-green-500',
            PROMOTION_NEW: 'text-purple-500',
            PAYMENT_CONFIRMED: 'text-emerald-500',
            ORDER_SHIPPED: 'text-orange-500',
            ORDER_COMPLETED: 'text-green-600',
            NEW_ORDER: 'text-blue-600',
            CANCEL_REQUEST: 'text-red-500',
            NEW_COMMENT: 'text-cyan-500',
            LOW_STOCK: 'text-yellow-500',
            NEW_REVIEW: 'text-amber-500',
            NEW_USER: 'text-indigo-500',
            PAYMENT_RECEIVED: 'text-green-500'
        };
        return colors[type] || 'text-gray-500';
    }

    // Lấy route để navigate khi click vào thông báo
    getNotificationRoute(notification: Notification): string[] {
        switch (notification.referenceType) {
            case 'ORDER':
                return ['/order', notification.referenceId || ''];
            case 'COMMENT':
                return ['/product', notification.referenceId || ''];
            case 'PRODUCT':
                return ['/admin/products', notification.referenceId || ''];
            default:
                return ['/'];
        }
    }

    private authHeaders(): HttpHeaders {
        const token = this.cookies.get('accessToken');
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }
}
