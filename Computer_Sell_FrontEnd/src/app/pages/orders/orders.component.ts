import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Title, Meta } from '@angular/platform-browser';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit {
  loading = false;
  error: string | null = null;
  orders: any[] = [];
  canceling: Record<string, boolean> = {};
  // simple modal state for cancel request
  cancelModal = { show: false, orderId: '' as string, reason: '' as string };

  constructor(
    private orderService: OrderService,
    private auth: AuthService,
    private router: Router,
    private title: Title,
    private meta: Meta,
  ) {}

  ngOnInit(): void {
    this.title.setTitle('Đơn hàng của tôi - THComputer');
    this.meta.updateTag({ name: 'description', content: 'Theo dõi lịch sử đặt hàng, trạng thái đơn và chi tiết từng đơn tại THComputer.' });

    const uid = this.auth.getUserIdSafe();
    if (!uid) { this.router.navigate(['/login']); return; }
    this.fetch(uid);
  }

  fetch(userId: string): void {
    this.loading = true;
    this.orderService.getOrdersByUser(userId).subscribe({
      next: (res) => { this.orders = res?.data || []; this.loading = false; },
      error: (e) => { this.error = e?.error?.message || 'Không tải được danh sách'; this.loading = false; }
    });
  }

  openCancelModal(id: string): void {
    this.cancelModal = { show: true, orderId: id, reason: '' };
  }

  submitCancel(): void {
    const { orderId, reason } = this.cancelModal;
    if (!orderId) return;
    if (!reason || reason.trim().length < 5) {
      alert('Vui lòng nhập lý do hủy (tối thiểu 5 ký tự).');
      return;
    }
    this.canceling[orderId] = true;
    this.orderService.requestCancel(orderId, reason.trim()).subscribe({
      next: () => {
        this.cancelModal.show = false;
        const uid = this.auth.getUserIdSafe();
        if (uid) this.fetch(uid);
      },
      error: (e) => {
        alert(e?.error?.message || 'Gửi yêu cầu hủy thất bại');
      }
    }).add(() => this.canceling[orderId] = false);
  }

  closeCancel(): void { this.cancelModal.show = false; }
}
