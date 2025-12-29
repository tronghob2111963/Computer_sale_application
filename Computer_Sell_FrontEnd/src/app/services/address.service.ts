import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface AddressDTO {
    id?: string;
    apartmentNumber?: string;
    streetNumber?: string;
    ward?: string;
    city?: string;
    addressType?: string;
    fullAddress?: string;
}

export interface AddressRequest {
    apartmentNumber: string;
    streetNumber: string;
    ward: string;
    city: string;
    addressType: string;
}

@Injectable({ providedIn: 'root' })
export class AddressService {
    private readonly API = `${environment.apiUrl}/address`;

    constructor(private http: HttpClient, private cookies: CookieService) { }

    // Get all addresses by user ID
    getAddressesByUserId(userId: string): Observable<any> {
        return this.http.get<any>(`${this.API}/user/${userId}`, { headers: this.authHeaders() });
    }

    // Get address by ID
    getAddressById(addressId: string): Observable<any> {
        return this.http.get<any>(`${this.API}/${addressId}`, { headers: this.authHeaders() });
    }

    // Create new address
    createAddress(userId: string, address: AddressRequest): Observable<any> {
        return this.http.post<any>(`${this.API}/user/${userId}`, address, { headers: this.authHeaders() });
    }

    // Update address
    updateAddress(userId: string, addressId: string, address: AddressRequest): Observable<any> {
        return this.http.put<any>(`${this.API}/user/${userId}/${addressId}`, address, { headers: this.authHeaders() });
    }

    // Delete address
    deleteAddress(userId: string, addressId: string): Observable<any> {
        return this.http.delete<any>(`${this.API}/user/${userId}/${addressId}`, { headers: this.authHeaders() });
    }

    private authHeaders(): HttpHeaders {
        const token = this.cookies.get('accessToken');
        if (token && token !== 'undefined' && token !== 'null' && token.trim() !== '') {
            return new HttpHeaders({ Authorization: `Bearer ${token}` });
        }
        return new HttpHeaders();
    }
}
