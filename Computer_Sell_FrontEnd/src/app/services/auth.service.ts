import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { AuthStateService } from './auth-state.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_URL = 'http://localhost:8080/auth';

  constructor(
    private http: HttpClient,
    private cookieService: CookieService,
    private authState: AuthStateService
  ) {}

  login(payload: any): Observable<any> {
    return this.http.post(`${this.API_URL}/access-token`, payload);
  }

  saveTokens(tokenData: any): void {
    this.cookieService.set('accessToken', tokenData.accessToken, 1, '/');
    this.cookieService.set('refreshToken', tokenData.refreshToken, 7, '/');
    this.cookieService.set('username', tokenData.username, 1, '/');
        const role =
      Array.isArray(tokenData.role) && tokenData.role.length > 0
        ? tokenData.role[0].replace(/[\[\]]/g, '').trim() // Loại bỏ ký tự []
        : (tokenData.role || 'User');
    this.cookieService.set('role', role, 1, '/');
    // Try to persist userId if backend returns it under common keys
    const possibleId = tokenData.userId || tokenData.id || tokenData.uid;
    if (possibleId) {
      this.cookieService.set('userId', String(possibleId), 7, '/');
    }

    // 🔥 Cập nhật state toàn hệ thống
    this.authState.setUsername(tokenData.username);
    this.authState.setRole(role);
    // this.authState.setUsername(tokenData.username);
  }

  logout(): void {
    const accessToken = this.cookieService.get('accessToken');
    if (accessToken) {
      this.http.post(`${this.API_URL}/remove-token`, {}, {
        headers: { Authorization: `Bearer ${accessToken}` }
      }).subscribe();
    }

    this.cookieService.deleteAll('/');
    this.authState.clear();
  }

  getUsername(): string {
    return this.cookieService.get('username');
  }
  getRole(): string {
    return this.cookieService.get('role');
  }
  getAccessToken(): string {
    return this.cookieService.get('accessToken');
  }
  getUserId(): string { return this.cookieService.get('userId'); }

  // Fallback method: derive userId from JWT when cookie is missing
  getUserIdSafe(): string {
    const id = this.getUserId();
    if (id) return id;
    const token = this.getAccessToken();
    if (!token) return '';
    const parts = token.split('.');
    if (parts.length < 2) return '';
    try {
      const base64Url = parts[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + (4 - (base64.length % 4)) % 4, '=');
      const payload = JSON.parse(atob(padded)) as Record<string, any>;
      const candidate = payload['userId'] || payload['uid'] || payload['id'] || payload['sub'];
      return candidate ? String(candidate) : '';
    } catch {
      return '';
    }
  }
}
