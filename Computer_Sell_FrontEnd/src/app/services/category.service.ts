import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, catchError, of, switchMap } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';
import { PageResponse, ResponseEnvelope } from './product.service';

export interface CategoryDTO {
  id: string;
  name: string;
}

export interface CategoryListParams {
  keyword?: string;
  page?: number;
  size?: number;
  sortBy?: string;
}

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly API_URL_PRIMARY = `${environment.apiUrl}/category`;
  private readonly API_URL_FALLBACK = `${environment.apiUrl}/category`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  listCategories(params: CategoryListParams = {}): Observable<ResponseEnvelope<PageResponse<CategoryDTO>>> {
    const headers = this.authHeaders();
    const httpParams = this.buildParams(params);
    // Backend controller path: /category/list
    return this.http.get<ResponseEnvelope<PageResponse<CategoryDTO>>>(`${environment.apiUrl}/category/list`, {
      params: httpParams,
      headers
    });
  }

  createCategory(payload: { name: string; description?: string }): Observable<any> {
    // Backend may expose POST /category/save (similar to BrandController)
    return this.http.post(`${this.API_URL_PRIMARY}/save`, payload, { headers: this.authHeaders() });
  }

  getCategoryById(id: string): Observable<any> {
    return this.http.get(`${this.API_URL_PRIMARY}/get/${id}`, { headers: this.authHeaders() });
  }

  private buildParams(params: CategoryListParams): HttpParams {
    let httpParams = new HttpParams();
    if (params.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params.page !== undefined) httpParams = httpParams.set('page', String(params.page));
    if (params.size !== undefined) httpParams = httpParams.set('size', String(params.size));
    if (params.sortBy) httpParams = httpParams.set('sortBy', params.sortBy);
    return httpParams;
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
