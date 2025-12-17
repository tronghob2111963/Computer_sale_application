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
  styleUrls: ['./cart.component.scss'],
})
export class CartComponent implements OnInit {
  loading = false;
  error: string | null = null;
  cart: CartDTO | null = null;
  private readonly baseUrl = environment.apiUrl || 'http://localhost:8080';

  // UI state
  updating = false;
  toast = { show: false, type: '' as 'success' | 'error' | '', message: '' };

  // Selection state
  selectedItems: Record<string, boolean> = {};

  constructor(
    private cartService: CartService,
    private auth: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const uid = this.auth.getUserId?.() || '';
    if (!uid) {
      this.router.navigate(['/login']);
      return;
    }
    this.fetch(uid);
  }

  // Computed: total of all items
  get total(): number {
    if (!this.cart) return 0;
    return this.cart.items.reduce((s, i) => s + i.subtotal, 0);
  }

  // Computed: total of selected items only
  get selectedTotal(): number {
    if (!this.cart) return 0;
    return this.cart.items
      .filter((it) => this.selectedItems[it.productId])
      .reduce((s, i) => s + i.subtotal, 0);
  }

  // Computed: count of selected items
  get selectedCount(): number {
    return Object.values(this.selectedItems).filter(Boolean).length;
  }

  // Computed: check if all items are selected
  get isAllSelected(): boolean {
    if (!this.cart || this.cart.items.length === 0) return false;
    return this.cart.items.every((it) => this.selectedItems[it.productId]);
  }

  // Toggle single item selection
  toggleSelect(productId: string): void {
    this.selectedItems[productId] = !this.selectedItems[productId];
  }

  // Toggle select all
  toggleSelectAll(): void {
    if (!this.cart) return;
    const newState = !this.isAllSelected;
    this.cart.items.forEach((it) => {
      this.selectedItems[it.productId] = newState;
    });
  }

  // Get selected items list
  getSelectedItems(): CartItem[] {
    if (!this.cart) return [];
    return this.cart.items.filter((it) => this.selectedItems[it.productId]);
  }

  // Proceed to checkout with selected items
  proceedToCheckout(): void {
    const selected = this.getSelectedItems();
    if (selected.length === 0) {
      this.err('Vui lòng chọn sản phẩm để thanh toán');
      return;
    }

    // Store selected product IDs in sessionStorage for checkout page
    const selectedIds = selected.map((it) => it.productId);
    sessionStorage.setItem('checkoutItems', JSON.stringify(selectedIds));

    this.router.navigate(['/checkout']);
  }

  // Remove all selected items
  removeSelected(): void {
    if (!this.cart) return;
    const selected = this.getSelectedItems();
    if (selected.length === 0) return;

    if (!confirm(`Bạn có chắc muốn xóa ${selected.length} sản phẩm đã chọn?`)) {
      return;
    }

    this.updating = true;
    const uid = this.cart.userId;

    // Remove items sequentially
    const removeNext = (index: number) => {
      if (index >= selected.length) {
        this.updating = false;
        this.ok('Đã xóa các sản phẩm đã chọn');
        return;
      }

      const item = selected[index];
      this.cartService.removeItem(uid, item.productId).subscribe({
        next: (c) => {
          this.cart = this.normalizeCart(c);
          delete this.selectedItems[item.productId];
          removeNext(index + 1);
        },
        error: () => {
          this.updating = false;
          this.err('Xóa sản phẩm thất bại');
        },
      });
    };

    removeNext(0);
  }

  fetch(userId: string): void {
    this.loading = true;
    this.error = null;
    this.cartService.viewCart(userId).subscribe({
      next: (c) => {
        this.cart = this.normalizeCart(c);
        this.loading = false;
        // Initialize selection state (default: all selected)
        this.cart.items.forEach((it) => {
          if (this.selectedItems[it.productId] === undefined) {
            this.selectedItems[it.productId] = true;
          }
        });
      },
      error: (err) => {
        this.error = 'Không thể tải giỏ hàng';
        this.loading = false;
        console.error(err);
      },
    });
  }

  inc(item: CartItem): void {
    if (!this.cart) return;
    const uid = this.cart.userId;
    this.updating = true;
    this.cartService
      .updateQuantity(uid, item.productId, item.quantity + 1)
      .subscribe({
        next: (c) => {
          this.cart = this.normalizeCart(c);
          this.ok('Đã cập nhật số lượng');
        },
        error: () => this.err('Cập nhật số lượng thất bại'),
      })
      .add(() => (this.updating = false));
  }

  dec(item: CartItem): void {
    if (!this.cart) return;
    const newQty = Math.max(1, item.quantity - 1);
    const uid = this.cart.userId;
    this.updating = true;
    this.cartService.updateQuantity(uid, item.productId, newQty).subscribe({
      next: (c) => {
        this.cart = this.normalizeCart(c);
        this.ok('Đã cập nhật số lượng');
      },
      error: () => this.err('Cập nhật số lượng thất bại'),
    })
      .add(() => (this.updating = false));
  }

  remove(item: CartItem): void {
    if (!this.cart) return;
    const uid = this.cart.userId;
    this.updating = true;
    this.cartService.removeItem(uid, item.productId).subscribe({
      next: (c) => {
        this.cart = this.normalizeCart(c);
        delete this.selectedItems[item.productId];
        this.ok('Đã xóa sản phẩm');
      },
      error: () => this.err('Xóa sản phẩm thất bại'),
    })
      .add(() => (this.updating = false));
  }

  private ok(msg: string) {
    this.toast = { show: true, type: 'success', message: msg };
    setTimeout(() => (this.toast.show = false), 1800);
  }

  private err(msg: string) {
    this.toast = { show: true, type: 'error', message: msg };
    setTimeout(() => (this.toast.show = false), 2200);
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

  private normalizeCart(c: CartDTO): CartDTO {
    return {
      ...c,
      items: (c.items || []).map((it) => ({
        ...it,
        productImg: this.toAbsoluteImage(it.productImg),
      })),
    };
  }
}
