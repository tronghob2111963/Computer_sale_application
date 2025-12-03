import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';
import { ResponseEnvelope } from './product.service';
import { PageResponse } from './review.service';

export interface CommentResponse {
  id: string;
  productId: string;
  productName?: string;
  userId?: string;
  userName?: string;
  content: string;
  status: string;
  createdAt: string;
  parentId?: string;
  replies?: CommentResponse[];
}

export interface CommentPayload {
  productId: string;
  content: string;
  parentId?: string;
}

export interface CommentSearchParams {
  keyword?: string;
  status?: string;
  productId?: string;
  pageNo?: number;
  pageSize?: number;
  sortBy?: string;
}

@Injectable({ providedIn: 'root' })
export class CommentService {
  private readonly API = `${environment.apiUrl}`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  getProductComments(productId: string): Observable<ResponseEnvelope<CommentResponse[]>> {
    return this.http.get<ResponseEnvelope<CommentResponse[]>>(`${this.API}/product/${productId}/comments`);
  }

  addComment(payload: CommentPayload): Observable<ResponseEnvelope<CommentResponse>> {
    return this.http.post<ResponseEnvelope<CommentResponse>>(`${this.API}/api/comments`, payload, {
      headers: this.authHeaders()
    });
  }

  adminSearch(params: CommentSearchParams = {}): Observable<ResponseEnvelope<PageResponse<CommentResponse[]>>> {
    return this.http.get<ResponseEnvelope<PageResponse<CommentResponse[]>>>(`${this.API}/api/admin/comments/search`, {
      params: this.buildParams(params),
      headers: this.authHeaders()
    });
  }

  updateStatus(id: string, status: string): Observable<ResponseEnvelope<CommentResponse>> {
    const params = new HttpParams().set('status', status);
    return this.http.put<ResponseEnvelope<CommentResponse>>(`${this.API}/api/admin/comments/${id}/status`, {}, {
      params,
      headers: this.authHeaders()
    });
  }

  private buildParams(params: CommentSearchParams): HttpParams {
    let hp = new HttpParams();
    if (params.keyword) hp = hp.set('keyword', params.keyword);
    if (params.status) hp = hp.set('status', params.status);
    if (params.productId) hp = hp.set('productId', params.productId);
    if (params.pageNo) hp = hp.set('pageNo', params.pageNo);
    if (params.pageSize) hp = hp.set('pageSize', params.pageSize);
    if (params.sortBy) hp = hp.set('sortBy', params.sortBy);
    return hp;
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    if (token && token !== 'undefined' && token !== 'null' && token.trim() !== '') {
      return new HttpHeaders({ Authorization: `Bearer ${token}` });
    }
    return new HttpHeaders();
  }
}
