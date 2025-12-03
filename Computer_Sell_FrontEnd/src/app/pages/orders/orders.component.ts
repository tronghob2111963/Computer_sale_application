import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Meta, Title } from '@angular/platform-browser';
import { AuthService } from '../../services/auth.service';
import {
  DashboardNavItem,
  OrderDashboardResponse,
  OrderService
} from '../../services/order.service';
import { PaymentService } from '../../services/payment.service';

type Tone = 'success' | 'info' | 'warning' | 'danger';
type OnlineProvider = 'VNPAY' | 'MOMO';

interface FlowStage {
  id: string;
  title: string;
  description: string;
  highlight?: string;
  icon: string;
}

interface PaymentChip {
  id: string;
  methodLabel: string;
  methodCode: string;
  statusLabel: string;
  statusCode: string;
  tone: Tone;
  amount: number;
  paymentDate?: string;
}

interface OrderCardView {
  id: string;
  code: string;
  orderDate?: string;
  statusLabel: string;
  statusTone: Tone;
  statusCode: string;
  paymentStatusLabel: string;
  paymentStatusTone: Tone;
  paymentStatusCode: string;
  totalAmount: number;
  itemsCount: number;
  detailsPreview: string;
  payments: PaymentChip[];
  cancellable: boolean;
  canPayCash: boolean;
  canPayOnline: boolean;
  promoCode?: string | null;
}

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit, OnDestroy {
  dashboardLoading = false;
  ordersLoading = false;
  error: string | null = null;
  ordersRaw: any[] = [];
  orderCards: OrderCardView[] = [];
  pendingOrders: OrderCardView[] = [];
  dashboard?: OrderDashboardResponse;
  sidebarItems: DashboardNavItem[] = [];
  shortcutItems: DashboardNavItem[] = [];
  activeSection = 'overview';
  canceling: Record<string, boolean> = {};
  cancelModal = { show: false, orderId: '' as string, reason: '' as string };
  paying: Record<string, boolean> = {};
  toast = { show: false, tone: 'info' as Tone, message: '' };
  readonly onlineOptions: { key: OnlineProvider; label: string; helper: string }[] = [
    { key: 'VNPAY', label: 'VNPay', helper: 'Cổng ngân hàng nội địa' },
    { key: 'MOMO', label: 'MoMo', helper: 'Ví điện tử phổ biến' }
  ];
  readonly flowStages: FlowStage[] = [
    {
      id: 'cart',
      title: 'Chọn sản phẩm',
      description: 'Khách hàng duyệt catalogue và thêm cấu hình mong muốn vào giỏ hàng.',
      highlight: 'Bước A • B',
      icon: '🛒'
    },
    {
      id: 'checkout',
      title: 'Gửi yêu cầu đặt hàng',
      description: 'Ngay khi nhấn "Tiến hành đặt hàng", FE gọi API /api/orders/create.',
      highlight: 'Bước C • D',
      icon: '🧾'
    },
    {
      id: 'pending',
      title: 'Tạo Order + Payment',
      description: 'Backend trả về OrderEntity kèm OrderDetail & PaymentEntity với status PENDING/UNPAID.',
      highlight: 'Bước E → L',
      icon: '⚙️'
    },
    {
      id: 'payment',
      title: 'Khách thanh toán',
      description: 'FE hiển thị trạng thái và cho phép chọn CASH, VNPay hoặc MoMo để hoàn tất.',
      highlight: 'Bước M → Q',
      icon: '💳'
    },
    {
      id: 'aftercare',
      title: 'Theo dõi / Huỷ đơn',
      description: 'Nếu chưa thanh toán, khách có thể yêu cầu huỷ; khi đã PAID thì xử lý refund.',
      highlight: 'Bước R → T',
      icon: '🌀'
    }
  ];
  readonly toneClasses: Record<string, string> = {
    success: 'bg-emerald-50 text-emerald-700 border border-emerald-200',
    info: 'bg-sky-50 text-sky-700 border border-sky-200',
    warning: 'bg-amber-50 text-amber-700 border border-amber-200',
    danger: 'bg-rose-50 text-rose-700 border border-rose-200'
  };
  readonly toneSoftClasses: Record<string, string> = {
    success: 'bg-emerald-100/60 text-emerald-700',
    info: 'bg-sky-100/70 text-sky-700',
    warning: 'bg-amber-100 text-amber-700',
    danger: 'bg-rose-100 text-rose-700'
  };
  readonly toastToneClasses: Record<string, string> = {
    success: 'bg-emerald-600 text-white',
    info: 'bg-sky-600 text-white',
    warning: 'bg-amber-500 text-white',
    danger: 'bg-rose-600 text-white'
  };
  private toastTimer?: ReturnType<typeof setTimeout>;

  private readonly statusLabelMap: Record<string, string> = {
    PENDING: 'Đang chờ duyệt',
    CONFIRMED: 'Đã xác nhận',
    SHIPPING: 'Đang vận chuyển',
    COMPLETED: 'Hoàn tất',
    CANCELED: 'Đã hủy',
    CANCEL_REQUEST: 'Chờ hủy'
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
    UNPAID: "Chua thanh toan",
    PENDING: "Dang cho thanh toan",
    PAID: "Da thanh toan",
    REFUNDED: "Da hoan tien",
    SUCCESS: "Thanh toan thanh cong"
  };

  private readonly paymentToneMap: Record<string, Tone> = {
    UNPAID: "warning",
    PENDING: "info",
    PAID: "success",
    SUCCESS: "success",
    REFUNDED: "info"
  };

  private readonly paymentMethodLabelMap: Record<string, string> = {
    CASH: 'Tiền mặt',
    VNPAY: 'VNPay',
    MOMO: 'MoMo'
  };

  constructor(
    private orderService: OrderService,
    private paymentService: PaymentService,
    private auth: AuthService,
    private router: Router,
    private title: Title,
    private meta: Meta
  ) {}

  ngOnInit(): void {
    this.title.setTitle('Trung tâm khách hàng - THComputer');
    this.meta.updateTag({
      name: 'description',
      content: 'Theo dõi trạng thái đơn hàng, thanh toán và ưu đãi dành riêng cho bạn tại THComputer.'
    });

    const uid = this.auth.getUserIdSafe();
    if (!uid) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadDashboard(uid);
    this.loadOrders(uid);
  }

  ngOnDestroy(): void {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
  }

  openCancelModal(id: string): void {
    this.cancelModal = { show: true, orderId: id, reason: '' };
  }

  closeCancel(): void {
    this.cancelModal.show = false;
  }

  submitCancel(): void {
    const { orderId, reason } = this.cancelModal;
    if (!orderId) {
      return;
    }
    if (!reason || reason.trim().length < 5) {
      this.showToast('warning', 'Vui lòng nhập lý do tối thiểu 5 ký tự.');
      return;
    }
    this.canceling[orderId] = true;
    this.orderService
      .requestCancel(orderId, reason.trim())
      .subscribe({
        next: () => {
          this.cancelModal.show = false;
          const uid = this.auth.getUserIdSafe();
          if (uid) {
            this.loadOrders(uid);
            this.loadDashboard(uid);
          }
          this.showToast('success', 'Đã gửi yêu cầu hủy đơn.');
        },
        error: (e) => {
          this.showToast('danger', e?.error?.message || 'Gửi yêu cầu hủy thất bại.');
        }
      })
      .add(() => (this.canceling[orderId] = false));
  }

  payCash(orderId: string): void {
    if (!orderId || this.paying[orderId]) {
      return;
    }
    const target = this.orderCards.find((card) => card.id === orderId);
    const label = target?.code || '#Đơn hàng';
    const confirmMessage = `Xác nhận khách đã thanh toán tiền mặt cho ${label}?`;
    if (!window.confirm(confirmMessage)) {
      return;
    }
    this.paying[orderId] = true;
    this.paymentService
      .createCashPayment(orderId)
      .subscribe({
        next: () => {
          this.showToast('success', 'Đã ghi nhận thanh toán tiền mặt.');
          const uid = this.auth.getUserIdSafe();
          if (uid) {
            this.loadOrders(uid);
            this.loadDashboard(uid);
          }
        },
        error: (e) => {
          this.showToast('danger', e?.error?.message || 'Không thể cập nhật thanh toán.');
        }
      })
      .add(() => (this.paying[orderId] = false));
  }

  startOnlinePayment(orderId: string, provider: OnlineProvider): void {
    if (!orderId || this.paying[orderId]) {
      return;
    }

    if (provider !== 'VNPAY') {
      this.showToast('info', `${provider} se duoc ho tro sau khi cau hinh cong thanh toan.`);
      return;
    }

    this.paying[orderId] = true;
    this.paymentService
      .createVnpayPayment(orderId)
      .subscribe({
        next: (res) => {
          const url = res?.data?.transactionId;
          if (url) {
            this.showToast('info', 'Dang chuyen sang VNPay...');
            window.location.href = url;
            return;
          }
          this.showToast('danger', 'Khong nhan duoc duong dan VNPay.');
        },
        error: (e) => {
          this.showToast('danger', e?.error?.message || 'Khong tao duoc giao dich VNPay.');
        }
      })
      .add(() => (this.paying[orderId] = false));
  }

  scrollTo(section: string): void {
    this.activeSection = section;
    const target = document.getElementById(section);
    if (target) {
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  reloadData(): void {
    const uid = this.auth.getUserIdSafe();
    if (uid) {
      this.loadDashboard(uid);
      this.loadOrders(uid);
    }
  }

  private loadDashboard(userId: string): void {
    this.dashboardLoading = true;
    this.orderService.getDashboard(userId).subscribe({
      next: (res) => {
        this.dashboard = res?.data;
        const sections = this.dashboard?.sections ?? [];
        this.sidebarItems = this.ensureSidebarItems(sections);
        this.shortcutItems = this.sidebarItems.filter((item) => item.pinned);
        this.dashboardLoading = false;
      },
      error: (e) => {
        this.error = e?.error?.message || 'Không thể tải thông tin tài khoản.';
        this.dashboardLoading = false;
      }
    });
  }

  private ensureSidebarItems(items: DashboardNavItem[]): DashboardNavItem[] {
    const normalized = [...items];
    const required: DashboardNavItem[] = [
      { key: 'overview', label: 'Tổng quan', icon: '🎯', description: 'Thông tin thành viên', pinned: false },
      { key: 'payments', label: 'Thanh toán', icon: '💳', description: 'Theo dõi thanh toán', pinned: false },
      { key: 'orders-list', label: 'Đơn hàng', icon: '📦', description: 'Lịch sử mua hàng', pinned: false }
    ];
    required.forEach((section) => {
      if (!normalized.some((item) => item.key === section.key)) {
        normalized.push(section);
      }
    });
    return normalized;
  }

  private loadOrders(userId: string): void {
    this.ordersLoading = true;
    this.orderService.getOrdersByUser(userId).subscribe({
      next: (res) => {
        this.ordersRaw = res?.data || [];
        this.refreshOrdersView();
        this.ordersLoading = false;
      },
      error: (e) => {
        this.error = e?.error?.message || 'Không thể tải danh sách đơn hàng.';
        this.ordersRaw = [];
        this.refreshOrdersView();
        this.ordersLoading = false;
      }
    });
  }

  private refreshOrdersView(): void {
    this.orderCards = (this.ordersRaw || []).map((order) => this.mapOrder(order));
    this.pendingOrders = this.orderCards.filter((card) => card.canPayCash || card.canPayOnline);
  }

  private mapOrder(order: any): OrderCardView {
    const statusCode = (order?.status || '').toUpperCase();
    const paymentStatusCode = (order?.paymentStatus || '').toUpperCase();
    const id = String(order?.id || '');
    const details = Array.isArray(order?.details) ? order.details : [];
    const payments = Array.isArray(order?.payments) ? order.payments : [];

    return {
      id,
      code: order?.code || this.buildOrderCode(id),
      orderDate: order?.orderDate || order?.createdAt || null,
      statusLabel: this.statusLabelMap[statusCode] || order?.status || 'Đang cập nhật',
      statusTone: this.statusToneMap[statusCode] || 'info',
      statusCode,
      paymentStatusLabel: this.paymentStatusLabelMap[paymentStatusCode] || order?.paymentStatus || 'Đang cập nhật',
      paymentStatusTone: this.paymentToneMap[paymentStatusCode] || 'info',
      paymentStatusCode,
      totalAmount: Number(order?.totalAmount ?? 0),
      itemsCount: details.reduce((sum: number, item: any) => sum + Number(item?.quantity ?? 0), 0),
      detailsPreview: this.previewItems(details),
      payments: this.mapPayments(payments),
      cancellable: statusCode === 'PENDING',
      canPayCash: ['UNPAID', 'PENDING'].includes(paymentStatusCode),
      canPayOnline: ['UNPAID', 'PENDING'].includes(paymentStatusCode),
      promoCode: order?.promoCode || null
    };
  }

  private mapPayments(payments: any[]): PaymentChip[] {
    return payments.map((payment) => {
      const statusCode = (payment?.paymentStatus || '').toUpperCase();
      const methodCode = (payment?.paymentMethod || '').toUpperCase();
      return {
        id: String(payment?.id || ''),
        methodLabel: this.paymentMethodLabelMap[methodCode] || payment?.paymentMethod || 'Không rõ',
        methodCode,
        statusLabel: this.paymentStatusLabelMap[statusCode] || payment?.paymentStatus || 'Chưa xác định',
        statusCode,
        tone: this.paymentToneMap[statusCode] || 'info',
        amount: Number(payment?.amount ?? 0),
        paymentDate: payment?.paymentDate || null
      };
    });
  }

  private previewItems(details: any[]): string {
    if (!details.length) {
      return 'Không có sản phẩm nào được ghi nhận.';
    }
    const names = details.map((item) => item?.productName).filter(Boolean) as string[];
    if (!names.length) {
      return `${details.length} sản phẩm`;
    }
    if (names.length <= 2) {
      return names.join(', ');
    }
    const extra = names.length - 2;
    return `${names.slice(0, 2).join(', ')} +${extra} sản phẩm`;
  }

  private buildOrderCode(id: string): string {
    if (!id) {
      return '#Đơn hàng';
    }
    const chunk = id.split('-')[0];
    return `#${chunk?.toUpperCase() || id}`;
  }

  private showToast(tone: Tone, message: string): void {
    this.toast = { show: true, tone, message };
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
    this.toastTimer = setTimeout(() => (this.toast.show = false), 3200);
  }
}


