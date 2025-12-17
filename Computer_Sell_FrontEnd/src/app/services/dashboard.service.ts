import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../enviroment';

export interface DailyRevenue {
    date: string;
    revenue: number;
    orders: number;
}

export interface DashboardStats {
    // Revenue stats
    totalRevenue: number;
    todayRevenue: number;
    weekRevenue: number;
    monthRevenue: number;

    // Order stats
    totalOrders: number;
    todayOrders: number;
    pendingOrders: number;
    confirmedOrders: number;
    shippingOrders: number;
    completedOrders: number;
    cancelledOrders: number;

    // Customer stats
    totalCustomers: number;
    newCustomersToday: number;
    newCustomersWeek: number;
    newCustomersMonth: number;

    // Revenue by payment method
    revenueByMethod: Record<string, number>;

    // Monthly revenue for chart (12 months)
    monthlyRevenue: number[];

    // Daily revenue for last 7 days
    dailyRevenue: DailyRevenue[];
}

export interface ResponseEnvelope<T> {
    code: number;
    message: string;
    data: T;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
    private readonly API = `${environment.apiUrl}/api/admin/dashboard`;

    constructor(private http: HttpClient, private cookies: CookieService) { }

    getStats(): Observable<ResponseEnvelope<DashboardStats>> {
        return this.http.get<ResponseEnvelope<DashboardStats>>(`${this.API}/stats`, {
            headers: this.authHeaders()
        });
    }

    getStatsByYear(year: number): Observable<ResponseEnvelope<DashboardStats>> {
        return this.http.get<ResponseEnvelope<DashboardStats>>(`${this.API}/stats/${year}`, {
            headers: this.authHeaders()
        });
    }

    private authHeaders(): HttpHeaders {
        const token = this.cookies.get('accessToken');
        return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
    }
}
