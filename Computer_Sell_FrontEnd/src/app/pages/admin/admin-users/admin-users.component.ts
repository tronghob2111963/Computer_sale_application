import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserAdminService, UserDTO } from '../../../services/user-admin.service';
import { Router } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-admin-users',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.scss']
})
export class AdminUsersComponent implements OnInit {
  loading = false;
  error: string | null = null;
  users: UserDTO[] = [];

  keyword = '';
  page = 0;
  size = 10;
  sortBy = 'createdAt:desc';
  roleId: number | null = null;
  pageNo = 1;
  totalPages = 0;
  totalElements = 0;

  roles = [
    { id: null, name: 'Tất cả' },
    { id: 1, name: 'SysAdmin' },
    { id: 2, name: 'Admin' },
    { id: 3, name: 'Staff' },
    { id: 4, name: 'User' }
  ];

  showModal = false;
  isEditing = false;
  showDetailModal = false;
  userDetail: any = null;
  form: any = {
    id: '', username: '', password: '', firstName: '', lastName: '', gender: 'MALE', dateOfBirth: '', phoneNumber: '', email: '', userType: 'CUSTOMER'
  };

  constructor(private userService: UserAdminService, private router: Router) { }

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true; this.error = null;
    this.userService.listUsers({
      keyword: this.keyword || undefined,
      page: this.page,
      size: this.size,
      sortBy: this.sortBy,
      roleId: this.roleId || undefined
    }).subscribe({
      next: (res) => {
        // BE format: ResponseData { code, message, data: { data: PageResponse } }
        const layer1: any = (res as any)?.data ?? res;
        const page: any = layer1?.data ?? layer1?.page ?? layer1;
        const items: any[] = Array.isArray(page?.items)
          ? page.items
          : (Array.isArray(page?.content) ? page.content : (Array.isArray(page) ? page : []));

        this.users = items as any[];
        this.pageNo = page?.pageNo ?? (this.page + 1);
        this.totalPages = page?.totalPages ?? 1;
        this.totalElements = page?.totalElements ?? this.users.length;
        this.loading = false;
      },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Không tải được danh sách người dùng'; }
    });
  }

  search() { this.page = 0; this.load(); }
  changePage(delta: number) { const next = this.page + delta; if (next < 0 || (this.totalPages && next >= this.totalPages)) return; this.page = next; this.load(); }
  changePageSize() { this.page = 0; this.load(); }

  openCreate() { this.isEditing = false; this.showModal = true; this.form = { username: '', password: '', firstName: '', lastName: '', gender: 'MALE', dateOfBirth: '', phoneNumber: '', email: '', userType: 'CUSTOMER' }; }
  openEdit(u: UserDTO) { this.isEditing = true; this.showModal = true; this.form = { id: u.id, username: u.username, firstName: u.firstName || '', lastName: u.lastName || '', gender: u.gender || 'MALE', dateOfBirth: u.dateOfBirth || '', phoneNumber: u.phoneNumber || '', email: u.email || '' }; }

  openDetail(u: UserDTO) {
    this.loading = true;
    this.userService.findDetailById(u.id).subscribe({
      next: (res) => {
        this.userDetail = res?.data || res;
        this.showDetailModal = true;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Không thể tải thông tin chi tiết';
      }
    });
  }

  closeDetail() {
    this.showDetailModal = false;
    this.userDetail = null;
  }

  getAddressTypeLabel(type: string): string {
    return type === 'HOME' ? 'Nhà riêng' : type === 'WORK' ? 'Công ty' : type;
  }

  getFullAddress(addr: any): string {
    const parts = [];
    if (addr.apartmentNumber) parts.push(addr.apartmentNumber);
    if (addr.streetNumber) parts.push(addr.streetNumber);
    if (addr.ward) parts.push(addr.ward);
    if (addr.city) parts.push(addr.city);
    return parts.join(', ');
  }

  save() {
    this.loading = true;
    if (this.isEditing) {
      const payload = { id: this.form.id, firstName: this.form.firstName, lastName: this.form.lastName, gender: this.form.gender, dateOfBirth: this.form.dateOfBirth || null, phoneNumber: this.form.phoneNumber, email: this.form.email, addresses: [] };
      this.userService.updateUser(payload).subscribe({ next: () => { this.loading = false; this.showModal = false; this.load(); }, error: (e) => { this.loading = false; this.error = e?.error?.message || 'Cập nhật thất bại'; } });
    } else {
      const payload = { username: this.form.username, password: this.form.password, firstName: this.form.firstName, lastName: this.form.lastName, gender: this.form.gender, dateOfBirth: this.form.dateOfBirth || null, phoneNumber: this.form.phoneNumber, email: this.form.email, userType: this.form.userType, addresses: [] };
      this.userService.createUser(payload).subscribe({
        next: (res) => {
          this.loading = false; this.showModal = false; this.load();
          // nếu tạo Staff/Admin -> chuyển đến trang tạo nhân viên và gán userId
          const t = (this.form.userType || '').toUpperCase();
          if (t === 'STAFF' || t === 'ADMIN') {
            const newId = this.extractUserId(res);
            const qp: any = { open: 'create' };
            if (newId) qp.userId = newId;
            this.router.navigate(['/admin/employees'], { queryParams: qp });
          }
        }, error: (e) => { this.loading = false; this.error = e?.error?.message || 'Tạo người dùng thất bại'; }
      });
    }
  }

  private extractUserId(res: any): string {
    // Hỗ trợ nhiều định dạng ResponseData
    // 1) { code, message, data: '<uuid>' }
    // 2) { code, message, data: { id|userId: '<uuid>' } }
    // 3) '<uuid>'
    const uuidRe = /[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}/;
    const tryPick = (v: any): string => {
      if (!v) return '';
      if (typeof v === 'string' && uuidRe.test(v)) return v.match(uuidRe)?.[0] || '';
      if (typeof v === 'object') {
        const cand = v['userId'] || v['id'] || v['data'] || v['result'] || '';
        return tryPick(cand);
      }
      return '';
    };
    return tryPick(res);
  }
}
