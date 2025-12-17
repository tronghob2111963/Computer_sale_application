import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface UserProfile {
    id: string;
    username: string;
    firstName?: string;
    lastName?: string;
    gender?: string;
    dateOfBirth?: string;
    phoneNumber?: string;
    email?: string;
    userType?: string;
    status?: string;
    createdAt?: string;
    updatedAt?: string;
    addresses?: AddressDTO[];
}

export interface AddressDTO {
    id?: string;
    apartmentNumber?: string;
    streetNumber?: string;
    ward?: string;
    city?: string;
    addressType?: string;
}

export interface UpdateProfilePayload {
    firstName?: string;
    lastName?: string;
    gender?: string;
    dateOfBirth?: string;
    phoneNumber?: string;
    email?: string;
}

export interface ChangePasswordPayload {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
    private readonly API = `${environment.apiUrl}`;

    constructor(private http: HttpClient, private cookies: CookieService) { }

    getMyProfile(userId: string): Observable<any> {
        return this.http.get<any>(`${this.API}/user/find/${userId}`, { headers: this.authHeaders() });
    }

    updateProfile(payload: UpdateProfilePayload): Observable<any> {
        return this.http.post<any>(`${this.API}/user/update`, payload, { headers: this.authHeaders() });
    }

    changePassword(payload: ChangePasswordPayload): Observable<any> {
        return this.http.post<any>(`${this.API}/auth/change-password`, payload, { headers: this.authHeaders() });
    }

    addAddress(userId: string, address: AddressDTO): Observable<any> {
        return this.http.post<any>(`${this.API}/user/${userId}/address`, address, { headers: this.authHeaders() });
    }

    updateAddress(userId: string, addressId: string, address: AddressDTO): Observable<any> {
        return this.http.put<any>(`${this.API}/user/${userId}/address/${addressId}`, address, { headers: this.authHeaders() });
    }

    deleteAddress(userId: string, addressId: string): Observable<any> {
        return this.http.delete<any>(`${this.API}/user/${userId}/address/${addressId}`, { headers: this.authHeaders() });
    }

    private authHeaders(): HttpHeaders {
        const token = this.cookies.get('accessToken');
        if (token && token !== 'undefined' && token !== 'null' && token.trim() !== '') {
            return new HttpHeaders({ Authorization: `Bearer ${token}` });
        }
        return new HttpHeaders();
    }
}
