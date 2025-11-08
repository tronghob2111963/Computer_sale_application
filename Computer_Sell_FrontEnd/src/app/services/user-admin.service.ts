import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface UserDTO {
  id: string;
  username: string;
  firstName?: string;
  lastName?: string;
  gender?: string;
  dateOfBirth?: string;
  phoneNumber?: string;
  email?: string;
}

export interface UserListParams {
  keyword?: string;
  page?: number;
  size?: number;
  sortBy?: string;
}

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  private readonly API = `${environment.apiUrl}/user`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  listUsers(params: UserListParams = {}): Observable<any> {
    const httpParams = this.buildParams(params);
    return this.http.get<any>(`${this.API}/list`, { params: httpParams, headers: this.authHeaders() });
  }

  findById(id: string): Observable<any> {
    return this.http.get<any>(`${this.API}/find/${id}`, { headers: this.authHeaders() });
    }

  createUser(payload: any): Observable<any> {
    // Use /save to create Admin/Staff/Customer based on userType
    return this.http.post<any>(`${this.API}/save`, payload, { headers: this.authHeaders() });
  }

  updateUser(payload: any): Observable<any> {
    return this.http.post<any>(`${this.API}/update`, payload, { headers: this.authHeaders() });
  }

  deleteUser(id: string): Observable<any> {
    return this.http.delete<any>(`${this.API}/delete/${id}`, { headers: this.authHeaders() });
  }

  private buildParams(p: UserListParams): HttpParams {
    let params = new HttpParams();
    if (p.keyword) params = params.set('keyword', p.keyword);
    if (p.page != null) params = params.set('page', String(p.page));
    if (p.size != null) params = params.set('size', String(p.size));
    if (p.sortBy) params = params.set('sortBy', p.sortBy);
    return params;
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}

