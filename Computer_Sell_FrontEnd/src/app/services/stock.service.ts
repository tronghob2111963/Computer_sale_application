import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export type StockMovementType = 'IMPORT' | 'EXPORT' | 'RETURN' | 'ADJUSTMENT';

export interface StockHistoryResponse {
    id: string;
    productId: string;
    productName: string;
    movementType: StockMovementType;
    quantity: number;
    stockBefore: number;
    stockAfter: number;
    unitPrice: number;
    referenceId: string;
    referenceType: string;
    note: string;
    createdBy: string;
    movementDate: string;
    createdAt: string;
}

export interface StockAdjustmentRequest {
    productId: string;
    newStock: number;
    note?: string;
    createdBy: string;
}

export interface ResponseEnvelope<T = any> {
    code: number;
    message: string;
    data: T;
}

@Injectable({ providedIn: 'root' })
export class StockService {
    private readonly API = `${environment.apiUrl}/api/stock`;

    constructor(private http: HttpClient, private cookies: CookieService) { }

    // Lấy lịch sử kho theo sản phẩm
    getHistoryByProduct(productId: string): Observable<ResponseEnvelope<StockHistoryResponse[]>> {
        return this.http.get<ResponseEnvelope<StockHistoryResponse[]>>(
            `${this.API}/history/product/${productId}`,
            { headers: this.authHeaders() }
        );
    }

    // Lấy lịch sử kho theo loại biến động
    getHistoryByType(type: StockMovementType): Observable<ResponseEnvelope<StockHistoryResponse[]>> {
        return this.http.get<ResponseEnvelope<StockHistoryResponse[]>>(
            `${this.API}/history/type/${type}`,
            { headers: this.authHeaders() }
        );
    }

    // Lấy lịch sử kho theo khoảng thời gian
    getHistoryByDateRange(start: string, end: string): Observable<ResponseEnvelope<StockHistoryResponse[]>> {
        return this.http.get<ResponseEnvelope<StockHistoryResponse[]>>(
            `${this.API}/history/date-range`,
            {
                headers: this.authHeaders(),
                params: new HttpParams().set('start', start).set('end', end)
            }
        );
    }

    // Kiểm tra tồn kho hiện tại
    checkStock(productId: string): Observable<ResponseEnvelope<number>> {
        return this.http.get<ResponseEnvelope<number>>(
            `${this.API}/check/${productId}`,
            { headers: this.authHeaders() }
        );
    }

    // Kiểm tra tồn kho có đủ không
    checkStockAvailable(productId: string, quantity: number): Observable<ResponseEnvelope<boolean>> {
        return this.http.get<ResponseEnvelope<boolean>>(
            `${this.API}/check-available/${productId}`,
            {
                headers: this.authHeaders(),
                params: new HttpParams().set('quantity', quantity.toString())
            }
        );
    }

    // Điều chỉnh tồn kho (kiểm kê)
    adjustStock(request: StockAdjustmentRequest): Observable<ResponseEnvelope<null>> {
        return this.http.post<ResponseEnvelope<null>>(
            `${this.API}/adjust`,
            request,
            { headers: this.authHeaders() }
        );
    }

    private authHeaders(): HttpHeaders {
        const token = this.cookies.get('accessToken');
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }
}
