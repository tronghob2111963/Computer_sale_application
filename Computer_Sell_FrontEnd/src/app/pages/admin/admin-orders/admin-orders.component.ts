import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminOrderService } from '../../../services/admin-order.service';

type OrderStatusType = 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'COMPLETED' | 'CANCEL_REQUEST' | 'CANCELED';
type PaymentStatusType = 'UNPAID' | 'PAID' | 'SUCCESS' | 'REFUNDED';

interface TimelineNode {
  key: OrderStatusType;
  label: string;
  state: 'done' | 'active' | 'upcoming';
}

interface DocStep {
  title: string;
  description: string;
  icon: string;
  accent: string;
}

interface GuardRule {
  rule: string;
  reason: string;
}

interface SummaryState {
  total: number;
  pending: number;
  shipping: number;
  completed: number;
  cancel: number;
  awaitingPayment: number;
}

interface ModalState {
  show: boolean;
  message: string;
  action: (() => void) | null;
}

@Component({
  standalone: true,
  selector: 'app-admin-orders',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-orders.component.html',
  styleUrls: ['./admin-orders.component.scss']
})
export class AdminOrdersComponent implements OnInit, OnDestroy {
  orders: any[] = [];
  loading = false;
  statuses: OrderStatusType[] = ['PENDING', 'CANCEL_REQUEST', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELED'];
  status: OrderStatusType | null = null;
  start: string | null = null;
  end: string | null = null;
  summary: SummaryState = { total: 0, pending: 0, shipping: 0, completed: 0, cancel: 0, awaitingPayment: 0 };
  toast = { show: false, type: '' as 'success' | 'error' | '', message: '' };
  modal: ModalState = { show: false, message: '', action: null };

  readonly lifecycleSteps: DocStep[] = [
    { title: 'PENDING', description: 'Order was created and waits for stock and customer validation.', icon: '01', accent: 'bg-slate-900 text-white' },
    { title: 'CONFIRMED', description: 'Inventory checked and order is ready for fulfillment.', icon: '02', accent: 'bg-sky-900 text-white' },
    { title: 'SHIPPING', description: 'Package handed to carrier and is in transit.', icon: '03', accent: 'bg-indigo-900 text-white' },
    { title: 'COMPLETED', description: 'Customer received goods. Payment must be synced.', icon: '04', accent: 'bg-emerald-900 text-white' },
    { title: 'CANCEL_REQUEST', description: 'User asked to cancel while order is still pending flow.', icon: '!', accent: 'bg-amber-100 text-amber-700' },
    { title: 'CANCELED', description: 'Order closed after cancel request approval.', icon: 'X', accent: 'bg-rose-100 text-rose-700' }
  ];

  readonly docSteps: DocStep[] = [
    { title: '1. Validate current status', description: 'Block edits when the order is already COMPLETED or CANCELED.', icon: 'S1', accent: 'bg-slate-100 text-slate-800' },
    { title: '2. Check transition rule', description: 'Only allow the next status defined by the flow. Reject invalid jumps.', icon: 'S2', accent: 'bg-slate-100 text-slate-800' },
    { title: '3. Update order status', description: 'Persist the new status once the rule is satisfied.', icon: 'S3', accent: 'bg-slate-100 text-slate-800' },
    { title: '4. Sync payment', description: 'When COMPLETED, mark order.paymentStatus = PAID and payments = SUCCESS.', icon: 'S4', accent: 'bg-slate-100 text-slate-800' }
  ];

  readonly guardRules: GuardRule[] = [
    { rule: 'PENDING → SHIPPING', reason: 'Order must be confirmed before shipping.' },
    { rule: 'SHIPPING → CONFIRMED', reason: 'Flow only moves forward.' },
    { rule: 'COMPLETED → SHIPPING / CONFIRMED', reason: 'Completed orders are immutable.' },
    { rule: 'CANCELED → *', reason: 'Canceled orders cannot be reopened.' },
    { rule: 'CANCEL_REQUEST → CONFIRMED', reason: 'Cancel requests can only be approved or rejected.' }
  ];

  readonly paymentNotes: string[] = [
    'Completing an order automatically marks UNPAID payments as SUCCESS and sets order.paymentStatus = PAID.',
    'COD (CASH) orders should be completed only after the shipper confirms cash collection.',
    'Online gateways (VNPay/MoMo) will eventually update the payment record but the admin flow still relies on this lifecycle.'
  ];

  readonly baseTimeline: OrderStatusType[] = ['PENDING', 'CONFIRMED', 'SHIPPING', 'COMPLETED'];
  readonly statusLabels: Record<OrderStatusType, string> = {
    PENDING: 'Pending',
    CONFIRMED: 'Confirmed',
    SHIPPING: 'Shipping',
    COMPLETED: 'Completed',
    CANCEL_REQUEST: 'Cancel request',
    CANCELED: 'Canceled'
  };

  readonly statusDescriptions: Record<OrderStatusType, string> = {
    PENDING: 'New order, waiting for validation.',
    CONFIRMED: 'Stock checked, ready to ship.',
    SHIPPING: 'In transit with the carrier.',
    COMPLETED: 'Delivered successfully.',
    CANCEL_REQUEST: 'User asked to cancel.',
    CANCELED: 'Closed after cancel process.'
  };

  readonly transitionMap: Record<OrderStatusType, OrderStatusType[]> = {
    PENDING: ['CONFIRMED'],
    CONFIRMED: ['SHIPPING'],
    SHIPPING: ['COMPLETED'],
    COMPLETED: [],
    CANCEL_REQUEST: [],
    CANCELED: []
  };

  readonly confirmMessages: Partial<Record<OrderStatusType, string>> = {
    CONFIRMED: 'Confirm this order after you have validated stock and customer information?',
    SHIPPING: 'Move to SHIPPING when the parcel has been handed over to the carrier?',
    COMPLETED: 'Mark as COMPLETED? All UNPAID payments will switch to SUCCESS and the order payment status becomes PAID automatically.'
  };

  readonly statusBadgeClasses: Record<OrderStatusType, string> = {
    PENDING: 'bg-amber-50 text-amber-700 border border-amber-200',
    CONFIRMED: 'bg-sky-50 text-sky-700 border border-sky-200',
    SHIPPING: 'bg-indigo-50 text-indigo-700 border border-indigo-200',
    COMPLETED: 'bg-emerald-50 text-emerald-700 border border-emerald-200',
    CANCEL_REQUEST: 'bg-amber-100 text-amber-800 border border-amber-200',
    CANCELED: 'bg-rose-50 text-rose-700 border border-rose-200'
  };

  readonly paymentBadgeClasses: Record<string, string> = {
    PAID: 'bg-emerald-100 text-emerald-800 border border-emerald-200',
    SUCCESS: 'bg-emerald-100 text-emerald-800 border border-emerald-200',
    UNPAID: 'bg-amber-100 text-amber-800 border border-amber-200',
    REFUNDED: 'bg-sky-100 text-sky-700 border border-sky-200'
  };

  readonly paymentStatusLabelMap: Record<PaymentStatusType | string, string> = {
    PAID: 'Paid',
    SUCCESS: 'Success',
    UNPAID: 'Unpaid',
    REFUNDED: 'Refunded'
  };

  readonly timelineStateClasses: Record<TimelineNode['state'], string> = {
    done: 'bg-emerald-600 text-white',
    active: 'bg-sky-600 text-white',
    upcoming: 'bg-slate-200 text-slate-500'
  };

  private toastTimer?: ReturnType<typeof setTimeout>;

  constructor(private adminOrders: AdminOrderService) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
  }

  load(): void {
    this.loading = true;
    const filter: Record<string, string> = {};
    if (this.status) {
      filter['status'] = this.status;
    }
    if (this.start) {
      filter['start'] = new Date(this.start).toISOString();
    }
    if (this.end) {
      filter['end'] = new Date(this.end).toISOString();
    }
    this.adminOrders.getOrders(filter).subscribe({
      next: (res: any) => {
        this.orders = res?.data || res || [];
        this.loading = false;
        this.buildSummary();
      },
      error: (e) => {
        this.orders = [];
        this.loading = false;
        this.buildSummary();
        this.showToast(e?.error?.message || 'Cannot load orders', 'error');
      }
    });
  }

  resetFilters(): void {
    this.status = null;
    this.start = null;
    this.end = null;
    this.load();
  }

  chipClass(active: boolean): string {
    return [
      'px-3 py-1 rounded-full border text-sm font-medium transition',
      active ? 'bg-slate-900 text-white border-slate-900 shadow-sm' : 'bg-white text-slate-600 border-slate-200 hover:border-slate-400'
    ].join(' ');
  }

  statusBadge(status: string): string {
    const key = this.normalizeStatus(status);
    return this.statusBadgeClasses[key] || 'bg-slate-100 text-slate-700 border border-slate-200';
  }

  paymentBadge(status: string): string {
    const key = this.normalizePaymentStatus(status);
    return this.paymentBadgeClasses[key] || 'bg-slate-100 text-slate-700 border border-slate-200';
  }

  statusLabel(status: string): string {
    const key = this.normalizeStatus(status);
    return this.statusLabels[key] || status || 'Unknown';
  }

  paymentStatusLabel(status: string): string {
    const key = this.normalizePaymentStatus(status);
    return this.paymentStatusLabelMap[key] || status || 'Unknown';
  }

  allowedNextStatuses(order: any): OrderStatusType[] {
    const key = this.normalizeStatus(order?.status);
    const next = this.transitionMap[key] || [];
    return [...next];
  }

  stateGuardMessage(order: any): string {
    const key = this.normalizeStatus(order?.status);
    if (key === 'COMPLETED') {
      return 'Order is completed and cannot change anymore.';
    }
    if (key === 'CANCELED') {
      return 'Order was canceled and stays locked.';
    }
    if (key === 'CANCEL_REQUEST') {
      return 'Approve or reject the cancel request to finish the flow.';
    }
    if (!this.allowedNextStatuses(order).length) {
      return 'No valid transition is available for this status.';
    }
    return '';
  }

  timelineFor(order: any): TimelineNode[] {
    const key = this.normalizeStatus(order?.status);
    const currentIndex = this.baseTimeline.indexOf(key);
    return this.baseTimeline.map((status, index) => {
      let state: TimelineNode['state'] = 'upcoming';
      if (currentIndex === -1) {
        state = 'upcoming';
      } else if (index < currentIndex) {
        state = 'done';
      } else if (index === currentIndex) {
        state = 'active';
      }
      return {
        key: status,
        label: this.statusLabels[status],
        state
      };
    });
  }

  orderCode(order: any): string {
    if (order?.code) {
      return order.code;
    }
    const id = String(order?.id || '');
    if (!id) {
      return '#Order';
    }
    const chunk = id.split('-')[0];
    return `#${chunk?.toUpperCase() || id}`;
  }

  confirmSetStatus(order: any, status: OrderStatusType): void {
    const message = this.confirmMessages[status] || `Update order to ${status}?`;
    this.modal = {
      show: true,
      message,
      action: () => this.setStatus(order.id, status)
    };
  }

  confirmProcessCancel(order: any, approve: boolean): void {
    const message = approve
      ? 'Approve this cancel request? Order will become CANCELED and payments remain untouched unless UNPAID.'
      : 'Reject this cancel request and keep the current flow?';
    this.modal = {
      show: true,
      message,
      action: () => this.processCancel(order.id, approve)
    };
  }

  closeModal(): void {
    this.modal = { show: false, message: '', action: null };
  }

  confirmModal(): void {
    if (this.modal.action) {
      this.modal.action();
    }
    this.closeModal();
  }

  setStatus(id: string, status: OrderStatusType): void {
    this.adminOrders.updateStatus(id, status).subscribe({
      next: () => {
        this.load();
        this.showToast(`Order updated to ${status}.`, 'success');
      },
      error: (e) => this.showToast(e?.error?.message || 'Update failed', 'error')
    });
  }

  processCancel(id: string, approve: boolean): void {
    this.adminOrders.processCancel(id, approve).subscribe({
      next: () => {
        this.load();
        this.showToast(approve ? 'Cancel request approved.' : 'Cancel request rejected.', 'success');
      },
      error: (e) => this.showToast(e?.error?.message || 'Processing failed', 'error')
    });
  }

  paymentSyncMessage(order: any): string {
    const status = this.normalizeStatus(order?.status);
    if (status === 'COMPLETED') {
      return `Payment sync: ${this.paymentStatusLabel(order?.paymentStatus)}.`;
    }
    return '';
  }

  private buildSummary(): void {
    const summary: SummaryState = {
      total: this.orders.length,
      pending: 0,
      shipping: 0,
      completed: 0,
      cancel: 0,
      awaitingPayment: 0
    };
    this.orders.forEach((order) => {
      const status = this.normalizeStatus(order?.status);
      if (status === 'PENDING') summary.pending += 1;
      if (status === 'SHIPPING') summary.shipping += 1;
      if (status === 'COMPLETED') summary.completed += 1;
      if (status === 'CANCEL_REQUEST' || status === 'CANCELED') summary.cancel += 1;
      const paymentStatus = this.normalizePaymentStatus(order?.paymentStatus);
      if (paymentStatus === 'UNPAID') summary.awaitingPayment += 1;
    });
    this.summary = summary;
  }

  private normalizeStatus(value: any): OrderStatusType {
    const upper = String(value || '').toUpperCase();
    return this.statuses.includes(upper as OrderStatusType) ? (upper as OrderStatusType) : 'PENDING';
  }

  private normalizePaymentStatus(value: any): PaymentStatusType {
    const upper = String(value || '').toUpperCase();
    const known: PaymentStatusType[] = ['UNPAID', 'PAID', 'SUCCESS', 'REFUNDED'];
    return known.includes(upper as PaymentStatusType) ? (upper as PaymentStatusType) : 'UNPAID';
  }

  private showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toast = { show: true, type, message };
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
    this.toastTimer = setTimeout(() => (this.toast.show = false), 2600);
  }
}
