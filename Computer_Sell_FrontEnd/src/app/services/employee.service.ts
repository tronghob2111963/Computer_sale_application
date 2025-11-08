import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';
import { PageResponse } from './product.service';

export interface EmployeeResponse {
  id: string;
  userId: string;
  username: string;
  position: string;
  salary: number;
  hireDate: string;
  terminateDate?: string | null;
  status: 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE' | 'RESIGNED';
  note?: string | null;
}

export interface EmployeeRequest {
  userId: string;
  position: string;
  salary: number;
  hireDate?: string; // ISO date string
  terminateDate?: string | null;
  note?: string | null;
  status?: 'ACTIVE' | 'INACTIVE' | 'ON_LEAVE' | 'RESIGNED';
}

export interface EmployeeListParams {
  keyword?: string;
  pageNo?: number; // 1-based per backend
  pageSize?: number;
  sortBy?: string; // e.g. createdAt:desc
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly API = `${environment.apiUrl}/api/employees`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  listEmployees(params: EmployeeListParams = {}): Observable<PageResponse<EmployeeResponse>> {
    const httpParams = this.buildParams(params);
    return this.http.get<PageResponse<EmployeeResponse>>(this.API, { params: httpParams, headers: this.authHeaders() });
  }

  getEmployeeById(id: string): Observable<EmployeeResponse> {
    return this.http.get<EmployeeResponse>(`${this.API}/${id}`, { headers: this.authHeaders() });
  }

  createEmployee(payload: EmployeeRequest): Observable<EmployeeResponse> {
    return this.http.post<EmployeeResponse>(this.API, payload, { headers: this.authHeaders() });
  }

  updateEmployee(id: string, payload: EmployeeRequest): Observable<EmployeeResponse> {
    return this.http.put<EmployeeResponse>(`${this.API}/${id}`, payload, { headers: this.authHeaders() });
  }

  updateStatus(id: string, status: string): Observable<string> {
    const params = new HttpParams().set('status', status);
    return this.http.patch(`${this.API}/${id}/status`, null, { params, headers: this.authHeaders(), responseType: 'text' });
  }

  // Get employeeId by userId (backend returns UUID as plain string)
  getEmployeeIdByUserId(userId: string): Observable<string> {
    return this.http.get(`${this.API}/user/${userId}`, { headers: this.authHeaders(), responseType: 'text' });
  }

  private buildParams(p: EmployeeListParams): HttpParams {
    let params = new HttpParams();
    if (p.keyword) params = params.set('keyword', p.keyword);
    if (p.pageNo != null) params = params.set('pageNo', String(p.pageNo));
    if (p.pageSize != null) params = params.set('pageSize', String(p.pageSize));
    if (p.sortBy) params = params.set('sortBy', p.sortBy);
    return params;
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
