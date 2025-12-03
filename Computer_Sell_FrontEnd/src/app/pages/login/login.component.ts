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

  constructor(private authService: AuthService, private router: Router) { }

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
        console.log('Login response:', res); // Debug: Check backend response

        // Handle both wrapped and unwrapped responses
        const tokenData = res.data || res;
        console.log('Token data:', tokenData); // Debug
        console.log('Username:', tokenData.username); // Debug: Check username field

        this.authService.saveTokens(tokenData);

        this.showNotification('Đăng nhập thành công 🎉', 'success');

        // Điều hướng sau khi cookies được lưu
        setTimeout(() => {
          // Debug: Check what was saved
          const savedUsername = this.authService.getUsername();
          const savedRole = this.authService.getRole();
          console.log('Saved username:', savedUsername);
          console.log('Saved role:', savedRole);
          console.log('Saved role (raw):', tokenData.role);

          // Normalize role for comparison
          const roleUpper = savedRole.toUpperCase();
          console.log('Role uppercase:', roleUpper);

          // Check if user is admin (case-insensitive, multiple variations)
          const isAdmin = roleUpper.includes('SYSADMIN') ||
            roleUpper.includes('ADMIN') ||
            roleUpper.includes('SYS_ADMIN') ||
            roleUpper.includes('SYSTEMADMIN');

          if (isAdmin) {
            console.log('✅ Admin detected - Redirecting to /admin');
            window.location.href = '/admin';
          } else {
            console.log('👤 Regular user - Redirecting to /');
            window.location.href = '/';
          }
        }, 1500); // Tăng thời gian chờ lên 1.5s
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

