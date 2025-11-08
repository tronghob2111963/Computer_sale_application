import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface CartItem {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  subtotal: number;
  productImg?: string;
}

export interface CartDTO {
  cartId: string;
  userId: string;
  totalPrice: number;
  status: string;
  items: CartItem[];
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly API_URL = `${environment.apiUrl}/cart`;
  // Emits whenever cart content changes (add/update/remove)
  cartUpdated$ = new Subject<void>();

  constructor(private http: HttpClient, private cookies: CookieService) {}

  addToCart(userId: string, productId: string, quantity = 1): Observable<CartDTO> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('productId', productId)
      .set('quantity', String(quantity));
    return this.http
      .post<CartDTO>(`${this.API_URL}/add`, {}, { params, headers: this.authHeaders() })
      .pipe(tap(() => this.cartUpdated$.next()));
  }

  viewCart(userId: string): Observable<CartDTO> {
    return this.http.get<CartDTO>(`${this.API_URL}/${userId}`, { headers: this.authHeaders() });
  }

  updateQuantity(userId: string, productId: string, quantity: number): Observable<CartDTO> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('productId', productId)
      .set('quantity', String(quantity));
    return this.http
      .put<CartDTO>(`${this.API_URL}/update`, {}, { params, headers: this.authHeaders() })
      .pipe(tap(() => this.cartUpdated$.next()));
  }

  removeItem(userId: string, productId: string): Observable<CartDTO> {
    const params = new HttpParams().set('userId', userId).set('productId', productId);
    return this.http
      .delete<CartDTO>(`${this.API_URL}/remove`, { params, headers: this.authHeaders() })
      .pipe(tap(() => this.cartUpdated$.next()));
  }

  private authHeaders(): HttpHeaders {
    const token = this.cookies.get('accessToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
