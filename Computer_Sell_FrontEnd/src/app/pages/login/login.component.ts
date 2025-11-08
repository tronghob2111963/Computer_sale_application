import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  username = '';
  password = '';

  // Hiển thị mật khẩu
  showPassword = false;

  // Trạng thái loading
  isLoading = false;

  // Toast notification
  toastMessage = '';
  toastType: 'success' | 'error' | '' = '';
  showToast = false;

  constructor(private authService: AuthService, private router: Router) {}

  /**
   * Đăng nhập
   */
  onSubmit(): void {
    if (!this.username || !this.password) {
      this.showNotification('Vui lòng nhập đầy đủ thông tin!', 'error');
      return;
    }

    const payload = {
      username: this.username,
      password: this.password,
      platform: 'web',
      version: '1.0.0',
      deviceToken: 'vite-angular'
    };

    this.isLoading = true;
    this.authService.login(payload).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.authService.saveTokens(res);

        this.showNotification('Đăng nhập thành công 🎉', 'success');

        // Điều hướng về trang chủ sau 1.5s
      setTimeout(() => {
        const role = this.authService.getRole().toUpperCase();

        if (role.includes('SYSADMIN') || role.includes('ADMIN')) {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/']);
        }
      }, 1200);
      },
      error: () => {
        this.isLoading = false;
        this.showNotification('Sai tài khoản hoặc mật khẩu ', 'error');
      }
    });
  }
  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  /**
   * Hiển thị thông báo toast
   */
  showNotification(message: string, type: 'success' | 'error'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.showToast = true;

    setTimeout(() => (this.showToast = false), 3000);
  }

  /**
   * Ẩn/hiện mật khẩu
   */
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }
}

