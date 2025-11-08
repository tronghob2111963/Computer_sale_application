import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Title, Meta } from '@angular/platform-browser';
import { CartDTO, CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';
import { PromotionService, PromotionResponse } from '../../services/promotion.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit {
  cart: CartDTO | null = null;
  loading = false;
  submitting = false;
  error: string | null = null;
  // Form state
  paymentMethod = 'CASH';
  promoCode = '';
  note = '';
  toast = { show: false, type: '' as 'success'|'error'|'', message: '' };
  // Promo state
  promoInfo: PromotionResponse | null = null;
  promoChecking = false;
  promoError: string | null = null;

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private promoService: PromotionService,
    private auth: AuthService,
    private router: Router,
    private title: Title,
    private meta: Meta,
  ) {}

  ngOnInit(): void {
    this.title.setTitle('Thanh toán - THComputer');
    this.meta.updateTag({ name: 'description', content: 'Xem lại giỏ hàng và hoàn tất đặt hàng tại THComputer. Ưu đãi, mã giảm giá và nhiều phương thức thanh toán.' });

    const uid = this.auth.getUserIdSafe();
    if (!uid) { this.router.navigate(['/login']); return; }
    this.loading = true;
    this.cartService.viewCart(uid).subscribe({
      next: (c) => { this.cart = c; this.loading = false; },
      error: () => { this.error = 'Không thể tải giỏ hàng'; this.loading = false; }
    });
  }

  get total(): number {
    if (!this.cart) return 0;
    return (this.cart.items || []).reduce((s, i) => s + i.subtotal, 0);
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
      error: (e) => { this.promoInfo = null; this.promoError = e?.error?.message || 'Mã khuyến mãi không hợp lệ'; }
    }).add(() => this.promoChecking = false);
  }

  placeOrder(): void {
    if (!this.cart) return;
    const userId = this.auth.getUserIdSafe();
    if (!userId) { this.router.navigate(['/login']); return; }
    if (!this.cart.items || this.cart.items.length === 0) { return; }

    this.submitting = true;
    const payload = {
      userId,
      paymentMethod: this.paymentMethod,
      promoCode: this.promoCode?.trim() || null,
      items: this.cart.items.map(it => ({ productId: it.productId, quantity: it.quantity }))
    };

    this.orderService.createOrder(payload).subscribe({
      next: (res) => {
        const order = res?.data || {};
        const id = order.id || order.orderId; // try common keys
        this.toast = { show: true, type: 'success', message: 'Đặt hàng thành công' };
        setTimeout(() => {
          if (id) this.router.navigate(['/order', id]);
          else this.router.navigate(['/orders']);
        }, 600);
      },
      error: (e) => {
        const msg = e?.error?.message || 'Đặt hàng thất bại';
        this.toast = { show: true, type: 'error', message: msg };
      }
    }).add(() => this.submitting = false);
  }
}
