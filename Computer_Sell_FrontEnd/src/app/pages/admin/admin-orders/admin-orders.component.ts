import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminOrderService } from '../../../services/admin-order.service';

@Component({
  standalone: true,
  selector: 'app-admin-orders',
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
  <div class="flex items-center justify-between mb-4">
    <h1 class="text-2xl font-semibold">Quản lý đơn hàng</h1>
    <div class="text-sm text-gray-600">Tổng: <b>{{ orders.length }}</b> đơn</div>
  </div>

  <div class="bg-white border rounded-xl p-4 mb-4 shadow-sm">
    <form class="grid md:grid-cols-5 gap-3 items-end" (ngSubmit)="load()">
      <div class="md:col-span-2">
        <label class="block text-sm text-gray-600 mb-1">Trạng thái</label>
        <div class="flex flex-wrap gap-2">
          <button type="button" (click)="status = null; load()" [class]="chipClass(!status)">Tất cả</button>
          <button *ngFor="let s of statuses" type="button" (click)="status = s; load()" [class]="chipClass(status===s)">{{ s }}</button>
        </div>
      </div>
      <div>
        <label class="block text-sm text-gray-600 mb-1">Từ ngày</label>
        <input type="datetime-local" [(ngModel)]="start" name="start" class="w-full border rounded px-2 py-1" />
      </div>
      <div>
        <label class="block text-sm text-gray-600 mb-1">Đến ngày</label>
        <input type="datetime-local" [(ngModel)]="end" name="end" class="w-full border rounded px-2 py-1" />
      </div>
      <div>
        <button class="px-3 py-2 bg-blue-600 text-white rounded w-full">Lọc</button>
      </div>
    </form>
  </div>

  <div class="bg-white border rounded-xl overflow-hidden shadow-sm">
    <div class="overflow-x-auto">
      <table class="min-w-full text-sm">
        <thead class="bg-gray-50">
          <tr>
            <th class="text-left px-4 py-2">Mã</th>
            <th class="text-left px-4 py-2">Ngày</th>
            <th class="text-left px-4 py-2">Trạng thái</th>
            <th class="text-left px-4 py-2">Thanh toán</th>
            <th class="text-right px-4 py-2">Tổng</th>
            <th class="px-4 py-2">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <ng-container *ngFor="let o of orders">
            <tr class="border-t hover:bg-gray-50/50">
              <td class="px-4 py-2">
                <a [routerLink]="['/order', o.id]" class="text-blue-700 hover:underline">{{ o.id }}</a>
              </td>
              <td class="px-4 py-2">{{ (o.orderDate || o.createdAt) | date:'short' }}</td>
              <td class="px-4 py-2">
                <span [class]="statusClass(o.status)">{{ o.status }}</span>
              </td>
              <td class="px-4 py-2">
                <span [class]="paymentClass(o.paymentStatus)">{{ o.paymentStatus }}</span>
              </td>
              <td class="px-4 py-2 text-right font-medium">{{ (o.totalAmount || 0) | number }}</td>
              <td class="px-4 py-2">
                <div class="flex gap-2 justify-end">
                  <button class="px-2 py-1 border rounded hover:bg-gray-50" (click)="toggle(o.id)">{{ expanded[o.id] ? 'Ẩn' : 'Chi tiết' }}</button>
                  <ng-container [ngSwitch]="o.status">
                    <button *ngSwitchCase="'PENDING'" (click)="confirmSetStatus(o,'CONFIRMED')" class="px-2 py-1 border rounded hover:bg-gray-50">Xác nhận</button>
                    <button *ngSwitchCase="'PENDING'" (click)="confirmSetStatus(o,'CANCELED')" class="px-2 py-1 border rounded hover:bg-gray-50">Hủy</button>

                    <ng-container *ngSwitchCase="'CANCEL_REQUEST'">
                      <button (click)="confirmProcessCancel(o,true)" class="px-2 py-1 border rounded hover:bg-gray-50">Duyệt hủy</button>
                      <button (click)="confirmProcessCancel(o,false)" class="px-2 py-1 border rounded hover:bg-gray-50">Từ chối</button>
                    </ng-container>

                    <button *ngSwitchCase="'CONFIRMED'" (click)="confirmSetStatus(o,'SHIPPING')" class="px-2 py-1 border rounded hover:bg-gray-50">Giao hàng</button>
                    <button *ngSwitchCase="'CONFIRMED'" (click)="confirmSetStatus(o,'CANCELED')" class="px-2 py-1 border rounded hover:bg-gray-50">Hủy</button>
                    <button *ngSwitchCase="'SHIPPING'" (click)="confirmSetStatus(o,'COMPLETED')" class="px-2 py-1 border rounded hover:bg-gray-50">Hoàn thành</button>
                  </ng-container>
                </div>
              </td>
            </tr>
            <tr *ngIf="expanded[o.id]" class="border-t bg-gray-50/60">
              <td colspan="6" class="px-4 py-3">
                <div class="grid md:grid-cols-3 gap-4">
                  <div class="md:col-span-2">
                    <div class="font-semibold mb-2">Sản phẩm</div>
                    <div class="divide-y">
                      <div *ngFor="let d of (o.details || [])" class="flex items-center justify-between py-2">
                        <div class="text-gray-700">
                          <div class="font-medium">{{ d.productName || d.product?.name }}</div>
                          <div class="text-xs text-gray-500">x{{ d.quantity }} · {{ (d.unitPrice || d.price) | number }} ₫</div>
                        </div>
                        <div class="font-medium">{{ (d.subtotal || (d.quantity * (d.unitPrice || d.price))) | number }} ₫</div>
                      </div>
                    </div>
                  </div>
                  <div>
                    <div class="font-semibold mb-2">Khuyến mãi</div>
                    <div *ngIf="(o.promotions || o.orderPromotions || []).length === 0" class="text-sm text-gray-500">Không áp dụng</div>
                    <div *ngFor="let p of (o.promotions || o.orderPromotions || [])" class="text-sm bg-green-50 text-green-700 border border-green-200 rounded px-2 py-1 mb-1">
                      {{ p.promoCode || p.promotion?.promoCode }} · Giảm {{ p.discountPercent || p.discountAmount }}
                    </div>
                    <div *ngIf="o.status==='SHIPPING'" class="mt-3 text-sm text-blue-700">Phiếu vận chuyển đã/đang được tạo tự động.</div>
                  </div>
                </div>
              </td>
            </tr>
          </ng-container>
        </tbody>
      </table>
    </div>
    <div *ngIf="orders.length === 0" class="text-gray-600 p-6">Không có đơn hàng.</div>
  </div>

  <!-- Confirm modal -->
  <div *ngIf="modal.show" class="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50">
    <div class="bg-white rounded-lg shadow-xl w-full max-w-md p-5">
      <div class="text-lg font-semibold mb-2">Xác nhận</div>
      <p class="text-sm text-gray-700 mb-4">{{ modal.message }}</p>
      <div class="flex justify-end gap-2">
        <button class="px-4 py-2 border rounded hover:bg-gray-50" (click)="closeModal()">Đóng</button>
        <button class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded" (click)="confirmModal()">Đồng ý</button>
      </div>
    </div>
  </div>

  <!-- Toast -->
  <div *ngIf="toast.show" class="fixed right-4 bottom-4 z-50">
    <div class="px-4 py-3 rounded shadow text-white" [ngClass]="{ 'bg-green-600': toast.type==='success', 'bg-red-600': toast.type==='error' }">
      {{ toast.message }}
    </div>
  </div>
  `
})
export class AdminOrdersComponent implements OnInit {
  orders: any[] = [];
  loading = false;
  statuses = ['PENDING','CANCEL_REQUEST','CONFIRMED','SHIPPING','COMPLETED','CANCELED'];
  status: string | null = null;
  start: string | null = null;
  end: string | null = null;
  expanded: Record<string, boolean> = {};
  toast = { show: false, type: '' as 'success'|'error'|'', message: '' };
  modal: { show: boolean; message: string; action: () => void } = { show: false, message: '', action: () => {} };

  constructor(private adminOrders: AdminOrderService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    const filter: any = {};
    if (this.status) filter.status = this.status;
    if (this.start) filter.start = new Date(this.start).toISOString();
    if (this.end) filter.end = new Date(this.end).toISOString();
    this.adminOrders.getOrders(filter).subscribe({
      next: (res: any) => { this.orders = res?.data || res || []; this.loading = false; },
      error: () => { this.orders = []; this.loading = false; }
    });
  }

  chipClass(active: boolean): string {
    return `px-3 py-1 rounded-full border ${active ? 'bg-blue-600 text-white border-blue-600' : 'bg-white hover:bg-gray-50'}`;
  }

  statusClass(s: string): string {
    const map: any = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      CONFIRMED: 'bg-blue-100 text-blue-800',
      SHIPPING: 'bg-indigo-100 text-indigo-800',
      COMPLETED: 'bg-green-100 text-green-800',
      CANCELED: 'bg-red-100 text-red-800',
      CANCEL_REQUEST: 'bg-orange-100 text-orange-800'
    };
    return `px-2 py-1 rounded ${map[s] || 'bg-gray-100 text-gray-800'}`;
  }

  paymentClass(s: string): string {
    const map: any = { PAID: 'bg-green-100 text-green-800', UNPAID: 'bg-gray-100 text-gray-700', REFUNDED: 'bg-purple-100 text-purple-800' };
    return `px-2 py-1 rounded ${map[s] || 'bg-gray-100 text-gray-800'}`;
  }

  toggle(id: string): void { this.expanded[id] = !this.expanded[id]; }

  showToast(message: string, type: 'success'|'error'='success'): void {
    this.toast = { show: true, type, message };
    setTimeout(() => this.toast.show = false, 1600);
  }

  confirmSetStatus(order: any, status: string): void {
    const msg = status === 'SHIPPING' ? 'Chuyển sang SHIPPING? Phiếu vận chuyển sẽ được tạo tự động.' : `Cập nhật trạng thái: ${status}?`;
    this.modal = { show: true, message: msg, action: () => this.setStatus(order.id, status) };
  }

  confirmProcessCancel(order: any, approve: boolean): void {
    const msg = approve ? 'Duyệt yêu cầu hủy đơn này?' : 'Từ chối yêu cầu hủy?';
    this.modal = { show: true, message: msg, action: () => this.processCancel(order.id, approve) };
  }

  closeModal(): void { this.modal.show = false; }
  confirmModal(): void { const fn = this.modal.action; this.modal.show = false; fn && fn(); }

  setStatus(id: string, status: string): void {
    this.adminOrders.updateStatus(id, status).subscribe({
      next: () => {
        this.load();
        if (status === 'SHIPPING') this.showToast('Đã chuyển SHIPPING. Phiếu VC sẽ tạo tự động.');
        else this.showToast('Cập nhật trạng thái thành công.');
      },
      error: (e) => this.showToast(e?.error?.message || 'Cập nhật thất bại', 'error')
    });
  }

  processCancel(id: string, approve: boolean): void {
    this.adminOrders.processCancel(id, approve).subscribe({
      next: () => { this.load(); this.showToast(approve ? 'Đã duyệt hủy đơn.' : 'Đã từ chối hủy.'); },
      error: (e) => this.showToast(e?.error?.message || 'Xử lý thất bại', 'error')
    });
  }
}
