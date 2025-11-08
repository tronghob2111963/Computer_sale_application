import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface ImportReceiptItemRequest {
  productId: string;
  quantity: number;
  importPrice: number;
}

export interface ImportReceiptRequest {
  employeeId: string;
  note?: string;
  items: ImportReceiptItemRequest[];
}

export interface ResponseEnvelope<T = any> {
  code: number;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class ImportReceiptService {
  private readonly API = `${environment.apiUrl}/api/import-receipts`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  create(request: ImportReceiptRequest): Observable<ResponseEnvelope<any>> {
    return this.http.post<ResponseEnvelope<any>>(this.API, request, {
      headers: this.authHeaders()
    });
  }

  list(): Observable<ResponseEnvelope<any[]>> {
    return this.http.get<ResponseEnvelope<any[]>>(this.API, {
      headers: this.authHeaders()
    });
  }

  getById(id: string): Observable<ResponseEnvelope<any>> {
    return this.http.get<ResponseEnvelope<any>>(`${this.API}/${id}`, {
      headers: this.authHeaders()
    });
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}

