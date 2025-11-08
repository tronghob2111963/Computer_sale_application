import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ImportReceiptService, ImportReceiptRequest, ResponseEnvelope } from '../../../services/import-receipt.service';
import { EmployeeService, EmployeeResponse } from '../../../services/employee.service';
import { AuthService } from '../../../services/auth.service';
import { ProductService, ResponseEnvelope as ProductEnvelope } from '../../../services/product.service';

interface ItemRow {
  productId: string;
  productName?: string;
  quantity: number | null;
  importPrice: number | null;
}

@Component({
  standalone: true,
  selector: 'app-admin-import-receipt-create',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-import-receipt-create.component.html',
  styleUrls: ['./admin-import-receipt-create.component.scss']
})
export class AdminImportReceiptCreateComponent {
  loading = false;
  error: string | null = null;
  success: string | null = null;

  employees: EmployeeResponse[] = [];
  productOptions: any[] = [];

  model: { employeeId: string; note: string } = { employeeId: '', note: '' };
  items: ItemRow[] = [];

  keyword = '';

  constructor(
    private svc: ImportReceiptService,
    private empSvc: EmployeeService,
    private productSvc: ProductService,
    private router: Router,
    private auth: AuthService
  ) {
    this.loadEmployees();
    this.loadProductOptions();
    this.addRow();
    this.prefillEmployeeFromUser();
  }

  prefillEmployeeFromUser(): void {
    const userId = this.auth.getUserIdSafe();
    if (!userId) return;
    this.empSvc.getEmployeeIdByUserId(userId).subscribe({
      next: (empId) => {
        // ResponseType 'text' returns raw string; trim quotes if any
        const id = String(empId).replace(/^"|"$/g, '');
        this.model.employeeId = id;
      },
      error: () => { /* ignore; admin can still pick employee manually */ }
    });
  }

  loadEmployees(): void {
    // Simple fetch first page for selection
    this.empSvc.listEmployees({ pageNo: 1, pageSize: 50 }).subscribe({
      next: (res: any) => {
        const items = Array.isArray(res?.items) ? res.items : res?.data?.items;
        this.employees = Array.isArray(items) ? items : [];
      },
      error: () => {}
    });
  }

  loadProductOptions(): void {
    this.productSvc.listProducts<any>({ keyword: this.keyword || undefined, page: 0, size: 50 }).subscribe({
      next: (res: ProductEnvelope<any>) => {
        const data: any = (res as any)?.data ?? {};
        this.productOptions = Array.isArray(data?.items) ? data.items : [];
      },
      error: () => {}
    });
  }

  onSearchProducts(): void {
    this.loadProductOptions();
  }

  addRow(): void {
    this.items.push({ productId: '', quantity: null, importPrice: null });
  }

  removeRow(idx: number): void {
    this.items.splice(idx, 1);
  }

  productNameById(id: string): string {
    return this.productOptions.find((p) => p.id === id)?.name || '';
  }

  get subtotal(): number {
    return this.items.reduce((sum, it) => {
      const qty = it.quantity ?? 0;
      const price = it.importPrice ?? 0;
      return sum + qty * price;
    }, 0);
  }

  submit(): void {
    this.error = null;
    this.success = null;

    if (!this.model.employeeId) {
      this.error = 'Vui lòng chọn nhân viên';
      return;
    }

    const validItems = this.items
      .filter((i) => i.productId && (i.quantity ?? 0) > 0 && (i.importPrice ?? 0) > 0)
      .map((i) => ({ productId: i.productId, quantity: Number(i.quantity), importPrice: Number(i.importPrice) }));

    if (!validItems.length) {
      this.error = 'Thêm ít nhất 1 sản phẩm hợp lệ';
      return;
    }

    const payload: ImportReceiptRequest = {
      employeeId: this.model.employeeId,
      note: this.model.note,
      items: validItems
    };

    this.loading = true;
    this.svc.create(payload).subscribe({
      next: (res: ResponseEnvelope<any>) => {
        this.loading = false;
        this.success = res?.message || 'Tạo phiếu nhập thành công';
        const id = res?.data?.id || res?.data?.receiptId || res?.data?.uuid;
        if (id) {
          this.router.navigate(['/admin/import-receipts', id]);
        } else {
          this.router.navigate(['/admin/import-receipts']);
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Không thể tạo phiếu nhập';
      }
    });
  }
}
