import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';
import { PageResponse, ResponseEnvelope } from './product.service';

export interface PaymentSearchParams {
  keyword?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  pageNo?: number;
  pageSize?: number;
  sortBy?: string;
}

export interface PaymentRecord {
  id: string;
  customerName: string;
  paymentMethod: string;
  paymentStatus: string;
  amount: number;
  paymentDate: string;
  transactionId?: string;
}

export interface PaymentDetail extends PaymentRecord {
  orderId: string;
  proofImageUrl?: string;
  qrCodeUrl?: string;
}

export interface VietQRPaymentResponse {
  id: string;
  orderId: string;
  transactionId: string;
  paymentMethod: string;
  amount: number;
  paymentStatus: string;
  paymentDate: string;
  qrCodeUrl: string;
  proofImageUrl?: string;
}

export interface PaymentOverviewStats {
  totalRevenue: number;
  totalTransactions: number;
  revenueByMethod: Record<string, number>;
}

export interface MonthlyRevenue {
  year: number;
  monthlyRevenue: number[];
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly PAYMENT_API = `${environment.apiUrl}/api/payments`;
  private readonly ADMIN_PAYMENT_API = `${environment.apiUrl}/api/admin/payments`;
  private readonly ADMIN_PAYMENT_STAT_API = `${environment.apiUrl}/api/admin/payments/statistics`;

  constructor(private http: HttpClient, private cookies: CookieService) { }

  searchPayments(params: PaymentSearchParams = {}): Observable<ResponseEnvelope<PageResponse<PaymentRecord>>> {
    return this.http.get<ResponseEnvelope<PageResponse<PaymentRecord>>>(`${this.ADMIN_PAYMENT_API}/search`, {
      headers: this.authHeaders(),
      params: this.buildParams(params)
    });
  }

  getOverview(): Observable<ResponseEnvelope<PaymentOverviewStats>> {
    return this.http.get<ResponseEnvelope<PaymentOverviewStats>>(`${this.ADMIN_PAYMENT_STAT_API}/overview`, {
      headers: this.authHeaders()
    });
  }

  getMonthlyRevenue(year: number): Observable<ResponseEnvelope<MonthlyRevenue>> {
    return this.http.get<ResponseEnvelope<MonthlyRevenue>>(`${this.ADMIN_PAYMENT_STAT_API}/monthly/${year}`, {
      headers: this.authHeaders()
    });
  }

  confirmPayment(paymentId: string): Observable<ResponseEnvelope<PaymentDetail>> {
    return this.http.put<ResponseEnvelope<PaymentDetail>>(`${this.PAYMENT_API}/confirm/${paymentId}`, {}, { headers: this.authHeaders() });
  }

  createVnpayPayment(orderId: string): Observable<ResponseEnvelope<PaymentDetail>> {
    return this.http.post<ResponseEnvelope<PaymentDetail>>(`${this.PAYMENT_API}/vnpay/${orderId}`, {}, { headers: this.authHeaders() });
  }

  getPaymentDetail(paymentId: string): Observable<ResponseEnvelope<PaymentDetail>> {
    return this.http.get<ResponseEnvelope<PaymentDetail>>(`${this.PAYMENT_API}/${paymentId}`, { headers: this.authHeaders() });
  }

  createCashPayment(orderId: string): Observable<ResponseEnvelope<PaymentDetail>> {
    return this.http.post<ResponseEnvelope<PaymentDetail>>(`${this.PAYMENT_API}/cash/${orderId}`, {}, { headers: this.authHeaders() });
  }

  // VietQR Payment Methods
  createVietQRPayment(orderId: string): Observable<ResponseEnvelope<VietQRPaymentResponse>> {
    return this.http.post<ResponseEnvelope<VietQRPaymentResponse>>(`${this.PAYMENT_API}/vietqr/${orderId}`, {}, { headers: this.authHeaders() });
  }

  uploadVietQRProof(paymentId: string, file: File): Observable<ResponseEnvelope<PaymentDetail>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ResponseEnvelope<PaymentDetail>>(`${this.PAYMENT_API}/vietqr/${paymentId}/proof`, formData, { headers: this.authHeaders() });
  }

  confirmVietQRPayment(paymentId: string): Observable<ResponseEnvelope<PaymentDetail>> {
    return this.http.put<ResponseEnvelope<PaymentDetail>>(`${this.PAYMENT_API}/vietqr/${paymentId}/confirm`, {}, { headers: this.authHeaders() });
  }

  rejectVietQRPayment(paymentId: string, reason?: string): Observable<ResponseEnvelope<PaymentDetail>> {
    const params = reason ? new HttpParams().set('reason', reason) : new HttpParams();
    return this.http.put<ResponseEnvelope<PaymentDetail>>(`${this.PAYMENT_API}/vietqr/${paymentId}/reject`, {}, { headers: this.authHeaders(), params });
  }

  private buildParams(params: PaymentSearchParams): HttpParams {
    let httpParams = new HttpParams();
    if (params.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    if (typeof params.pageNo === 'number') httpParams = httpParams.set('pageNo', String(params.pageNo));
    if (typeof params.pageSize === 'number') httpParams = httpParams.set('pageSize', String(params.pageSize));
    if (params.sortBy) httpParams = httpParams.set('sortBy', params.sortBy);
    return httpParams;
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
