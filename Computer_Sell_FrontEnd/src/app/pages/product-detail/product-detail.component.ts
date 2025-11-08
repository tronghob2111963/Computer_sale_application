import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProductService, ResponseEnvelope } from '../../services/product.service';
import { environment } from '../../enviroment';
import { buildImageUrl } from '../../utils/image.util';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
  loading = false;
  error: string | null = null;
  product: any = null;
  qty = 1;
  private readonly baseUrl = environment.apiUrl || 'http://localhost:8080';
  private routeId = '';

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.routeId = id;
    this.fetch(id);
  }

  fetch(id: string) {
    this.loading = true;
    this.productService.getProductDetail<any>(id).subscribe({
      next: (res: ResponseEnvelope<any>) => {
        const p = res?.data || {};
        this.product = {
          ...p,
          id: p?.id ?? p?.productId ?? id,
          images: Array.isArray(p?.image)
            ? p.image.map((u: string) => buildImageUrl(this.baseUrl, u) || '')
            : [],
          originalPrice: p?.originalPrice ?? p?.oldPrice ?? p?.listPrice ?? undefined,
          brandName: p?.brandName ?? p?.brand?.name ?? undefined,
          stock: p?.stock ?? p?.quantity ?? undefined,
        };
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Không thể tải chi tiết sản phẩm';
        this.loading = false;
        console.error(err);
      }
    });
  }

  addToCart() {
    const userId = this.auth.getUserId?.() || '';
    if (!userId) {
      alert('Vui lòng đăng nhập để thêm vào giỏ hàng');
      return;
    }
    const pid = this.product?.id ?? this.product?.productId ?? this.routeId;
    if (!pid) {
      alert('Không xác định được sản phẩm');
      return;
    }
    this.cartService.addToCart(userId, pid, this.qty).subscribe({
      next: () => alert('Đã thêm vào giỏ hàng'),
      error: () => alert('Thêm vào giỏ hàng thất bại')
    });
  }

  selectImage(img: string) {
    if (!this.product?.images) return;
    const rest = this.product.images.filter((i: string) => i !== img);
    this.product.images = [img, ...rest];
  }

  decQty() {
    if (this.qty > 1) this.qty -= 1;
  }

  incQty() {
    this.qty += 1;
  }

  get inStockLabel(): string {
    const s = Number(this.product?.stock ?? 0);
    return s > 0 ? 'Còn hàng' : 'Hết hàng';
  }

  get discountPercent(): number | null {
    const price = Number(this.product?.price ?? 0);
    const orig = Number(this.product?.originalPrice ?? 0);
    if (!orig || !price || orig <= price) return null;
    return Math.round(((orig - price) / orig) * 100);
  }
}
