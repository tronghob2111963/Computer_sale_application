import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Title, Meta } from '@angular/platform-browser';
import { OrderService } from '../../services/order.service';
import { PaymentService, VietQRPaymentResponse } from '../../services/payment.service';

type TimelineStep = { key: string; label: string; date?: Date | string | null };
type Tone = 'success' | 'info' | 'warning' | 'danger';

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
  timelineSteps: TimelineStep[] = [];
  currentTimelineIndex = 0;

  // VietQR state
  vietQRData: VietQRPaymentResponse | null = null;
  showVietQRModal = false;
  proofFile: File | null = null;
  proofPreview: string | null = null;
  uploadingProof = false;
  proofUploaded = false;

  private readonly baseTimeline: TimelineStep[] = [
    { key: 'PENDING', label: 'Đặt hàng thành công' },
    { key: 'CONFIRMED', label: 'Đã xác nhận' },
    { key: 'SHIPPING', label: 'Đang vận chuyển' },
    { key: 'COMPLETED', label: 'Đã nhận hàng' }
  ];

  private readonly statusLabelMap: Record<string, string> = {
    PENDING: 'Chờ xử lý',
    CONFIRMED: 'Đã xác nhận',
    SHIPPING: 'Đang vận chuyển',
    COMPLETED: 'Hoàn tất',
    CANCELED: 'Đã hủy',
    CANCEL_REQUEST: 'Đang chờ hủy'
  };

  private readonly statusToneMap: Record<string, Tone> = {
    PENDING: 'info',
    CONFIRMED: 'info',
    SHIPPING: 'warning',
    COMPLETED: 'success',
    CANCELED: 'danger',
    CANCEL_REQUEST: 'warning'
  };

  private readonly paymentStatusLabelMap: Record<string, string> = {
    UNPAID: "Chưa thanh toán",
    PENDING: "Đang chờ thanh toán",
    PAID: "Đã thanh toán",
    REFUNDED: "Đã hoàn tiền",
    SUCCESS: "Thanh toán thành công"
  };

  private readonly paymentToneMap: Record<string, Tone> = {
    UNPAID: "warning",
    PENDING: "info",
    PAID: "success",
    SUCCESS: "success",
    REFUNDED: "info"
  };

  private readonly paymentMethodLabelMap: Record<string, string> = {
    CASH: 'Thanh toán khi nhận hàng',
    VNPAY: 'VNPay',
    MOMO: 'Momo',
    VIETQR: 'Chuyển khoản VietQR'
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private paymentService: PaymentService,
    private title: Title,
    private meta: Meta,
  ) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.router.navigate(['/']); return; }
    this.loading = true;
    this.orderService.getOrderById(id).subscribe({
      next: (res) => {
        this.order = res?.data || null;
        const title = `Đơn Hàng #${this.order?.id || id} - THComputer`;
        this.title.setTitle(title);
        this.meta.updateTag({ name: 'description', content: 'Chi tiết đơn hàng, trạng thái thanh toán và danh sách sản phẩm.' });
        this.refreshViewModel();
        this.loading = false;
      },
      error: (e) => {
        this.error = e?.error?.message || 'Không thể tải được đơn hàng';
        this.refreshViewModel();
        this.loading = false;
      }
    });
  }

  get items(): any[] { return this.order?.details || this.order?.items || []; }
  get total(): number { return Number(this.order?.totalAmount ?? 0); }
  get status(): string { return this.order?.status || 'N/A'; }
  get paymentMethod(): string { return this.order?.paymentMethod || 'N/A'; }
  get paymentStatus(): string { return this.order?.paymentStatus || 'N/A'; }
  get statusLabel(): string {
    return this.statusLabelMap[this.normalizedStatus] || this.order?.status || 'Đang cập nhật';
  }
  get paymentStatusLabel(): string {
    return this.paymentStatusLabelMap[this.paymentStatusCode] || this.order?.paymentStatus || 'Đang cập nhật';
  }
  get paymentMethodLabel(): string {
    return this.paymentMethodLabelMap[this.paymentMethodCode] || this.order?.paymentMethod || 'Không xác định';
  }
  get statusBadgeTone(): Tone {
    return this.statusToneMap[this.normalizedStatus] || 'info';
  }
  get paymentBadgeTone(): Tone {
    return this.paymentToneMap[this.paymentStatusCode] || 'info';
  }
  get itemsSubtotal(): number {
    return this.items.reduce((sum, item) => {
      const unit = Number(item?.unitPrice ?? item?.price ?? 0);
      const qty = Number(item?.quantity ?? 0);
      const subtotal = Number(item?.subtotal ?? (unit * qty));
      return sum + subtotal;
    }, 0);
  }
  get discount(): number {
    return Number(this.order?.discount ?? 0);
  }
  get shippingFee(): number {
    return Number(this.order?.shippingFee ?? 0);
  }
  get finalTotal(): number {
    if (this.order?.grandTotal != null) { return Number(this.order.grandTotal); }
    if (this.order?.totalAmount != null) { return Number(this.order.totalAmount); }
    return this.itemsSubtotal - this.discount + this.shippingFee;
  }
  get firstItem(): any | null {
    return this.items.length > 0 ? this.items[0] : null;
  }

  get orderCode(): string {
    return this.order?.code || this.order?.id || 'N/A';
  }

  get customerName(): string {
    const fullName = [
      this.order?.user?.firstName,
      this.order?.user?.lastName
    ].filter(Boolean).join(' ').trim();
    return this.order?.customerName
      || this.order?.fullName
      || this.order?.User_fullName
      || this.order?.user_fullName
      || fullName
      || 'Chua cap nhat';
  }

  get customerPhone(): string {
    return this.order?.customerPhone
      || this.order?.phoneNumber
      || this.order?.User_phone
      || this.order?.user_phone
      || this.order?.user?.phone
      || 'Chua cap nhat';
  }

  get customerAddress(): string {
    return this.order?.shippingAddress
      || this.order?.address
      || this.order?.User_address
      || this.order?.user?.address
      || 'Chua cap nhat';
  }

  get customerNote(): string {
    return this.order?.note || this.order?.customerNote || 'Khong co';
  }

  // UI actions: cancel request when PENDING
  openCancel(): void { this.cancelModal = { show: true, reason: '' }; }
  closeCancel(): void { this.cancelModal.show = false; }
  submitCancel(): void {
    if (!this.order?.id) return;
    if (!this.cancelModal.reason || this.cancelModal.reason.trim().length < 5) {
      alert('Vui lòng nhập lí do hủy (tối thiểu 5 kí tự).');
      return;
    }
    this.canceling = true;
    this.orderService.requestCancel(this.order.id, this.cancelModal.reason.trim()).subscribe({
      next: () => {
        this.cancelModal.show = false;
        // refresh detail
        this.ngOnInit();
      },
      error: (e) => alert(e?.error?.message || 'Gọi yêu cầu hủy thất bại')
    }).add(() => this.canceling = false);
  }

  hideImage(event: Event): void {
    const target = event?.target as HTMLImageElement | null;
    if (target) {
      target.style.display = 'none';
    }
  }

  private refreshViewModel(): void {
    if (!this.order) {
      this.timelineSteps = [];
      this.currentTimelineIndex = 0;
      return;
    }
    this.timelineSteps = this.buildTimelineSteps();
    this.currentTimelineIndex = this.resolveTimelineIndex(this.timelineSteps);
  }

  private buildTimelineSteps(): TimelineStep[] {
    const steps: TimelineStep[] = [...this.baseTimeline];

    if (this.normalizedStatus === 'CANCEL_REQUEST') {
      steps.push({ key: 'CANCEL_REQUEST', label: 'Đang chờ hủy' });
    } else if (this.normalizedStatus === 'CANCELED') {
      steps.push({ key: 'CANCELED', label: 'Đã hủy' });
    }

    return steps.map((step, index) => ({
      ...step,
      date: index === 0 ? (this.order?.orderDate || this.order?.createdAt || null) : step.date
    }));
  }

  private resolveTimelineIndex(steps: TimelineStep[]): number {
    if (!steps.length) { return 0; }
    const fallbackKey = steps[0].key;
    const targetKey = this.normalizedStatus || fallbackKey;
    const foundIndex = steps.findIndex(step => step.key === targetKey);
    return foundIndex >= 0 ? foundIndex : 0;
  }

  private get normalizedStatus(): string {
    return (this.order?.status || '').toUpperCase();
  }

  private get paymentStatusCode(): string {
    return (this.order?.paymentStatus || '').toUpperCase();
  }

  private get paymentMethodCode(): string {
    return (this.order?.paymentMethod || '').toUpperCase();
  }

  // VietQR Methods
  get isVietQRPayment(): boolean {
    return this.paymentMethodCode === 'VIETQR';
  }

  get canUploadVietQRProof(): boolean {
    return this.isVietQRPayment &&
      ['PENDING', 'UNPAID'].includes(this.paymentStatusCode);
  }

  openVietQRModal(): void {
    if (!this.order?.id) return;

    this.paymentService.createVietQRPayment(this.order.id).subscribe({
      next: (res) => {
        this.vietQRData = res?.data || null;
        if (this.vietQRData) {
          this.showVietQRModal = true;
        }
      },
      error: (e) => {
        alert(e?.error?.message || 'Không thể tạo mã QR');
      }
    });
  }

  closeVietQRModal(): void {
    this.showVietQRModal = false;
  }

  onProofFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.proofFile = input.files[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        this.proofPreview = e.target?.result as string;
      };
      reader.readAsDataURL(this.proofFile);
    }
  }

  removeProofImage(): void {
    this.proofFile = null;
    this.proofPreview = null;
  }

  uploadProofImage(): void {
    if (!this.proofFile || !this.vietQRData) return;

    this.uploadingProof = true;
    this.paymentService.uploadVietQRProof(this.vietQRData.id, this.proofFile).subscribe({
      next: () => {
        this.proofUploaded = true;
        this.uploadingProof = false;
        alert('Đã gửi ảnh xác nhận thành công! Vui lòng chờ admin xác nhận.');
      },
      error: (e) => {
        this.uploadingProof = false;
        alert(e?.error?.message || 'Không thể upload ảnh xác nhận');
      }
    });
  }
}

