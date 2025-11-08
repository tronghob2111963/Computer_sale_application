import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { ShippingOrderService } from '../../../services/shipping-order.service';

@Component({
  standalone: true,
  selector: 'app-admin-shipping-order-detail',
  imports: [CommonModule],
  template: `
  <div class="flex items-center justify-between mb-4">
    <h1 class="text-2xl font-semibold">Chi tiết phiếu vận chuyển</h1>
    <button *ngIf="data" class="px-3 py-2 bg-blue-600 text-white rounded" (click)="exportPdf()">Xuất PDF</button>
  </div>

  <div *ngIf="data" class="grid md:grid-cols-3 gap-4">
    <div class="md:col-span-2 bg-white border rounded-xl p-4 shadow-sm">
      <div class="grid md:grid-cols-2 gap-4">
        <div>
          <div class="text-sm text-gray-500">Mã phiếu</div>
          <div class="font-semibold">{{ data.id }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">Mã đơn hàng</div>
          <div class="font-semibold">{{ data.orderId }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">Người nhận</div>
          <div class="font-semibold">{{ data.recipientName }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">SĐT người nhận</div>
          <div class="font-semibold">{{ data.recipientPhone }}</div>
        </div>
        <div class="md:col-span-2">
          <div class="text-sm text-gray-500">Địa chỉ giao hàng</div>
          <div class="font-semibold">{{ data.shippingAddress }}</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">Thanh toán</div>
          <div>
            <span class="px-2 py-1 rounded" [ngClass]="data.paymentCompleted ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-700'">
              {{ data.paymentCompleted ? 'Đã thanh toán' : 'Chưa thanh toán' }}
            </span>
          </div>
        </div>
        <div>
          <div class="text-sm text-gray-500">Tổng tiền</div>
          <div class="font-semibold">{{ (data.totalAmount || 0) | number }} ₫</div>
        </div>
        <div>
          <div class="text-sm text-gray-500">Ngày tạo</div>
          <div class="font-semibold">{{ data.createdAt | date:'short' }}</div>
        </div>
      </div>
    </div>

    <div class="bg-white border rounded-xl p-4 shadow-sm">
      <div class="font-semibold mb-2">Ghi chú</div>
      <div class="text-sm text-gray-600">Phiếu được tạo khi đơn chuyển trạng thái CONFIRMED → SHIPPING.</div>
    </div>

    <div class="md:col-span-3 bg-white border rounded-xl p-4 shadow-sm" *ngIf="(data.items || data.details || []).length">
      <div class="font-semibold mb-3">Sản phẩm</div>
      <div class="overflow-x-auto">
        <table class="min-w-full text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="text-left px-4 py-2">Sản phẩm</th>
              <th class="text-right px-4 py-2">Số lượng</th>
              <th class="text-right px-4 py-2">Đơn giá</th>
              <th class="text-right px-4 py-2">Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let it of (data.items || data.details)" class="border-t">
              <td class="px-4 py-2">{{ it.productName || it.product?.name }}</td>
              <td class="px-4 py-2 text-right">{{ it.quantity }}</td>
              <td class="px-4 py-2 text-right">{{ (it.unitPrice || it.price || 0) | number }}</td>
              <td class="px-4 py-2 text-right">{{ (it.subtotal || (it.quantity * (it.unitPrice || it.price || 0))) | number }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <div *ngIf="!data && !loading" class="text-gray-600">Không tìm thấy phiếu vận chuyển.</div>

  <!-- Toast -->
  <div *ngIf="toast.show" class="fixed right-4 bottom-4 z-50">
    <div class="px-4 py-3 rounded shadow text-white" [ngClass]="{ 'bg-green-600': toast.type==='success', 'bg-red-600': toast.type==='error' }">
      {{ toast.message }}
    </div>
  </div>
  `
})
export class AdminShippingOrderDetailComponent implements OnInit {
  data: any;
  loading = false;
  toast = { show: false, type: '' as 'success'|'error'|'', message: '' };

  constructor(private route: ActivatedRoute, private shippingService: ShippingOrderService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.fetch(id);
  }

  fetch(id: string): void {
    this.loading = true;
    this.shippingService.getShippingOrderById(id).subscribe({
      next: (res) => { this.data = (res as any)?.data || (res as any) || null; this.loading = false; },
      error: () => { this.data = null; this.loading = false; }
    });
  }

  exportPdf(): void {
    if (!this.data?.id) return;
    this.shippingService.exportShippingOrderToPdf(this.data.id).subscribe({
      next: (blob) => {
        const fileURL = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = fileURL;
        a.download = `shipping-order-${this.data.id}.pdf`;
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

