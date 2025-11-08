import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProductService, PageResponse, ResponseEnvelope } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../enviroment';
import { buildImageUrl } from '../../utils/image.util';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  loading = false;
  error: string | null = null;
  products: any[] = [];
  categoryName: string | null = null;
  private readonly baseUrl = environment.apiUrl || 'http://localhost:8080';

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private auth: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const catId = params['categoryId'];
      this.categoryName = params['categoryName'] || null;
      this.fetchProducts(catId);
    });
  }

  fetchProducts(categoryId?: string): void {
    this.loading = true;
    this.error = null;
    const source$ = categoryId
      ? this.productService.listProductsByCategory<any>(categoryId, { page: 0, size: 12, sortBy: 'createdAt:desc' })
      : this.productService.listProducts<any>({ page: 0, size: 12, sortBy: 'createdAt:desc' });
    source$.subscribe({
        next: (res: ResponseEnvelope<PageResponse<any>>) => {
          const items = res?.data?.items ?? [];
          this.products = items.map((p: any) => ({
            ...p,
            displayImage: buildImageUrl(this.baseUrl, Array.isArray(p?.image) && p.image.length ? p.image[0] : undefined)
          }));
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Không thể tải danh sách sản phẩm';
          this.loading = false;
          console.error(err);
        }
      });
  }

  refresh(): void {
    const id = this.route.snapshot.queryParamMap.get('categoryId') || undefined as any;
    this.fetchProducts(id);
  }

  addToCart(p: any): void {
    const userId = this.auth.getUserId?.() || '';
    if (!userId) {
      alert('Vui lòng đăng nhập để thêm vào giỏ hàng');
      return;
    }
    this.cartService.addToCart(userId, p?.id, 1).subscribe({
      next: () => alert('Đã thêm vào giỏ hàng'),
      error: () => alert('Thêm vào giỏ hàng thất bại')
    });
  }
}
