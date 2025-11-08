import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmployeeService, EmployeeResponse, EmployeeRequest } from '../../../services/employee.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-admin-employees',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-employees.component.html',
  styleUrls: ['./admin-employees.component.scss']
})
export class AdminEmployeesComponent implements OnInit {
  // list state
  loading = false;
  error: string | null = null;
  employees: EmployeeResponse[] = [];

  keyword = '';
  pageNo = 1; // backend 1-based
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  sortBy = 'createdAt:desc';

  // modal state
  showModal = false;
  isEditing = false;
  form: EmployeeRequest & { id?: string } = {
    userId: '',
    position: '',
    salary: 0,
    hireDate: new Date().toISOString().slice(0, 10),
    note: ''
  };

  constructor(private employeeService: EmployeeService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.loadEmployees();
    // auto open create modal when navigated with ?open=create
    this.route.queryParams.subscribe(params => {
      const shouldOpen = (params['open'] || '').toString().toLowerCase() === 'create';
      const preUserId = params['userId'];
      if (shouldOpen) {
        this.openCreate();
        if (preUserId) {
          this.form.userId = preUserId;
        }
      }
    });
  }

  loadEmployees(): void {
    this.loading = true;
    this.error = null;
    this.employeeService
      .listEmployees({ keyword: this.keyword || undefined, pageNo: this.pageNo, pageSize: this.pageSize, sortBy: this.sortBy })
      .subscribe({
        next: (res) => {
          const data: any = res || {};
          this.employees = Array.isArray(data.items) ? data.items : [];
          this.totalPages = data.totalPages ?? 0;
          this.totalElements = data.totalElements ?? this.employees.length;
          this.loading = false;
        },
        error: (err) => {
          this.loading = false;
          this.error = err?.error?.message || 'Không tải được danh sách nhân viên';
        }
      });
  }

  search(): void { this.pageNo = 1; this.loadEmployees(); }
  changePage(delta: number): void {
    const next = this.pageNo + delta;
    if (next < 1 || (this.totalPages && next > this.totalPages)) return;
    this.pageNo = next; this.loadEmployees();
  }
  changePageSize(): void { this.pageNo = 1; this.loadEmployees(); }

  openCreate(): void {
    this.isEditing = false;
    this.showModal = true;
    this.form = { userId: '', position: '', salary: 0, hireDate: new Date().toISOString().slice(0,10), note: '' } as any;
  }
  openEdit(e: EmployeeResponse): void {
    this.isEditing = true; this.showModal = true;
    this.form = {
      id: e.id,
      userId: e.userId,
      position: e.position,
      salary: Number(e.salary),
      hireDate: (e.hireDate || '').toString(),
      note: e.note || '',
      status: e.status
    } as any;
  }

  save(): void {
    const payload: EmployeeRequest = {
      userId: this.form.userId,
      position: this.form.position,
      salary: Number(this.form.salary),
      hireDate: this.form.hireDate,
      note: this.form.note || undefined,
      status: this.form.status
    };
    const obs = this.isEditing && this.form.id
      ? this.employeeService.updateEmployee(this.form.id, payload)
      : this.employeeService.createEmployee(payload);
    this.loading = true;
    obs.subscribe({
      next: () => { this.loading = false; this.showModal = false; this.loadEmployees(); },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Lưu nhân viên thất bại'; }
    });
  }

  updateStatus(e: EmployeeResponse, status: string): void {
    this.employeeService.updateStatus(e.id, status).subscribe({
      next: () => this.loadEmployees(),
      error: () => alert('Cập nhật trạng thái thất bại')
    });
  }
}
