import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

// Generic response envelope based on backend ResponseData/ResponseError
export interface ResponseEnvelope<T = any> {
  code: number;
  message: string;
  data: T;
}

// Product status enum
export type ProductStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED';

export interface PageResponse<T = any> {
  pageNo: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  items: T[];
}

export interface ListParams {
  keyword?: string;
  page?: number; // backend expects page index (0-based per controller default)
  size?: number;
  sortBy?: string; // e.g., "price:asc" or "name:desc"
}

export interface CreateProductPayload {
  id?: string; // UUID
  name: string;
  price: number;
  stock: number;
  warrantyPeriod?: number;
  description?: string;
  categoryId?: string; // UUID
  brandId?: string; // UUID
  productTypeId?: string; // UUID
  images?: File[]; // maps to backend field name "image"
}

export interface UpdateProductPayload extends Partial<CreateProductPayload> {
  id: string; // UUID
}

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly API_URL = `${environment.apiUrl}/product`;

  constructor(private http: HttpClient, private cookies: CookieService) { }

  // Create product (multipart/form-data)
  createProduct(payload: CreateProductPayload): Observable<ResponseEnvelope<string>> {
    const form = this.buildFormData(payload);
    return this.http.post<ResponseEnvelope<string>>(`${this.API_URL}/create`, form, {
      headers: this.authHeaders()
    });
  }

  // Update product (multipart/form-data)
  updateProduct(payload: UpdateProductPayload): Observable<ResponseEnvelope<string>> {
    const form = this.buildFormData(payload);
    form.append('id', payload.id);
    return this.http.post<ResponseEnvelope<string>>(`${this.API_URL}/update`, form, {
      headers: this.authHeaders()
    });
  }

  // Delete product (hard delete - không khuyến khích)
  deleteProduct(id: string): Observable<ResponseEnvelope<null>> {
    return this.http.delete<ResponseEnvelope<null>>(`${this.API_URL}/delete/${id}`, {
      headers: this.authHeaders()
    });
  }

  // Soft delete product - chuyển trạng thái sang DELETED
  softDeleteProduct(id: string): Observable<ResponseEnvelope<null>> {
    return this.http.put<ResponseEnvelope<null>>(`${this.API_URL}/soft-delete/${id}`, null, {
      headers: this.authHeaders()
    });
  }

  // Khôi phục sản phẩm đã xóa mềm
  restoreProduct(id: string): Observable<ResponseEnvelope<null>> {
    return this.http.put<ResponseEnvelope<null>>(`${this.API_URL}/restore/${id}`, null, {
      headers: this.authHeaders()
    });
  }

  // Cập nhật trạng thái sản phẩm (ACTIVE/INACTIVE/DELETED)
  updateProductStatus(id: string, status: ProductStatus): Observable<ResponseEnvelope<null>> {
    return this.http.put<ResponseEnvelope<null>>(
      `${this.API_URL}/status/${id}`,
      null,
      {
        headers: this.authHeaders(),
        params: new HttpParams().set('status', status)
      }
    );
  }

  // Product detail
  getProductDetail<T = any>(id: string): Observable<ResponseEnvelope<T>> {
    return this.http.get<ResponseEnvelope<T>>(`${this.API_URL}/detail/${id}`, {
      headers: this.authHeaders()
    });
  }

  // List all products
  listProducts<T = any>(params: ListParams = {}): Observable<ResponseEnvelope<PageResponse<T>>> {
    return this.http.get<ResponseEnvelope<PageResponse<T>>>(`${this.API_URL}/list`, {
      params: this.buildParams(params),
      headers: this.authHeaders()
    });
  }

  // List by brand
  listProductsByBrand<T = any>(brandId: string, params: ListParams = {}): Observable<ResponseEnvelope<PageResponse<T>>> {
    return this.http.get<ResponseEnvelope<PageResponse<T>>>(`${this.API_URL}/list/brand/${brandId}`, {
      params: this.buildParams(params),
      headers: this.authHeaders()
    });
  }

  // List by category
  listProductsByCategory<T = any>(categoryId: string, params: ListParams = {}): Observable<ResponseEnvelope<PageResponse<T>>> {
    return this.http.get<ResponseEnvelope<PageResponse<T>>>(`${this.API_URL}/list/category/${categoryId}`, {
      params: this.buildParams(params),
      headers: this.authHeaders()
    });
  }

  // List by product type (paginated, matches controller `/filter/product-type/{id}`)
  listProductsByProductType<T = any>(productTypeId: string, params: ListParams = {}): Observable<ResponseEnvelope<PageResponse<T>>> {
    return this.http.get<ResponseEnvelope<PageResponse<T>>>(`${this.API_URL}/filter/product-type/${productTypeId}`, {
      params: this.buildParams(params),
      headers: this.authHeaders()
    });
  }

  // Utilities
  private buildFormData(data: Partial<CreateProductPayload>): FormData {
    const form = new FormData();
    if (data.name != null) form.append('name', String(data.name));
    if (data.price != null) form.append('price', String(data.price));
    if (data.stock != null) form.append('stock', String(data.stock));
    if (data.warrantyPeriod != null) form.append('warrantyPeriod', String(data.warrantyPeriod));
    if (data.description != null) form.append('description', data.description);
    if (data.categoryId) form.append('categoryId', data.categoryId);
    if (data.brandId) form.append('brandId', data.brandId);
    if (data.productTypeId) form.append('productTypeId', data.productTypeId);

    if (data.images && data.images.length) {
      for (const file of data.images) {
        form.append('image', file);
      }
    }

    return form;
  }

  private buildParams(params: ListParams): HttpParams {
    let httpParams = new HttpParams();
    if (params.keyword) httpParams = httpParams.set('keyword', params.keyword);
    if (params.page !== undefined) httpParams = httpParams.set('page', String(params.page));
    if (params.size !== undefined) httpParams = httpParams.set('size', String(params.size));
    if (params.sortBy) httpParams = httpParams.set('sortBy', params.sortBy);
    return httpParams;
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    // Only add Authorization header if token exists and is not empty
    if (token && token !== 'undefined' && token !== 'null' && token.trim() !== '') {
      return new HttpHeaders({ Authorization: `Bearer ${token}` });
    }
    return new HttpHeaders();
  }
}
