import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface OrderItemRequest {
  productId: string;
  quantity: number;
}

export interface OrderRequest {
  userId: string;
  paymentMethod: string; // Must match backend enum name
  promoCode?: string | null;
  items: OrderItemRequest[];
}

export interface ResponseEnvelope<T = any> {
  code: number;
  message: string;
  data: T;
}

export interface DashboardProfile {
  fullName: string;
  avatarText: string;
  phoneMasked: string;
  membershipLevel: string;
  membershipMessage: string;
  memberSince: string;
  nextReviewDate: string;
  badges: string[];
}

export interface DashboardStats {
  totalOrders: number;
  totalSpent: number;
  pendingCount: number;
  cancelRequestedCount: number;
  completedCount: number;
  periodLabel: string;
}

export interface DashboardNavItem {
  key: string;
  label: string;
  icon: string;
  pinned: boolean;
  description?: string;
}

export interface DashboardOrderCard {
  id: string;
  code: string;
  orderDate: string;
  title: string;
  subtitle: string;
  itemsCount: number;
  status: string;
  statusLabel: string;
  statusTone: 'success' | 'warning' | 'info' | 'danger';
  totalAmount: number;
  cancellable: boolean;
}

export interface DashboardOffer {
  id: string;
  title: string;
  code: string;
  description?: string;
  discountPercent?: number;
  endDate?: string;
  highlight: string;
}

export interface OrderDashboardResponse {
  profile: DashboardProfile;
  stats: DashboardStats;
  sections: DashboardNavItem[];
  recentOrders: DashboardOrderCard[];
  offers: DashboardOffer[];
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly API = `${environment.apiUrl}/api/orders`;
  private readonly USER_API_V1 = `${environment.apiUrl}/api/v1/user/orders`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  createOrder(payload: OrderRequest): Observable<ResponseEnvelope<any>> {
    return this.http.post<ResponseEnvelope<any>>(`${this.API}/create`, payload, { headers: this.authHeaders() });
  }

  getOrderById(id: string): Observable<ResponseEnvelope<any>> {
    return this.http.get<ResponseEnvelope<any>>(`${this.API}/${id}`, { headers: this.authHeaders() });
  }

  getOrdersByUser(userId: string): Observable<ResponseEnvelope<any[]>> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<ResponseEnvelope<any[]>>(this.USER_API_V1, {
      params,
      headers: this.authHeaders()
    });
  }

  getDashboard(userId: string): Observable<ResponseEnvelope<OrderDashboardResponse>> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<ResponseEnvelope<OrderDashboardResponse>>(`${this.USER_API_V1}/dashboard`, {
      params,
      headers: this.authHeaders()
    });
  }

  cancelOrder(id: string): Observable<ResponseEnvelope<null>> {
    return this.http.put<ResponseEnvelope<null>>(`${this.API}/cancel/${id}`, {}, { headers: this.authHeaders() });
  }

  // New: user sends cancel request (only when PENDING)
  requestCancel(id: string, reason: string): Observable<ResponseEnvelope<null>> {
    const url = `${this.USER_API_V1}/${id}/cancel-request` + (reason ? `?reason=${encodeURIComponent(reason)}` : '');
    return this.http.put<ResponseEnvelope<null>>(url, {}, { headers: this.authHeaders() });
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
