import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Meta, Title } from '@angular/platform-browser';
import {
  MonthlyRevenue,
  PaymentDetail,
  PaymentOverviewStats,
  PaymentRecord,
  PaymentService,
  PaymentSearchParams
} from '../../../services/payment.service';
import { environment } from '../../../enviroment';

interface PageMeta<T = any> {
  items: T[];
  pageNo: number;
  totalPages: number;
  totalElements: number;
}

@Component({
  standalone: true,
  selector: 'app-admin-payments',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-payments.component.html',
  styleUrls: ['./admin-payments.component.scss']
})
export class AdminPaymentsComponent implements OnInit {
  loading = { table: false, stats: false };
  confirming: Record<string, boolean> = {};
  payments: PaymentRecord[] = [];
  overview?: PaymentOverviewStats | null;
  monthly?: MonthlyRevenue | null;
  tableError: string | null = null;
  statsError: string | null = null;

  keyword = '';
  status: string | null = null;
  startDate = '';
  endDate = '';
  pageNo = 1;
  pageSize = 10;
  totalPages = 1;
  totalElements = 0;
  sortBy = 'paymentDate';

  readonly statusOptions = [
    { value: 'SUCCESS', label: 'Thành công' },
    { value: 'PAID', label: 'Đã thu tiền' },
    { value: 'UNPAID', label: 'Chưa thanh toán' },
    { value: 'FAILED', label: 'Thất bại' },
    { value: 'REFUNDED', label: 'Đã hoàn' }
  ];

  readonly monthLabels = ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'];
  readonly yearOptions = [0, 1, 2].map((offset) => new Date().getFullYear() - offset);
  selectedYear = this.yearOptions[0];

  detailPanel: { open: boolean; data: PaymentDetail | null; loading: boolean } = {
    open: false,
    data: null,
    loading: false
  };

  toast = { show: false, type: 'success' as 'success' | 'error', message: '' };

  // VietQR specific state
  vietqrProcessing: Record<string, boolean> = {};
  proofImageModal = { open: false, url: '' };
  rejectModal = { open: false, paymentId: '', reason: '' };

  // API base URL for images
  readonly apiUrl = environment.apiUrl;

  constructor(
    private paymentsApi: PaymentService,
    private title: Title,
    private meta: Meta
  ) { }

  ngOnInit(): void {
    this.title.setTitle('Quản lý thanh toán - Bảng điều khiển THComputer');
    this.meta.updateTag({
      name: 'description',
      content:
        'Giám sát giao dịch, doanh thu và trạng thái thanh toán theo thời gian thực trên trang quản trị THComputer.'
    });

    this.loadOverview();
    this.loadMonthly();
    this.loadPayments();
  }

  loadPayments(page = this.pageNo): void {
    this.loading.table = true;
    this.tableError = null;
    const params: PaymentSearchParams = {
      keyword: this.keyword || undefined,
      status: this.status || undefined,
      startDate: this.startDate ? new Date(this.startDate).toISOString() : undefined,
      endDate: this.endDate ? new Date(this.endDate).toISOString() : undefined,
      pageNo: page,
      pageSize: this.pageSize,
      sortBy: this.sortBy
    };

    this.paymentsApi.searchPayments(params).subscribe({
      next: (res: any) => {
        const pageData = this.extractPage<PaymentRecord>(res);
        this.payments = pageData.items;
        this.pageNo = pageData.pageNo;
        this.totalPages = pageData.totalPages;
        this.totalElements = pageData.totalElements;
        this.loading.table = false;
      },
      error: (err: any) => {
        this.tableError = err?.error?.message || 'Không thể tải danh sách thanh toán.';
        this.payments = [];
        this.loading.table = false;
      }
    });
  }

  loadOverview(): void {
    this.loading.stats = true;
    this.statsError = null;
    this.paymentsApi.getOverview().subscribe({
      next: (res: any) => {
        this.overview = res?.data ?? (res as any);
        this.loading.stats = false;
      },
      error: (err: any) => {
        this.statsError = err?.error?.message || 'Không thể tải thống kê tổng quan.';
        this.overview = null;
        this.loading.stats = false;
      }
    });
  }

  loadMonthly(year = this.selectedYear): void {
    this.selectedYear = year;
    this.paymentsApi.getMonthlyRevenue(year).subscribe({
      next: (res: any) => {
        this.monthly = res?.data ?? (res as any);
      },
      error: () => {
        this.monthly = { year, monthlyRevenue: Array(12).fill(0) };
      }
    });
  }

  applyFilters(): void {
    this.pageNo = 1;
    this.loadPayments(1);
  }

  resetFilters(): void {
    this.keyword = '';
    this.status = null;
    this.startDate = '';
    this.endDate = '';
    this.sortBy = 'paymentDate';
    this.pageSize = 10;
    this.applyFilters();
  }

  changePage(delta: number): void {
    const next = this.pageNo + delta;
    if (next < 1 || (this.totalPages && next > this.totalPages)) {
      return;
    }
    this.pageNo = next;
    this.loadPayments(next);
  }

  changePageSize(size: number): void {
    this.pageSize = size;
    this.pageNo = 1;
    this.loadPayments(1);
  }

  handlePageSizeChange(value: string | number): void {
    const size = Number(value) || 10;
    if (size === this.pageSize) {
      return;
    }
    this.changePageSize(size);
  }

  openDetail(payment: PaymentRecord): void {
    this.detailPanel.open = true;
    this.detailPanel.loading = true;
    this.detailPanel.data = null;
    this.paymentsApi.getPaymentDetail(payment.id).subscribe({
      next: (res: any) => {
        this.detailPanel.data = res?.data ?? (res as any);
        this.detailPanel.loading = false;
      },
      error: (err: any) => {
        this.detailPanel.loading = false;
        this.showToast(err?.error?.message || 'Không thể tải chi tiết giao dịch', 'error');
      }
    });
  }

  closeDetail(): void {
    this.detailPanel = { open: false, data: null, loading: false };
  }

  confirmPayment(payment: PaymentRecord): void {
    if (this.confirming[payment.id]) {
      return;
    }
    this.confirming[payment.id] = true;
    this.paymentsApi
      .confirmPayment(payment.id)
      .subscribe({
        next: () => {
          this.showToast('Đã xác nhận thanh toán thành công.');
          this.loadPayments(this.pageNo);
          this.loadOverview();
        },
        error: (err: any) => {
          this.showToast(err?.error?.message || 'Không thể xác nhận thanh toán', 'error');
        }
      })
      .add(() => (this.confirming[payment.id] = false));
  }

  canConfirm(payment: PaymentRecord): boolean {
    return !['SUCCESS', 'PAID', 'FAILED'].includes((payment?.paymentStatus || '').toUpperCase());
  }

  trackByPayment(_: number, item: PaymentRecord): string {
    return item.id;
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      SUCCESS: 'status success',
      PAID: 'status paid',
      UNPAID: 'status pending',
      FAILED: 'status danger',
      REFUNDED: 'status refunded'
    };
    return map[status?.toUpperCase()] || 'status neutral';
  }

  revenueEntries(): { method: string; amount: number }[] {
    const data = this.overview?.revenueByMethod || {};
    return Object.keys(data).map((method) => ({ method, amount: data[method] }));
  }

  monthlyBarHeight(amount: number): string {
    if (!this.monthly || !this.monthly.monthlyRevenue.length) {
      return '4%';
    }
    const max = Math.max(...this.monthly.monthlyRevenue, 1);
    const ratio = amount / max;
    return `${Math.max(ratio * 90, 6)}%`;
  }

  private extractPage<T>(res: any): PageMeta<T> {
    const layer1 = res?.data ?? res;
    const page = layer1?.data ?? layer1?.page ?? layer1;
    const items = Array.isArray(page?.items)
      ? page.items
      : Array.isArray(page?.content)
        ? page.content
        : Array.isArray(layer1)
          ? layer1
          : [];

    return {
      items: items as T[],
      pageNo: page?.pageNo ?? this.pageNo ?? 1,
      totalPages: page?.totalPages ?? 1,
      totalElements: page?.totalElements ?? (items?.length ?? 0)
    };
  }

  private showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toast = { show: true, type, message };
    setTimeout(() => (this.toast.show = false), 2000);
  }

  // VietQR Methods
  canConfirmVietQR(payment: PaymentDetail): boolean {
    return payment.paymentMethod === 'VIETQR' &&
      !['SUCCESS', 'PAID', 'FAILED'].includes((payment.paymentStatus || '').toUpperCase());
  }

  // Tạo full URL cho ảnh từ backend
  getFullImageUrl(url: string | undefined): string {
    if (!url) return '';
    // Nếu URL đã là absolute thì trả về luôn
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    // Nếu là relative URL thì thêm apiUrl
    return this.apiUrl + url;
  }

  openProofImageModal(url: string): void {
    this.proofImageModal = { open: true, url: this.getFullImageUrl(url) };
  }

  closeProofImageModal(): void {
    this.proofImageModal = { open: false, url: '' };
  }

  confirmVietQRPayment(payment: PaymentDetail): void {
    if (this.vietqrProcessing[payment.id]) return;

    this.vietqrProcessing[payment.id] = true;
    this.paymentsApi.confirmVietQRPayment(payment.id).subscribe({
      next: () => {
        this.showToast('Đã xác nhận thanh toán VietQR thành công');
        this.loadPayments(this.pageNo);
        this.loadOverview();
        if (this.detailPanel.data?.id === payment.id) {
          this.detailPanel.data.paymentStatus = 'SUCCESS';
        }
      },
      error: (err: any) => {
        this.showToast(err?.error?.message || 'Không thể xác nhận thanh toán', 'error');
      }
    }).add(() => this.vietqrProcessing[payment.id] = false);
  }

  openRejectModal(payment: PaymentDetail): void {
    this.rejectModal = { open: true, paymentId: payment.id, reason: '' };
  }

  closeRejectModal(): void {
    this.rejectModal = { open: false, paymentId: '', reason: '' };
  }

  submitRejectVietQR(): void {
    const { paymentId, reason } = this.rejectModal;
    if (!paymentId || this.vietqrProcessing[paymentId]) return;

    this.vietqrProcessing[paymentId] = true;
    this.paymentsApi.rejectVietQRPayment(paymentId, reason).subscribe({
      next: () => {
        this.showToast('Đã từ chối thanh toán VietQR');
        this.closeRejectModal();
        this.loadPayments(this.pageNo);
        if (this.detailPanel.data?.id === paymentId) {
          this.detailPanel.data.paymentStatus = 'FAILED';
        }
      },
      error: (err: any) => {
        this.showToast(err?.error?.message || 'Không thể từ chối thanh toán', 'error');
      }
    }).add(() => this.vietqrProcessing[paymentId] = false);
  }
}
