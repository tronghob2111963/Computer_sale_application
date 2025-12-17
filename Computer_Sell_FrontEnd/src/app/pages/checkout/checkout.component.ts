import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Title, Meta } from '@angular/platform-browser';
import { CartDTO, CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';
import { PromotionService, PromotionResponse } from '../../services/promotion.service';
import { PaymentService, VietQRPaymentResponse } from '../../services/payment.service';
import { environment } from '../../enviroment';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit {
  cart: CartDTO | null = null;
  checkoutItems: { productId: string; quantity: number; productName: string; price: number; subtotal: number; productImg?: string }[] = [];
  loading = false;
  submitting = false;
  error: string | null = null;
  private readonly baseUrl = environment.apiUrl || 'http://localhost:8080';
  // Form state
  paymentMethod = 'CASH';
  promoCode = '';
  note = '';
  toast = { show: false, type: '' as 'success' | 'error' | '', message: '' };
  // Promo state
  promoInfo: PromotionResponse | null = null;
  promoChecking = false;
  promoError: string | null = null;

  // VietQR state
  showVietQRModal = false;
  vietQRData: VietQRPaymentResponse | null = null;
  proofFile: File | null = null;
  proofPreview: string | null = null;
  uploadingProof = false;
  proofUploaded = false;
  currentOrderId: string | null = null;

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private promoService: PromotionService,
    private paymentService: PaymentService,
    private auth: AuthService,
    private router: Router,
    private title: Title,
    private meta: Meta,
  ) { }

  ngOnInit(): void {
    this.title.setTitle('Thanh toan - THComputer');
    this.meta.updateTag({ name: 'description', content: 'Xem lai gio hang va hoan tat dat hang tai THComputer. Nhieu uu dai va ma giam gia.' });

    const uid = this.auth.getUserIdSafe();
    if (!uid) { this.router.navigate(['/login']); return; }

    // Get selected items from sessionStorage
    const selectedIdsJson = sessionStorage.getItem('checkoutItems');
    const selectedIds: string[] = selectedIdsJson ? JSON.parse(selectedIdsJson) : [];

    this.loading = true;
    this.cartService.viewCart(uid).subscribe({
      next: (c) => {
        this.cart = c;
        // Filter only selected items, or all if none specified
        const items = (c.items || []).map(it => ({
          ...it,
          productImg: this.toAbsoluteImage(it.productImg)
        }));

        if (selectedIds.length > 0) {
          this.checkoutItems = items.filter(it => selectedIds.includes(it.productId));
        } else {
          this.checkoutItems = items;
        }

        if (this.checkoutItems.length === 0) {
          this.error = 'Khong co san pham nao duoc chon de thanh toan';
        }
        this.loading = false;
      },
      error: () => { this.error = 'Khong the tai gio hang'; this.loading = false; }
    });
  }

  private toAbsoluteImage(path?: string): string | undefined {
    if (!path) return undefined;
    if (/^https?:\/\//i.test(path)) return path;
    let p = path.startsWith('/') ? path : `/${path}`;
    if (!p.startsWith('/uploads/')) {
      p = `/uploads/products${p}`;
    }
    return `${this.baseUrl}${p}`;
  }

  get total(): number {
    return this.checkoutItems.reduce((s, i) => s + i.subtotal, 0);
  }

  get discount(): number {
    if (!this.promoInfo) return 0;
    const percent = this.promoInfo.discountPercent || 0;
    return Math.floor((this.total * percent) / 100);
  }

  get finalTotal(): number { return Math.max(0, this.total - this.discount); }

  applyPromo(): void {
    const code = (this.promoCode || '').trim();
    if (!code) { this.promoInfo = null; this.promoError = null; return; }
    this.promoChecking = true;
    this.promoService.getByCode(code).subscribe({
      next: (res) => { this.promoInfo = res?.data || null; this.promoError = null; },
      error: (e) => { this.promoInfo = null; this.promoError = e?.error?.message || 'Ma khuyen mai khong hop le'; }
    }).add(() => this.promoChecking = false);
  }

  placeOrder(): void {
    if (this.checkoutItems.length === 0) return;
    const userId = this.auth.getUserIdSafe();
    if (!userId) { this.router.navigate(['/login']); return; }

    this.submitting = true;
    const payload = {
      userId,
      paymentMethod: this.paymentMethod,
      promoCode: this.promoCode?.trim() || null,
      items: this.checkoutItems.map(it => ({ productId: it.productId, quantity: it.quantity }))
    };

    this.orderService.createOrder(payload).subscribe({
      next: (res) => {
        const order = res?.data || {};
        const id = order.id || order.orderId; // try common keys

        if (this.paymentMethod === 'VNPAY') {
          if (!id) {
            this.toast = { show: true, type: 'error', message: 'Khong lay duoc ma don hang de thanh toan VNPay' };
            this.submitting = false;
            return;
          }
          this.toast = { show: true, type: 'success', message: 'Dang chuyen sang VNPay...' };
          this.startVnpayPayment(String(id));
          return;
        }

        if (this.paymentMethod === 'VIETQR') {
          if (!id) {
            this.toast = { show: true, type: 'error', message: 'Khong lay duoc ma don hang de thanh toan VietQR' };
            this.submitting = false;
            return;
          }
          this.currentOrderId = String(id);
          this.startVietQRPayment(String(id));
          return;
        }

        this.toast = { show: true, type: 'success', message: 'Dat hang thanh cong' };
        setTimeout(() => {
          if (id) this.router.navigate(['/order', id]);
          else this.router.navigate(['/orders']);
        }, 600);
        this.submitting = false;
      },
      error: (e) => {
        const msg = e?.error?.message || 'Dat hang that bai';
        this.toast = { show: true, type: 'error', message: msg };
        this.submitting = false;
      }
    });
  }

  private startVnpayPayment(orderId: string): void {
    this.paymentService.createVnpayPayment(orderId).subscribe({
      next: (res) => {
        const redirectUrl = res?.data?.transactionId;
        if (redirectUrl) {
          window.location.href = redirectUrl;
          return;
        }
        this.toast = { show: true, type: 'error', message: 'Khong nhan duoc URL thanh toan VNPay' };
        this.router.navigate(['/order', orderId]);
        this.submitting = false;
      },
      error: (e) => {
        const msg = e?.error?.message || 'Khong tao duoc giao dich VNPay';
        this.toast = { show: true, type: 'error', message: msg };
        this.router.navigate(['/order', orderId]);
        this.submitting = false;
      }
    });
  }

  // VietQR Methods
  private startVietQRPayment(orderId: string): void {
    this.paymentService.createVietQRPayment(orderId).subscribe({
      next: (res) => {
        this.vietQRData = res?.data || null;
        if (this.vietQRData) {
          this.showVietQRModal = true;
          this.toast = { show: true, type: 'success', message: 'Vui long quet ma QR de chuyen khoan' };
        } else {
          this.toast = { show: true, type: 'error', message: 'Khong tao duoc ma QR' };
          this.router.navigate(['/order', orderId]);
        }
        this.submitting = false;
      },
      error: (e) => {
        const msg = e?.error?.message || 'Khong tao duoc giao dich VietQR';
        this.toast = { show: true, type: 'error', message: msg };
        this.router.navigate(['/order', orderId]);
        this.submitting = false;
      }
    });
  }

  onProofFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.proofFile = input.files[0];
      // Create preview
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
        this.toast = { show: true, type: 'success', message: 'Da gui anh xac nhan thanh cong!' };
      },
      error: (e) => {
        this.uploadingProof = false;
        const msg = e?.error?.message || 'Khong the upload anh xac nhan';
        this.toast = { show: true, type: 'error', message: msg };
      }
    });
  }

  closeVietQRModal(): void {
    this.showVietQRModal = false;
    if (this.currentOrderId) {
      this.router.navigate(['/order', this.currentOrderId]);
    }
  }

  goToOrderDetail(): void {
    this.showVietQRModal = false;
    if (this.currentOrderId) {
      this.router.navigate(['/order', this.currentOrderId]);
    } else {
      this.router.navigate(['/orders']);
    }
  }
}
