import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
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
    return this.http.get<ResponseEnvelope<any[]>>(`${this.API}/user/${userId}`, { headers: this.authHeaders() });
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
