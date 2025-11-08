import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface ResponseData<T> {
  status: number;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class ShippingOrderService {
  private readonly API = `${environment.apiUrl}/api/v1/admin/shipping-orders`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  getAllShippingOrders(): Observable<ResponseData<any[]>> {
    return this.http.get<ResponseData<any[]>>(this.API, { headers: this.authHeaders() });
  }

  getShippingOrderById(id: string): Observable<ResponseData<any>> {
    return this.http.get<ResponseData<any>>(`${this.API}/${id}`, { headers: this.authHeaders() });
  }

  exportShippingOrderToPdf(id: string): Observable<Blob> {
    return this.http.get(`${this.API}/${id}/export`, {
      headers: this.authHeaders(),
      responseType: 'blob'
    });
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}

