import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';
import { PageResponse, ResponseEnvelope } from './product.service';

export interface BrandDTO {
  id: string;
  name: string;
  country?: string;
  description?: string;
}

export interface BrandListParams {
  keyword?: string;
  page?: number;
  size?: number;
  sortBy?: string;
}

@Injectable({ providedIn: 'root' })
export class BrandService {
  private readonly API_URL = `${environment.apiUrl}/brand`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  listBrands(params: BrandListParams = {}): Observable<ResponseEnvelope<PageResponse<BrandDTO>>> {
    return this.http.get<ResponseEnvelope<PageResponse<BrandDTO>>>(`${this.API_URL}/list-branch`, {
      params: this.buildParams(params),
      headers: this.authHeaders()
    });
  }

  createBrand(payload: { name: string; country?: string; description?: string }): Observable<ResponseEnvelope<any>> {
    return this.http.post<ResponseEnvelope<any>>(`${this.API_URL}/save`, payload, { headers: this.authHeaders() });
  }

  getBrandById(id: string): Observable<ResponseEnvelope<BrandDTO>> {
    return this.http.get<ResponseEnvelope<BrandDTO>>(`${this.API_URL}/get/${id}`, { headers: this.authHeaders() });
  }

  private buildParams(params: BrandListParams): HttpParams {
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
