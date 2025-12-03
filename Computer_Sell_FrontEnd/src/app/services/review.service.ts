import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';
import { ResponseEnvelope } from './product.service';

export interface ReviewResponse {
  id: string;
  productId: string;
  productName?: string;
  userId?: string;
  userName?: string;
  rating: number;
  comment: string;
  status: string;
  createdAt: string;
}

export interface ReviewSummary {
  averageRating: number;
  totalReviews: number;
  fiveStar: number;
  fourStar: number;
  threeStar: number;
  twoStar: number;
  oneStar: number;
}

export interface ProductReviewPayload {
  productId: string;
  rating: number;
  comment: string;
}

export interface ProductReviewList {
  reviews: ReviewResponse[];
  summary: ReviewSummary;
}

export interface ReviewSearchParams {
  keyword?: string;
  status?: string;
  productId?: string;
  rating?: number;
  pageNo?: number;
  pageSize?: number;
  sortBy?: string;
}

export interface PageResponse<T = any> {
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  items: T;
}

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly API = `${environment.apiUrl}`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  getProductReviews(productId: string): Observable<ResponseEnvelope<ProductReviewList>> {
    return this.http.get<ResponseEnvelope<ProductReviewList>>(`${this.API}/product/${productId}/reviews`);
  }

  submitReview(payload: ProductReviewPayload): Observable<ResponseEnvelope<ReviewResponse>> {
    return this.http.post<ResponseEnvelope<ReviewResponse>>(`${this.API}/api/reviews`, payload, {
      headers: this.authHeaders()
    });
  }

  adminSearch(params: ReviewSearchParams = {}): Observable<ResponseEnvelope<PageResponse<ReviewResponse[]>>> {
    return this.http.get<ResponseEnvelope<PageResponse<ReviewResponse[]>>>(`${this.API}/api/admin/reviews/search`, {
      params: this.buildParams(params),
      headers: this.authHeaders()
    });
  }

  updateStatus(id: string, status: string): Observable<ResponseEnvelope<ReviewResponse>> {
    const params = new HttpParams().set('status', status);
    return this.http.put<ResponseEnvelope<ReviewResponse>>(`${this.API}/api/admin/reviews/${id}/status`, {}, {
      params,
      headers: this.authHeaders()
    });
  }

  private buildParams(params: ReviewSearchParams): HttpParams {
    let hp = new HttpParams();
    if (params.keyword) hp = hp.set('keyword', params.keyword);
    if (params.status) hp = hp.set('status', params.status);
    if (params.productId) hp = hp.set('productId', params.productId);
    if (params.rating != null) hp = hp.set('rating', params.rating);
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
