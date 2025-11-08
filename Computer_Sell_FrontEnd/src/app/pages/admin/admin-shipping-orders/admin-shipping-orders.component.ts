import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ShippingOrderService } from '../../../services/shipping-order.service';

@Component({
  standalone: true,
  selector: 'app-admin-shipping-orders',
  imports: [CommonModule, RouterModule],
  template: `
  <div class="flex items-center justify-between mb-4">
    <h1 class="text-2xl font-semibold">Phiếu vận chuyển</h1>
    <div class="text-sm text-gray-600">Tổng: <b>{{ list.length }}</b> phiếu</div>
  </div>

  <div class="bg-white border rounded-xl overflow-hidden shadow-sm">
    <div class="overflow-x-auto">
      <table class="min-w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-left px-4 py-2">Mã phiếu</th>
            <th class="text-left px-4 py-2">Mã đơn</th>
            <th class="text-left px-4 py-2">Người nhận</th>
            <th class="text-left px-4 py-2">SĐT</th>
            <th class="text-left px-4 py-2">Địa chỉ</th>
            <th class="text-left px-4 py-2">Thanh toán</th>
            <th class="text-right px-4 py-2">Tổng</th>
            <th class="text-left px-4 py-2">Ngày tạo</th>
            <th class="px-4 py-2 text-right">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let s of list" class="border-t hover:bg-gray-50/50">
            <td class="px-4 py-2">{{ s.id }}</td>
            <td class="px-4 py-2">{{ s.orderId }}</td>
            <td class="px-4 py-2">{{ s.recipientName }}</td>
            <td class="px-4 py-2">{{ s.recipientPhone }}</td>
            <td class="px-4 py-2 max-w-[300px] truncate" [title]="s.shippingAddress">{{ s.shippingAddress }}</td>
            <td class="px-4 py-2">
              <span class="px-2 py-1 rounded" [ngClass]="s.paymentCompleted ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-700'">
                {{ s.paymentCompleted ? 'Đã thanh toán' : 'Chưa thanh toán' }}
              </span>
            </td>
            <td class="px-4 py-2 text-right font-medium">{{ (s.totalAmount || 0) | number }}</td>
            <td class="px-4 py-2">{{ s.createdAt | date:'short' }}</td>
            <td class="px-4 py-2">
              <div class="flex gap-2 justify-end">
                <a [routerLink]="['/admin/shipping-orders', s.id]" class="px-2 py-1 border rounded hover:bg-gray-50">Chi tiết</a>
                <button class="px-2 py-1 border rounded hover:bg-gray-50" (click)="exportPdf(s)">Xuất PDF</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div *ngIf="list.length === 0 && !loading" class="text-gray-600 p-6">Không có phiếu vận chuyển.</div>
  </div>

  <!-- Toast -->
  <div *ngIf="toast.show" class="fixed right-4 bottom-4 z-50">
    <div class="px-4 py-3 rounded shadow text-white" [ngClass]="{ 'bg-green-600': toast.type==='success', 'bg-red-600': toast.type==='error' }">
      {{ toast.message }}
    </div>
  </div>
  `
})
export class AdminShippingOrdersComponent implements OnInit {
  list: any[] = [];
  loading = false;
  toast = { show: false, type: '' as 'success'|'error'|'', message: '' };

  constructor(private shippingService: ShippingOrderService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.shippingService.getAllShippingOrders().subscribe({
      next: (res) => { this.list = (res as any)?.data || (res as any) || []; this.loading = false; },
      error: () => { this.list = []; this.loading = false; }
    });
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
        this.showToast('Đã xuất PDF.');
      },
      error: (e) => this.showToast(e?.error?.message || 'Xuất PDF thất bại', 'error')
    });
  }

  private showToast(message: string, type: 'success'|'error'='success'): void {
    this.toast = { show: true, type, message };
    setTimeout(() => this.toast.show = false, 1600);
  }
}

