import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, catchError, of, switchMap } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';
import { ResponseEnvelope } from './product.service';

export interface ProductTypeDTO {
  id: string;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class ProductTypeService {
  private readonly API_URL = `${environment.apiUrl}/product-types`;

  constructor(private http: HttpClient, private cookies: CookieService) {}

  listProductTypes(): Observable<ResponseEnvelope<ProductTypeDTO[] | { items: ProductTypeDTO[] }>> {
    const headers = this.authHeaders();
    // Try plural path first, then fallback to singular if backend differs
    return this.http.get<ResponseEnvelope<any>>(`${this.API_URL}/list`, { headers }).pipe(
      catchError(() => this.http.get<ResponseEnvelope<any>>(`${environment.apiUrl}/product-type/list`, { headers }))
    );
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
