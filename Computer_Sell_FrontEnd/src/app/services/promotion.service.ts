import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface PromotionRequest {
  promoCode: string;
  description?: string;
  discountPercent: number;
  startDate: string; // ISO date string yyyy-MM-dd
  endDate: string;   // ISO date string yyyy-MM-dd
  isActive?: boolean;
}

export interface PromotionResponse {
  id: string;
  promoCode: string;
  description?: string;
  discountPercent: number;
  startDate: string; // ISO
  endDate: string;   // ISO
  isActive: boolean;
}

export interface ResponseEnvelope<T = any> {
  code: number;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class PromotionService {
  private readonly API = `${environment.apiUrl}/api/promotions`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  create(payload: PromotionRequest): Observable<ResponseEnvelope<PromotionResponse>> {
    return this.http.post<ResponseEnvelope<PromotionResponse>>(`${this.API}/create`, payload, { headers: this.authHeaders() });
  }

  update(id: string, payload: PromotionRequest): Observable<ResponseEnvelope<PromotionResponse>> {
    return this.http.put<ResponseEnvelope<PromotionResponse>>(`${this.API}/update/${id}`, payload, { headers: this.authHeaders() });
  }

  delete(id: string): Observable<ResponseEnvelope<null>> {
    return this.http.delete<ResponseEnvelope<null>>(`${this.API}/delete/${id}`, { headers: this.authHeaders() });
  }

  listAll(): Observable<ResponseEnvelope<PromotionResponse[]>> {
    return this.http.get<ResponseEnvelope<PromotionResponse[]>>(`${this.API}/get-all`, { headers: this.authHeaders() });
  }

  getByCode(code: string): Observable<ResponseEnvelope<PromotionResponse>> {
    return this.http.get<ResponseEnvelope<PromotionResponse>>(`${this.API}/get-by-code/${encodeURIComponent(code)}`);
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}

