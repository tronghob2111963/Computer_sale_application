import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ShippingOrderService } from '../../../services/shipping-order.service';

@Component({
  standalone: true,
  selector: 'app-admin-shipping-orders',
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
  <!-- Stats Cards -->
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
    <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-gray-500 mb-1">Tổng phiếu</p>
          <p class="text-2xl font-bold text-gray-800">{{ list.length }}</p>
        </div>
        <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
          <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
        </div>
      </div>
    </div>
    
    <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-gray-500 mb-1">Đã thanh toán</p>
          <p class="text-2xl font-bold text-green-600">{{ paidCount }}</p>
        </div>
        <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center">
          <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
      </div>
    </div>
    
    <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-gray-500 mb-1">Chưa thanh toán</p>
          <p class="text-2xl font-bold text-orange-600">{{ unpaidCount }}</p>
        </div>
        <div class="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center">
          <svg class="w-6 h-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
      </div>
    </div>
    
    <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-gray-500 mb-1">Tổng doanh thu</p>
          <p class="text-2xl font-bold text-indigo-600">{{ totalRevenue | number }}đ</p>
        </div>
        <div class="w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center">
          <svg class="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
      </div>
    </div>
  </div>

  <!-- Header & Filters -->
  <div class="bg-white rounded-xl shadow-sm border border-gray-100 mb-6">
    <div class="p-5 border-b border-gray-100">
      <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
        <div>
          <h1 class="text-xl font-bold text-gray-800">Phiếu vận chuyển</h1>
          <p class="text-sm text-gray-500 mt-1">Quản lý tất cả phiếu vận chuyển</p>
        </div>
        
        <!-- Search & Filters -->
        <div class="flex flex-col sm:flex-row gap-3">
          <!-- Search -->
          <div class="relative">
            <svg class="w-5 h-5 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
            </svg>
            <input type="text" [(ngModel)]="searchTerm" (ngModelChange)="applyFilters()" 
              placeholder="Tìm mã phiếu, người nhận..." 
              class="pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg w-full sm:w-64 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all">
          </div>
          
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="overflow-x-auto">
      <table class="w-full">
        <thead>
          <tr class="bg-gray-50">
            <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Mã phiếu</th>
            <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Mã đơn</th>
            <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Người nhận</th>
            <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider hidden lg:table-cell">Địa chỉ</th>
            <th class="text-right px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Tổng tiền</th>
            <th class="text-left px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider hidden md:table-cell">Ngày tạo</th>
            <th class="text-right px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Thao tác</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr *ngFor="let s of paginatedList; let i = index" 
            class="hover:bg-blue-50/50 transition-colors"
            [class.bg-gray-50]="i % 2 === 1">
            <td class="px-5 py-4">
              <span class="font-mono text-sm text-blue-600 font-medium">#{{ s.id?.toString().slice(0, 8) }}</span>
            </td>
            <td class="px-5 py-4">
              <span class="font-mono text-sm text-gray-600">#{{ s.orderId?.toString().slice(0, 8) }}</span>
            </td>
            <td class="px-5 py-4">
              <div class="flex items-center gap-3">
                <div class="w-9 h-9 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-semibold text-sm">
                  {{ getInitials(s.recipientName) }}
                </div>
                <div>
                  <p class="font-medium text-gray-800">{{ s.recipientName }}</p>
                  <p class="text-sm text-gray-500">{{ s.recipientPhone }}</p>
                </div>
              </div>
            </td>
            <td class="px-5 py-4 hidden lg:table-cell">
              <p class="text-sm text-gray-600 max-w-[250px] truncate" [title]="s.shippingAddress">
                {{ s.shippingAddress }}
              </p>
            </td>
            <td class="px-5 py-4 text-right">
              <span class="font-semibold text-gray-800">{{ (s.totalAmount || 0) | number }}đ</span>
            </td>
            <td class="px-5 py-4 hidden md:table-cell">
              <div class="text-sm text-gray-600">{{ s.createdAt | date:'dd/MM/yyyy' }}</div>
              <div class="text-xs text-gray-400">{{ s.createdAt | date:'HH:mm' }}</div>
            </td>
            <td class="px-5 py-4">
              <div class="flex items-center justify-end gap-2">
                <a [routerLink]="['/admin/shipping-orders', s.id]" 
                  class="p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors" title="Chi tiết">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                  </svg>
                </a>
                <button (click)="exportPdf(s)" 
                  class="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Xuất PDF">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                  </svg>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Empty State -->
    <div *ngIf="filteredList.length === 0 && !loading" class="py-16 text-center">
      <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
        </svg>
      </div>
      <h3 class="text-lg font-medium text-gray-800 mb-1">Không tìm thấy phiếu vận chuyển</h3>
      <p class="text-gray-500">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
    </div>

    <!-- Loading State -->
    <div *ngIf="loading" class="py-16 text-center">
      <div class="w-12 h-12 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-500">Đang tải dữ liệu...</p>
    </div>

    <!-- Pagination -->
    <div *ngIf="filteredList.length > 0" class="px-5 py-4 border-t border-gray-100 flex flex-col sm:flex-row items-center justify-between gap-4">
      <div class="text-sm text-gray-600">
        Hiển thị <span class="font-medium">{{ startIndex + 1 }}</span> - <span class="font-medium">{{ endIndex }}</span> 
        trong <span class="font-medium">{{ filteredList.length }}</span> phiếu
      </div>
      
      <div class="flex items-center gap-2">
        <button (click)="goToPage(currentPage - 1)" [disabled]="currentPage === 1"
          class="px-3 py-2 border border-gray-200 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>
        
        <ng-container *ngFor="let page of visiblePages">
          <button *ngIf="page !== '...'" (click)="goToPage(+page)"
            class="w-10 h-10 rounded-lg text-sm font-medium transition-colors"
            [ngClass]="currentPage === page ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-gray-100'">
            {{ page }}
          </button>
          <span *ngIf="page === '...'" class="px-2 text-gray-400">...</span>
        </ng-container>
        
        <button (click)="goToPage(currentPage + 1)" [disabled]="currentPage === totalPages"
          class="px-3 py-2 border border-gray-200 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>
      </div>
      
      <select [(ngModel)]="pageSize" (ngModelChange)="onPageSizeChange()" 
        class="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none bg-white">
        <option [value]="5">5 / trang</option>
        <option [value]="10">10 / trang</option>
        <option [value]="20">20 / trang</option>
        <option [value]="50">50 / trang</option>
      </select>
    </div>
  </div>

  <!-- Toast -->
  <div *ngIf="toast.show" class="fixed right-4 bottom-4 z-50 animate-slide-up">
    <div class="flex items-center gap-3 px-4 py-3 rounded-xl shadow-lg text-white"
      [ngClass]="{ 'bg-green-600': toast.type==='success', 'bg-red-600': toast.type==='error' }">
      <svg *ngIf="toast.type==='success'" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
      </svg>
      <svg *ngIf="toast.type==='error'" class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
      </svg>
      {{ toast.message }}
    </div>
  </div>
  `,
  styles: [`
    @keyframes slide-up {
      from { transform: translateY(100%); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
    .animate-slide-up { animation: slide-up 0.3s ease-out; }
  `]
})

export class AdminShippingOrdersComponent implements OnInit {
  list: any[] = [];
  filteredList: any[] = [];
  loading = false;
  toast = { show: false, type: '' as 'success' | 'error' | '', message: '' };

  // Filters
  searchTerm = '';

  // Pagination
  currentPage = 1;
  pageSize = 10;

  constructor(private shippingService: ShippingOrderService) { }

  ngOnInit(): void { this.load(); }

  // Stats
  get paidCount(): number {
    return this.list.filter(s => s.paymentCompleted).length;
  }

  get unpaidCount(): number {
    return this.list.filter(s => !s.paymentCompleted).length;
  }

  get totalRevenue(): number {
    return this.list.reduce((sum, s) => sum + (s.totalAmount || 0), 0);
  }

  // Pagination getters
  get totalPages(): number {
    return Math.ceil(this.filteredList.length / this.pageSize);
  }

  get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  get endIndex(): number {
    return Math.min(this.startIndex + this.pageSize, this.filteredList.length);
  }

  get paginatedList(): any[] {
    return this.filteredList.slice(this.startIndex, this.endIndex);
  }

  get visiblePages(): (number | string)[] {
    const pages: (number | string)[] = [];
    const total = this.totalPages;
    const current = this.currentPage;

    if (total <= 7) {
      for (let i = 1; i <= total; i++) pages.push(i);
    } else {
      if (current <= 3) {
        pages.push(1, 2, 3, 4, '...', total);
      } else if (current >= total - 2) {
        pages.push(1, '...', total - 3, total - 2, total - 1, total);
      } else {
        pages.push(1, '...', current - 1, current, current + 1, '...', total);
      }
    }
    return pages;
  }

  load(): void {
    this.loading = true;
    this.shippingService.getAllShippingOrders().subscribe({
      next: (res) => {
        this.list = (res as any)?.data || (res as any) || [];
        this.applyFilters();
        this.loading = false;
      },
      error: () => { this.list = []; this.filteredList = []; this.loading = false; }
    });
  }

  applyFilters(): void {
    let result = [...this.list];

    // Search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(s =>
        s.id?.toString().toLowerCase().includes(term) ||
        s.orderId?.toString().toLowerCase().includes(term) ||
        s.recipientName?.toLowerCase().includes(term) ||
        s.recipientPhone?.includes(term)
      );
    }

    this.filteredList = result;
    this.currentPage = 1;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  onPageSizeChange(): void {
    this.currentPage = 1;
  }

  getInitials(name: string): string {
    if (!name) return '?';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  exportPdf(s: any): void {
    this.shippingService.exportShippingOrderToPdf(s.id).subscribe({
      next: (blob) => {
        const fileURL = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = fileURL;
        a.download = `shipping-order-${s.id}.pdf`;
        a.click();
        URL.revokeObjectURL(fileURL);
        this.showToast('Đã xuất PDF thành công!');
      },
      error: (e) => this.showToast(e?.error?.message || 'Xuất PDF thất bại', 'error')
    });
  }

  private showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toast = { show: true, type, message };
    setTimeout(() => this.toast.show = false, 2500);
  }
}
