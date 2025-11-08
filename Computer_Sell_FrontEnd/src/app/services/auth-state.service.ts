import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  // Lưu trạng thái đăng nhập và username
  private usernameSubject = new BehaviorSubject<string>('');
  username$ = this.usernameSubject.asObservable();
  private roleSubject = new BehaviorSubject<string>('');
  role$ = this.roleSubject.asObservable();
  setUsername(username: string) {
    this.usernameSubject.next(username);
  }

  setRole(role: string): void {
    this.roleSubject.next(role);
  }

  clear() {
    this.usernameSubject.next('');
    this.roleSubject.next('');
  }
}
