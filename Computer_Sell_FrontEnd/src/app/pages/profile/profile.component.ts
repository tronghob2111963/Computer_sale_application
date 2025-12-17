import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserService, UserProfile, UpdateProfilePayload, AddressDTO } from '../../services/user.service';
import { UserAdminService } from '../../services/user-admin.service';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
    loading = true;
    saving = false;
    activeTab: 'info' | 'address' | 'password' | 'orders' = 'info';

    profile: UserProfile | null = null;
    editMode = false;

    // Edit form
    editForm: UpdateProfilePayload = {
        firstName: '',
        lastName: '',
        gender: '',
        dateOfBirth: '',
        phoneNumber: '',
        email: ''
    };

    // Password form
    passwordForm = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    };
    changingPassword = false;

    // Address
    addresses: AddressDTO[] = [];
    editingAddress: AddressDTO | null = null;
    showAddressModal = false;
    addressForm: AddressDTO = {
        apartmentNumber: '',
        streetNumber: '',
        ward: '',
        city: '',
        addressType: 'HOME'
    };
    savingAddress = false;

    // Toast
    toast = { show: false, type: '' as 'success' | 'error', message: '' };

    constructor(
        private authService: AuthService,
        private userService: UserService,
        private userAdminService: UserAdminService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.loadProfile();
    }

    loadProfile(): void {
        this.loading = true;
        const userId = this.authService.getUserIdSafe();

        if (!userId) {
            this.showToast('Vui lòng đăng nhập để xem hồ sơ', 'error');
            this.router.navigate(['/login']);
            return;
        }

        this.userAdminService.findById(userId).subscribe({
            next: (res) => {
                const data = res?.data || res;
                this.profile = data;
                this.addresses = data?.addresses || [];
                this.resetEditForm();
                this.loading = false;
            },
            error: (err) => {
                // Nếu API find cũng lỗi, thử lấy thông tin từ auth
                console.error('Load profile error:', err);
                this.profile = {
                    id: userId,
                    username: this.authService.getUsername() || '',
                    status: 'ACTIVE'
                };
                this.resetEditForm();
                this.loading = false;
            }
        });
    }

    resetEditForm(): void {
        if (this.profile) {
            this.editForm = {
                firstName: this.profile.firstName || '',
                lastName: this.profile.lastName || '',
                gender: this.profile.gender || '',
                dateOfBirth: this.profile.dateOfBirth || '',
                phoneNumber: this.profile.phoneNumber || '',
                email: this.profile.email || ''
            };
        }
    }

    toggleEdit(): void {
        this.editMode = !this.editMode;
        if (!this.editMode) {
            this.resetEditForm();
        }
    }

    saveProfile(): void {
        if (!this.profile) return;
        this.saving = true;

        const payload = {
            id: this.profile.id,
            ...this.editForm
        };

        this.userService.updateProfile(this.editForm).subscribe({
            next: () => {
                this.showToast('Cập nhật hồ sơ thành công!', 'success');
                this.editMode = false;
                this.saving = false;
                this.loadProfile();
            },
            error: (err) => {
                this.showToast(err?.error?.message || 'Cập nhật thất bại', 'error');
                this.saving = false;
            }
        });
    }

    changePassword(): void {
        if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
            this.showToast('Mật khẩu xác nhận không khớp', 'error');
            return;
        }

        if (this.passwordForm.newPassword.length < 6) {
            this.showToast('Mật khẩu mới phải có ít nhất 6 ký tự', 'error');
            return;
        }

        this.changingPassword = true;
        this.userService.changePassword(this.passwordForm).subscribe({
            next: () => {
                this.showToast('Đổi mật khẩu thành công!', 'success');
                this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
                this.changingPassword = false;
            },
            error: (err) => {
                this.showToast(err?.error?.message || 'Đổi mật khẩu thất bại', 'error');
                this.changingPassword = false;
            }
        });
    }


    // Address methods
    openAddressModal(address?: AddressDTO): void {
        if (address) {
            this.editingAddress = address;
            this.addressForm = { ...address };
        } else {
            this.editingAddress = null;
            this.addressForm = {
                apartmentNumber: '',
                streetNumber: '',
                ward: '',
                city: '',
                addressType: 'HOME'
            };
        }
        this.showAddressModal = true;
    }

    closeAddressModal(): void {
        this.showAddressModal = false;
        this.editingAddress = null;
    }

    saveAddress(): void {
        if (!this.profile?.id) return;
        this.savingAddress = true;

        if (this.editingAddress?.id) {
            this.userService.updateAddress(this.profile.id, this.editingAddress.id, this.addressForm).subscribe({
                next: () => {
                    this.showToast('Cập nhật địa chỉ thành công!', 'success');
                    this.closeAddressModal();
                    this.savingAddress = false;
                    this.loadProfile();
                },
                error: () => {
                    this.showToast('Cập nhật địa chỉ thất bại', 'error');
                    this.savingAddress = false;
                }
            });
        } else {
            this.userService.addAddress(this.profile.id, this.addressForm).subscribe({
                next: () => {
                    this.showToast('Thêm địa chỉ thành công!', 'success');
                    this.closeAddressModal();
                    this.savingAddress = false;
                    this.loadProfile();
                },
                error: () => {
                    this.showToast('Thêm địa chỉ thất bại', 'error');
                    this.savingAddress = false;
                }
            });
        }
    }

    deleteAddress(address: AddressDTO): void {
        if (!address.id || !this.profile?.id) return;
        if (!confirm('Bạn có chắc muốn xóa địa chỉ này?')) return;

        this.userService.deleteAddress(this.profile.id, address.id).subscribe({
            next: () => {
                this.showToast('Đã xóa địa chỉ', 'success');
                this.loadProfile();
            },
            error: () => {
                this.showToast('Xóa địa chỉ thất bại', 'error');
            }
        });
    }

    // Helpers
    getInitials(): string {
        if (!this.profile) return '?';
        const first = this.profile.firstName?.charAt(0) || '';
        const last = this.profile.lastName?.charAt(0) || '';
        return (first + last).toUpperCase() || this.profile.username?.charAt(0)?.toUpperCase() || '?';
    }

    getFullName(): string {
        if (!this.profile) return '';
        const parts = [this.profile.firstName, this.profile.lastName].filter(Boolean);
        return parts.join(' ') || this.profile.username || '';
    }

    getGenderLabel(gender?: string): string {
        const map: Record<string, string> = {
            'MALE': 'Nam',
            'FEMALE': 'Nữ',
            'OTHER': 'Khác'
        };
        return map[gender || ''] || 'Chưa cập nhật';
    }

    formatDate(date?: string): string {
        if (!date) return 'Chưa cập nhật';
        return new Date(date).toLocaleDateString('vi-VN');
    }

    private showToast(message: string, type: 'success' | 'error' = 'success'): void {
        this.toast = { show: true, type, message };
        setTimeout(() => this.toast.show = false, 3000);
    }
}
