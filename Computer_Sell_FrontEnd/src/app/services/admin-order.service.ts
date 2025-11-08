import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface AdminOrderFilter {
  status?: string | null;
  start?: string | null; // ISO string
  end?: string | null;   // ISO string
}

export interface ResponseEnvelope<T = any> {
  code?: number;
  message?: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class AdminOrderService {
  private readonly API = `${environment.apiUrl}/api/v1/admin/orders`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  getOrders(filter: AdminOrderFilter = {}): Observable<any> {
    let params = new HttpParams();
    if (filter.status) params = params.set('status', filter.status);
    if (filter.start) params = params.set('start', filter.start);
    if (filter.end) params = params.set('end', filter.end);
    return this.http.get(`${this.API}`, { headers: this.authHeaders(), params });
  }

  updateStatus(id: string, status: string): Observable<ResponseEnvelope<any>> {
    return this.http.put<ResponseEnvelope<any>>(`${this.API}/${id}/status`, null, {
      headers: this.authHeaders(),
      params: new HttpParams().set('status', status)
    });
  }

  processCancel(id: string, approve: boolean): Observable<ResponseEnvelope<any>> {
    return this.http.put<ResponseEnvelope<any>>(`${this.API}/${id}/cancel-request`, null, {
      headers: this.authHeaders(),
      params: new HttpParams().set('approve', String(approve))
    });
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}

