import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProductService, PageResponse, ResponseEnvelope } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../enviroment';
import { buildImageUrl } from '../../utils/image.util';
import { ProductTypeService, ProductTypeDTO } from '../../services/product-type.service';

interface ProductTypeTab {
  label: string;
  typeId: string;
}

interface ProductSection {
  title: string;
  banner: string;
  cta?: string;
  tabs: ProductTypeTab[];
  selectedTypeId?: string;
  products: any[];
  loading: boolean;
}

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
  productTypes: ProductTypeDTO[] = [];
  selectedTypeId: string | null = null;
  categoryName: string | null = null;
  sections: ProductSection[] = [];
  readonly stars = Array.from({ length: 5 });
  private readonly baseUrl = environment.apiUrl || 'http://localhost:8080';

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private auth: AuthService,
    private route: ActivatedRoute,
    private productTypeService: ProductTypeService
  ) {}

  ngOnInit(): void {
    this.loadProductTypes();
    this.route.queryParams.subscribe(params => {
      const catId = params['categoryId'];
      this.categoryName = params['categoryName'] || null;
      this.fetchProducts(catId);
    });
  }

  loadProductTypes(): void {
    this.productTypeService.listProductTypes().subscribe({
      next: (res) => {
        const data = res?.data;
        const items = Array.isArray(data) ? data : Array.isArray(data?.items) ? data.items : [];
        this.productTypes = items;
        this.buildSections();
      },
      error: (err) => console.error('Error loading product types', err)
    });
  }

  fetchProducts(categoryId?: string): void {
    this.loading = true;
    this.error = null;
    const source$ = this.selectedTypeId
      ? this.productService.listProductsByProductType<any>(this.selectedTypeId, { page: 0, size: 12, sortBy: 'createdAt:desc' })
      : categoryId
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
          this.error = 'Khong the tai danh sach san pham';
          this.loading = false;
          console.error(err);
        }
      });
  }

  refresh(): void {
    const id = this.route.snapshot.queryParamMap.get('categoryId') || undefined as any;
    this.fetchProducts(id);
  }

  selectProductType(type: ProductTypeDTO | null): void {
    this.selectedTypeId = type?.id || null;
    this.categoryName = type?.name || null;
    this.fetchProducts(this.route.snapshot.queryParamMap.get('categoryId') || undefined as any);
  }

  addToCart(p: any): void {
    const userId = this.auth.getUserId?.() || '';
    if (!userId) {
      alert('Vui long dang nhap de them vao gio hang');
      return;
    }
    this.cartService.addToCart(userId, p?.id, 1).subscribe({
      next: () => alert('Da them vao gio hang'),
      error: () => alert('Them vao gio hang that bai')
    });
  }

  // Section-based filtering by product type (tabs per section)
  buildSections(): void {
    this.sections = this.productTypes.map((pt) => {
      const slug = this.toSlug(pt.name);
      return {
        title: pt.name,
        banner: `/banners/${slug}.jpg`,
        cta: `Xem tat ca ${pt.name}`,
        tabs: [{ label: pt.name, typeId: pt.id }],
        selectedTypeId: pt.id,
        products: [],
        loading: false,
      } as ProductSection;
    });

    this.sections.forEach(section => {
      if (section.selectedTypeId) {
        this.loadSectionProducts(section);
      }
    });
  }

  selectSectionTab(section: ProductSection, tab: ProductTypeTab): void {
    section.selectedTypeId = tab.typeId;
    this.loadSectionProducts(section);
  }

  private loadSectionProducts(section: ProductSection): void {
    if (!section.selectedTypeId) return;
    section.loading = true;
    this.productService.listProductsByProductType<any>(section.selectedTypeId, { page: 0, size: 10, sortBy: 'createdAt:desc' })
      .subscribe({
        next: (res: ResponseEnvelope<PageResponse<any>>) => {
          const items = res?.data?.items ?? [];
          section.products = items.map((p: any) => ({
            ...p,
            displayImage: buildImageUrl(this.baseUrl, Array.isArray(p?.image) && p.image.length ? p.image[0] : undefined)
          }));
          section.loading = false;
        },
        error: (err) => {
          console.error('Load section products error', err);
          section.loading = false;
        }
      });
  }

  getSelectedTabLabel(section: ProductSection): string {
    const found = section.tabs.find((t) => t.typeId === section.selectedTypeId);
    return found?.label || '';
  }

  private toSlug(value: string): string {
    return (value || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-zA-Z0-9]/g, '')
      .toLowerCase();
  }
}
