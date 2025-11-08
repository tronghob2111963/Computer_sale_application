import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CartDTO, CartItem, CartService } from '../../services/cart.service';
import { environment } from '../../enviroment';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {
  loading = false;
  error: string | null = null;
  cart: CartDTO | null = null;
  private readonly baseUrl = environment.apiUrl || 'http://localhost:8080';
  // UI state
  updating = false;
  toast = { show: false, type: '' as 'success'|'error'|'', message: '' };

  constructor(
    private cartService: CartService,
    private auth: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    const uid = this.auth.getUserId?.() || '';
    if (!uid) {
      this.router.navigate(['/login']);
      return;
    }
    this.fetch(uid);
  }

  get total(): number {
    if (!this.cart) return 0;
    return this.cart.items.reduce((s, i) => s + i.subtotal, 0);
  }

  fetch(userId: string): void {
    this.loading = true;
    this.error = null;
    this.cartService.viewCart(userId).subscribe({
      next: (c) => {
        this.cart = this.normalizeCart(c);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Không thể tải giỏ hàng';
        this.loading = false;
        console.error(err);
      }
    });
  }

  inc(item: CartItem): void {
    if (!this.cart) return;
    const uid = this.cart.userId;
    this.updating = true;
    this.cartService.updateQuantity(uid, item.productId, item.quantity + 1).subscribe({
      next: (c) => { this.cart = this.normalizeCart(c); this.ok('Đã cập nhật số lượng'); },
      error: () => this.err('Cập nhật số lượng thất bại'),
    }).add(() => this.updating = false);
  }

  dec(item: CartItem): void {
    if (!this.cart) return;
    const newQty = Math.max(1, item.quantity - 1);
    const uid = this.cart.userId;
    this.updating = true;
    this.cartService.updateQuantity(uid, item.productId, newQty).subscribe({
      next: (c) => { this.cart = this.normalizeCart(c); this.ok('Đã cập nhật số lượng'); },
      error: () => this.err('Cập nhật số lượng thất bại')
    }).add(() => this.updating = false);
  }

  remove(item: CartItem): void {
    if (!this.cart) return;
    const uid = this.cart.userId;
    this.updating = true;
    this.cartService.removeItem(uid, item.productId).subscribe({
      next: (c) => { this.cart = this.normalizeCart(c); this.ok('Đã xóa sản phẩm'); },
      error: () => this.err('Xóa sản phẩm thất bại')
    }).add(() => this.updating = false);
  }

  private ok(msg: string) { this.toast = { show: true, type: 'success', message: msg }; setTimeout(()=> this.toast.show=false, 1800); }
  private err(msg: string) { this.toast = { show: true, type: 'error', message: msg }; setTimeout(()=> this.toast.show=false, 2200); }

  private toAbsoluteImage(path?: string): string | undefined {
    if (!path) return undefined;
    if (/^https?:\/\//i.test(path)) return path;
    // Normalize leading slash
    let p = path.startsWith('/') ? path : `/${path}`;
    // If backend only returns filename, prefix with uploads/products
    if (!p.startsWith('/uploads/')) {
      p = `/uploads/products${p}`;
    }
    return `${this.baseUrl}${p}`;
  }

  private normalizeCart(c: CartDTO): CartDTO {
    return {
      ...c,
      items: (c.items || []).map(it => ({
        ...it,
        productImg: this.toAbsoluteImage(it.productImg)
      }))
    };
  }
}
