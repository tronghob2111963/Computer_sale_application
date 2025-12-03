import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface ImportReceiptItemRequest {
  productId: string;
  quantity: number;
  importPrice: number;
}

export interface ImportReceiptRequest {
  employeeId?: string; // Optional - sẽ tự động lấy từ user đang đăng nhập
  note?: string;
  items: ImportReceiptItemRequest[];
}

export interface ImportReceiptDetail {
  productName: string;
  quantity: number;
  importPrice: number;
}

export interface ImportReceiptResponse {
  id: string;
  receiptId: string;
  receiptCode: string;
  employeeId: string;
  employeeName: string;
  totalAmount: number;
  details: ImportReceiptDetail[];
  note: string;
  receiptDate: string;  // Ngày nhập hàng
  status: ImportReceiptStatus;
  createdAt: string;
}

export type ImportReceiptStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED';

export interface ResponseEnvelope<T = any> {
  code: number;
  message: string;
  data: T;
}

@Injectable({ providedIn: 'root' })
export class ImportReceiptService {
  private readonly API = `${environment.apiUrl}/api/import-receipts`;

  constructor(private http: HttpClient, private cookies: CookieService) { }

  // Tạo phiếu nhập kho
  create(request: ImportReceiptRequest): Observable<ResponseEnvelope<ImportReceiptResponse>> {
    return this.http.post<ResponseEnvelope<ImportReceiptResponse>>(this.API, request, {
      headers: this.authHeaders()
    });
  }

  // Lấy danh sách tất cả phiếu nhập
  list(): Observable<ResponseEnvelope<ImportReceiptResponse[]>> {
    return this.http.get<ResponseEnvelope<ImportReceiptResponse[]>>(this.API, {
      headers: this.authHeaders()
    });
  }

  // Lấy chi tiết phiếu nhập theo ID
  getById(id: string): Observable<ResponseEnvelope<ImportReceiptResponse>> {
    return this.http.get<ResponseEnvelope<ImportReceiptResponse>>(`${this.API}/${id}`, {
      headers: this.authHeaders()
    });
  }

  // Hủy phiếu nhập (hoàn lại kho)
  cancel(id: string, cancelledBy: string): Observable<ResponseEnvelope<null>> {
    return this.http.put<ResponseEnvelope<null>>(
      `${this.API}/${id}/cancel`,
      null,
      {
        headers: this.authHeaders(),
        params: new HttpParams().set('cancelledBy', cancelledBy)
      }
    );
  }

  // Lọc phiếu nhập theo trạng thái
  filterByStatus(status: ImportReceiptStatus): Observable<ResponseEnvelope<ImportReceiptResponse[]>> {
    return this.http.get<ResponseEnvelope<ImportReceiptResponse[]>>(
      `${this.API}/filter/status/${status}`,
      { headers: this.authHeaders() }
    );
  }

  // Lọc phiếu nhập theo khoảng thời gian
  filterByDateRange(start: string, end: string): Observable<ResponseEnvelope<ImportReceiptResponse[]>> {
    return this.http.get<ResponseEnvelope<ImportReceiptResponse[]>>(
      `${this.API}/filter/date-range`,
      {
        headers: this.authHeaders(),
        params: new HttpParams().set('start', start).set('end', end)
      }
    );
  }

  // Lọc phiếu nhập theo nhân viên
  filterByEmployee(employeeId: string): Observable<ResponseEnvelope<ImportReceiptResponse[]>> {
    return this.http.get<ResponseEnvelope<ImportReceiptResponse[]>>(
      `${this.API}/filter/employee/${employeeId}`,
      { headers: this.authHeaders() }
    );
  }

  // Lấy employeeId của user đang đăng nhập
  getCurrentEmployeeId(): Observable<ResponseEnvelope<string>> {
    return this.http.get<ResponseEnvelope<string>>(
      `${this.API}/current-employee`,
      { headers: this.authHeaders() }
    );
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}

