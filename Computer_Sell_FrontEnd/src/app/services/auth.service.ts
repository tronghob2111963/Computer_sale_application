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
  ) { }

  login(payload: any): Observable<any> {
    return this.http.post(`${this.API_URL}/access-token`, payload);
  }

  saveTokens(tokenData: any): void {
    console.log('🔍 Saving tokens - Full tokenData:', tokenData);
    console.log('🔍 TokenData keys:', Object.keys(tokenData));

    // Save with explicit options
    const cookieOptions = {
      path: '/',
      sameSite: 'Lax' as 'Lax',
      secure: false // Set to true in production with HTTPS
    };

    this.cookieService.set('accessToken', tokenData.accessToken, { expires: 1, ...cookieOptions });
    this.cookieService.set('refreshToken', tokenData.refreshToken, { expires: 7, ...cookieOptions });
    this.cookieService.set('username', tokenData.username, { expires: 1, ...cookieOptions });

    const role = Array.isArray(tokenData.role) && tokenData.role.length > 0
      ? tokenData.role[0].replace(/[\[\]]/g, '').trim()
      : (tokenData.role || 'User');
    this.cookieService.set('role', role, { expires: 1, ...cookieOptions });

    // Backend returns 'id' field as UUID
    const userId = tokenData.id || tokenData.userId || tokenData.uid;
    console.log('🔍 Extracted userId:', userId);

    if (userId) {
      const userIdStr = String(userId);
      this.cookieService.set('userId', userIdStr, { expires: 7, ...cookieOptions });
      localStorage.setItem('userId', userIdStr);
      console.log('✅ Saved userId to cookie and localStorage:', userIdStr);
    } else {
      console.error('❌ No userId found in token data!');
    }

    // Also save to localStorage as backup
    localStorage.setItem('username', tokenData.username);
    localStorage.setItem('role', role);

    // Verify cookies were saved
    setTimeout(() => {
      const savedUserId = this.cookieService.get('userId');
      const localUserId = localStorage.getItem('userId');
      console.log('✅ Verification - Cookie userId:', savedUserId);
      console.log('✅ Verification - LocalStorage userId:', localUserId);
      console.log('✅ Verification - getUserIdSafe():', this.getUserIdSafe());
    }, 100);

    // 🔥 Cập nhật state toàn hệ thống
    this.authState.setUsername(tokenData.username);
    this.authState.setRole(role);
  }

  logout(): void {
    const accessToken = this.cookieService.get('accessToken');
    if (accessToken) {
      this.http.post(`${this.API_URL}/remove-token`, {}, {
        headers: { Authorization: `Bearer ${accessToken}` }
      }).subscribe();
    }

    // Clear cookies
    this.cookieService.deleteAll('/');

    // Clear localStorage
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');

    // Clear auth state
    this.authState.clear();
  }

  getUsername(): string {
    // Try cookie first
    let username = this.cookieService.get('username');
    console.log('Getting username from cookie:', username); // Debug log

    // Fallback to localStorage if cookie is empty or undefined
    if (!username || username === 'undefined' || username === 'null') {
      username = localStorage.getItem('username') || '';
      console.log('Fallback to localStorage username:', username); // Debug log
    }

    return username;
  }
  getRole(): string {
    // Try cookie first
    let role = this.cookieService.get('role');
    console.log('Getting role from cookie:', role); // Debug log

    // Fallback to localStorage if cookie is empty or undefined
    if (!role || role === 'undefined' || role === 'null') {
      role = localStorage.getItem('role') || '';
      console.log('Fallback to localStorage role:', role); // Debug log
    }

    return role;
  }
  getAccessToken(): string {
    return this.cookieService.get('accessToken');
  }
  getUserId(): string {
    const cookieId = this.cookieService.get('userId');
    if (cookieId && cookieId !== 'undefined' && cookieId !== 'null') {
      return cookieId;
    }
    // Fallback to localStorage
    const localId = localStorage.getItem('userId');
    return localId || '';
  }

  // Fallback method: derive userId from JWT when cookie is missing
  getUserIdSafe(): string {
    // Try cookie/localStorage first
    const id = this.getUserId();
    if (id) {
      console.log('✅ getUserIdSafe - Found userId:', id);
      return id;
    }

    console.log('⚠️ getUserIdSafe - No userId in cookie/localStorage, trying JWT...');

    // Try to extract from JWT token
    const token = this.getAccessToken();
    if (!token) {
      console.log('❌ getUserIdSafe - No access token found');
      return '';
    }

    const parts = token.split('.');
    if (parts.length < 2) {
      console.log('❌ getUserIdSafe - Invalid token format');
      return '';
    }

    try {
      const base64Url = parts[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + (4 - (base64.length % 4)) % 4, '=');
      const payload = JSON.parse(atob(padded)) as Record<string, any>;
      console.log('🔍 JWT Payload:', payload);

      const candidate = payload['userId'] || payload['uid'] || payload['id'] || payload['sub'];
      if (candidate) {
        console.log('✅ getUserIdSafe - Extracted from JWT:', candidate);
        // Save it for next time
        const userIdStr = String(candidate);
        this.cookieService.set('userId', userIdStr, { expires: 7, path: '/', sameSite: 'Lax' });
        localStorage.setItem('userId', userIdStr);
        return userIdStr;
      }

      console.log('❌ getUserIdSafe - No userId field in JWT');
      return '';
    } catch (error) {
      console.error('❌ getUserIdSafe - Error parsing JWT:', error);
      return '';
    }
  }
}
