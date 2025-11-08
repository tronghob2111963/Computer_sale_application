import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Title, Meta } from '@angular/platform-browser';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss']
})
export class OrderDetailComponent implements OnInit {
  loading = false;
  error: string | null = null;
  order: any = null;
  canceling = false;
  cancelModal = { show: false, reason: '' as string };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private title: Title,
    private meta: Meta,
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.router.navigate(['/']); return; }
    this.loading = true;
    this.orderService.getOrderById(id).subscribe({
      next: (res) => {
        this.order = res?.data || null;
        const title = `Đơn hàng #${this.order?.id || id} - THComputer`;
        this.title.setTitle(title);
        this.meta.updateTag({ name: 'description', content: 'Chi tiết đơn hàng, trạng thái thanh toán và danh sách sản phẩm.' });
        this.loading = false;
      },
      error: (e) => { this.error = e?.error?.message || 'Không tải được đơn hàng'; this.loading = false; }
    });
  }

  get items(): any[] { return this.order?.details || this.order?.items || []; }
  get total(): number { return this.order?.totalAmount ?? 0; }
  get status(): string { return this.order?.status || 'N/A'; }
  get paymentMethod(): string { return this.order?.paymentMethod || 'N/A'; }
  get paymentStatus(): string { return this.order?.paymentStatus || 'N/A'; }

  // UI actions: cancel request when PENDING
  openCancel(): void { this.cancelModal = { show: true, reason: '' }; }
  closeCancel(): void { this.cancelModal.show = false; }
  submitCancel(): void {
    if (!this.order?.id) return;
    if (!this.cancelModal.reason || this.cancelModal.reason.trim().length < 5) {
      alert('Vui lòng nhập lý do hủy (tối thiểu 5 ký tự).');
      return;
    }
    this.canceling = true;
    this.orderService.requestCancel(this.order.id, this.cancelModal.reason.trim()).subscribe({
      next: () => {
        this.cancelModal.show = false;
        // refresh detail
        this.ngOnInit();
      },
      error: (e) => alert(e?.error?.message || 'Gửi yêu cầu hủy thất bại')
    }).add(() => this.canceling = false);
  }
}
