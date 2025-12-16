import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent {
  user: any = {
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
    email: '',
    addresses: [
      {
        apartmentNumber: '',
        streetNumber: '',
        ward: '',
        city: '',
        addressType: 'HOME',
      },
    ],
  };

  toastMessage = '';
  toastType: 'success' | 'error' | null = null;

  private readonly API_URL = 'http://localhost:8080/user/register';

  constructor(private http: HttpClient, private router: Router) { }

  onSubmit() {
    this.http.post(this.API_URL, this.user).subscribe({
      next: () => {
        this.showToast('Đăng ký thành công 🎉', 'success');
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err) => {
        console.error('Lỗi đăng ký:', err);
        this.showToast('Đăng ký thất bại! Vui lòng kiểm tra lại 😢', 'error');
      },
    });
    if (this.user.password !== this.confirmPassword) {
      this.showToast('Mật khẩu xác nhận không khớp ⚠️', 'error');
      return;
    }
  }

  showToast(message: string, type: 'success' | 'error') {
    this.toastMessage = message;
    this.toastType = type;

    // Auto ẩn sau 3 giây
    setTimeout(() => {
      this.toastMessage = '';
      this.toastType = null;
    }, 3000);
  }
  showPassword = false;
  showConfirmPassword = false;
  confirmPassword = '';

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }


}
