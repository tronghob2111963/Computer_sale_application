import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../enviroment';

export interface UserBuildDetail {
    productId: string;
    productName: string;
    price: number;
    quantity: number;
    imageUrl: string;
}

export interface UserBuild {
    id: string;
    name: string;
    totalPrice: number;
    isPublic: boolean;
    details: UserBuildDetail[];
}

export interface BuildResponse {
    status: number;
    message: string;
    data: UserBuild;
}

export interface BuildListResponse {
    status: number;
    message: string;
    data: UserBuild[];
}

@Injectable({
    providedIn: 'root'
})
export class UserBuildService {
    private apiUrl = `${environment.apiUrl}/builds`;

    constructor(private http: HttpClient) { }

    createBuild(userId: string, name: string): Observable<BuildResponse> {
        return this.http.post<BuildResponse>(`${this.apiUrl}/create`, { userId, name });
    }

    addProductToBuild(buildId: string, productId: string, quantity: number = 1): Observable<BuildResponse> {
        const params = new HttpParams()
            .set('productId', productId)
            .set('quantity', quantity.toString());
        return this.http.post<BuildResponse>(`${this.apiUrl}/${buildId}/add-product`, null, { params });
    }

    removeProductFromBuild(buildId: string, productId: string): Observable<BuildResponse> {
        return this.http.delete<BuildResponse>(`${this.apiUrl}/${buildId}/remove-product/${productId}`);
    }

    updateProductQuantity(buildId: string, productId: string, quantity: number): Observable<BuildResponse> {
        const params = new HttpParams()
            .set('productId', productId)
            .set('quantity', quantity.toString());
        return this.http.put<BuildResponse>(`${this.apiUrl}/${buildId}/update-quantity`, null, { params });
    }

    getUserBuilds(userId: string): Observable<BuildListResponse> {
        return this.http.get<BuildListResponse>(`${this.apiUrl}/user/${userId}`);
    }

    getBuildDetails(buildId: string): Observable<UserBuild> {
        return this.http.get<UserBuild>(`${this.apiUrl}/${buildId}`);
    }

    deleteBuild(buildId: string): Observable<any> {
        return this.http.delete(`${this.apiUrl}/${buildId}`);
    }

    getProductsByType(productTypeId: string, options?: { keyword?: string; page?: number; size?: number; sortBy?: string }): Observable<any> {
        let params = new HttpParams();
        if (options?.keyword) params = params.set('keyword', options.keyword);
        if (options?.page !== undefined) params = params.set('page', options.page.toString());
        if (options?.size !== undefined) params = params.set('size', options.size.toString());
        if (options?.sortBy) params = params.set('sortBy', options.sortBy);

        return this.http.get(`${this.apiUrl}/products/by-type/${productTypeId}`, { params });
    }

    getProductTypes(): Observable<any> {
        return this.http.get(`${environment.apiUrl}/product-types/list`);
    }
}
