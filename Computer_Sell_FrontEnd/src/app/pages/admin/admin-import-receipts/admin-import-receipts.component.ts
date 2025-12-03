import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  ImportReceiptService,
  ImportReceiptResponse,
  ImportReceiptStatus,
  ResponseEnvelope
} from '../../../services/import-receipt.service';

@Component({
  standalone: true,
  selector: 'app-admin-import-receipts',
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin-import-receipts.component.html',
  styleUrls: ['./admin-import-receipts.component.scss']
})
export class AdminImportReceiptsComponent implements OnInit {
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;
  receipts: ImportReceiptResponse[] = [];

  // Filter options
  filterStatus: ImportReceiptStatus | '' = '';
  filterStartDate: string = '';
  filterEndDate: string = '';

  constructor(private svc: ImportReceiptService, private router: Router) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.svc.list().subscribe({
      next: (res: ResponseEnvelope<ImportReceiptResponse[]>) => {
        this.receipts = Array.isArray(res?.data) ? res.data : [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Không tải được danh sách phiếu nhập';
        this.loading = false;
      }
    });
  }

  // Lọc theo trạng thái
  filterByStatus(): void {
    if (!this.filterStatus) {
      this.load();
      return;
    }
    this.loading = true;
    this.error = null;
    this.svc.filterByStatus(this.filterStatus).subscribe({
      next: (res) => {
        this.receipts = Array.isArray(res?.data) ? res.data : [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Lỗi khi lọc phiếu nhập';
        this.loading = false;
      }
    });
  }

  // Lọc theo khoảng thời gian
  filterByDateRange(): void {
    if (!this.filterStartDate || !this.filterEndDate) {
      this.error = 'Vui lòng chọn ngày bắt đầu và kết thúc';
      return;
    }
    this.loading = true;
    this.error = null;
    const start = new Date(this.filterStartDate).toISOString();
    const end = new Date(this.filterEndDate).toISOString();
    this.svc.filterByDateRange(start, end).subscribe({
      next: (res) => {
        this.receipts = Array.isArray(res?.data) ? res.data : [];
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Lỗi khi lọc phiếu nhập';
        this.loading = false;
      }
    });
  }

  // Xóa bộ lọc
  clearFilters(): void {
    this.filterStatus = '';
    this.filterStartDate = '';
    this.filterEndDate = '';
    this.load();
  }

  // Hủy phiếu nhập
  cancelReceipt(r: ImportReceiptResponse): void {
    if (r.status !== 'COMPLETED') {
      this.error = 'Chỉ có thể hủy phiếu nhập đã hoàn thành';
      return;
    }
    if (!confirm(`Bạn có chắc muốn hủy phiếu nhập ${r.receiptCode}? Số lượng sẽ được hoàn lại kho.`)) {
      return;
    }
    const cancelledBy = 'admin'; // Có thể lấy từ auth service
    this.svc.cancel(r.receiptId, cancelledBy).subscribe({
      next: () => {
        this.successMessage = `Đã hủy phiếu nhập ${r.receiptCode} và hoàn lại kho`;
        this.load();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Lỗi khi hủy phiếu nhập';
      }
    });
  }

  create(): void {
    this.router.navigate(['/admin/import-receipts/create']);
  }

  view(r: ImportReceiptResponse): void {
    const id = r?.receiptId;
    if (id) {
      this.router.navigate(['/admin/import-receipts', id]);
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'bg-green-100 text-green-800';
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'Hoàn thành';
      case 'PENDING': return 'Đang xử lý';
      case 'CANCELLED': return 'Đã hủy';
      default: return status;
    }
  }
}

