import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserBuildService, UserBuild } from '../../services/user-build.service';
import { ResponseEnvelope, PageResponse } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';

interface BuildCategory {
  id: string;
  name: string;
  icon: string;
  productTypeId?: string;
  selectedProduct?: any;
  quantity: number;
}

@Component({
  selector: 'app-pc-builder',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pc-builder.component.html',
  styleUrls: ['./pc-builder.component.scss']
})
export class PcBuilderComponent implements OnInit {
  buildCategories: BuildCategory[] = [
    { id: 'cpu', name: 'CPU', icon: '/placeholder.svg', quantity: 1 },
    { id: 'mainboard', name: 'MAINBOARD', icon: '/placeholder.svg', quantity: 1 },
    { id: 'ram', name: 'RAM', icon: '/placeholder.svg', quantity: 1 },
    { id: 'gpu', name: 'CARD DO HOA', icon: '/placeholder.svg', quantity: 1 },
    { id: 'storage', name: 'O CUNG', icon: '/placeholder.svg', quantity: 1 },
    { id: 'psu', name: 'NGUON (PSU)', icon: '/placeholder.svg', quantity: 1 },
    { id: 'cooling', name: 'TAN NHIET', icon: '/placeholder.svg', quantity: 1 },
    { id: 'case', name: 'VO CASE', icon: '/placeholder.svg', quantity: 1 },
    { id: 'monitor', name: 'MAN HINH', icon: '/placeholder.svg', quantity: 1 }
  ];

  currentBuild: UserBuild | null = null;
  showProductModal = false;
  selectedCategory: BuildCategory | null = null;
  availableProducts: any[] = [];
  totalPrice = 0;
  buildName = 'Cau hinh PC cua toi';
  isLoading = false;

  constructor(
    private buildService: UserBuildService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  private setCurrentBuildFromResponse(data: any) {
    if (!data) {
      return;
    }

    if (typeof data === 'string') {
      // add-product API currently returns only buildId string
      this.currentBuild = {
        id: data,
        name: this.buildName,
        totalPrice: this.totalPrice,
        isPublic: false,
        details: []
      } as UserBuild;
      return;
    }

    this.currentBuild = data as UserBuild;
  }

  ngOnInit() {
    const userId = this.authService.getUserIdSafe();
    const token = this.authService.getAccessToken();

    if (!userId || !token) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/pc-builder' } });
      return;
    }

    this.loadCategories();

    // Check if loading existing build
    this.route.queryParams.subscribe(params => {
      if (params['buildId']) {
        this.loadExistingBuild(params['buildId']);
      } else {
        this.initializeBuild();
      }
    });
  }

  loadExistingBuild(buildId: string) {
    this.buildService.getBuildDetails(buildId).subscribe({
      next: (build) => {
        this.currentBuild = build;
        this.buildName = build.name;

        build.details.forEach(detail => {
          const category = this.buildCategories.find(cat => !cat.selectedProduct);
          if (category) {
            category.selectedProduct = {
              id: detail.productId,
              name: detail.productName,
              price: detail.price,
              images: detail.imageUrl ? [{ imageUrl: detail.imageUrl }] : []
            };
            category.quantity = detail.quantity;
          }
        });

        this.calculateTotal();
      },
      error: () => {
        this.initializeBuild();
      }
    });
  }

  loadCategories() {
    this.buildService.getProductTypes().subscribe({
      next: (response) => {
        this.mapProductTypesToBuildCategories(response.data);
      },
      error: (err) => console.error('Error loading product types:', err)
    });
  }

  mapProductTypesToBuildCategories(productTypes: any[]) {
    const typeNameMap: { [key: string]: string } = {
      'cpu': 'CPU',
      'mainboard': 'MAINBOARD',
      'ram': 'RAM',
      'gpu': 'GPU',
      'storage': 'O CUNG',
      'psu': 'PSU',
      'case': 'CASE',
      'cooling': 'TAN NHIET',
      'monitor': 'MAN HINH'
    };

    const normalize = (value: string) => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase();

    this.buildCategories.forEach(buildCat => {
      const matchedType = productTypes.find(type =>
        normalize(type.name) === normalize(typeNameMap[buildCat.id])
      );
      if (matchedType) {
        buildCat.productTypeId = matchedType.id;
      }
    });
  }

  initializeBuild() {
    const userId = this.authService.getUserIdSafe();
    if (!userId) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/pc-builder' } });
      return;
    }

    this.buildService.createBuild(userId, this.buildName).subscribe({
      next: (response) => {
        this.setCurrentBuildFromResponse(response.data);
      },
      error: (err) => {
        if (err.status === 403) {
          this.router.navigate(['/login'], { queryParams: { returnUrl: '/pc-builder' } });
        }
      }
    });
  }

  openProductSelector(category: BuildCategory) {
    this.selectedCategory = category;
    this.showProductModal = true;
    this.loadProductsForCategory(category);
  }

  loadProductsForCategory(category: BuildCategory) {
    if (!category.productTypeId) {
      return;
    }

    this.isLoading = true;
    this.buildService.getProductsByType(category.productTypeId, { size: 50 }).subscribe({
      next: (response: ResponseEnvelope<PageResponse<any>>) => {
        this.availableProducts = response.data.items;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading products:', err);
        this.isLoading = false;
      }
    });
  }

  selectProduct(product: any) {
    if (!this.selectedCategory) return;

    const buildId = this.currentBuild?.id;
    if (!buildId) {
      this.initializeBuild();
      return;
    }

    this.buildService.addProductToBuild(
      buildId,
      product.id,
      this.selectedCategory.quantity
    ).subscribe({
      next: (response) => {
        this.setCurrentBuildFromResponse(response.data ?? buildId);
        this.selectedCategory!.selectedProduct = product;
        this.calculateTotal();
        this.closeModal();
      },
      error: (err) => console.error('Error adding product:', err)
    });
  }

  removeProduct(category: BuildCategory) {
    if (!this.currentBuild || !category.selectedProduct) return;

    this.buildService.removeProductFromBuild(
      this.currentBuild.id,
      category.selectedProduct.id
    ).subscribe({
      next: (response) => {
        this.setCurrentBuildFromResponse(response.data ?? this.currentBuild?.id);
        category.selectedProduct = null;
        category.quantity = 1;
        this.calculateTotal();
      },
      error: (err) => console.error('Error removing product:', err)
    });
  }

  updateQuantity(category: BuildCategory) {
    if (!this.currentBuild || !category.selectedProduct) return;

    this.buildService.updateProductQuantity(
      this.currentBuild.id,
      category.selectedProduct.id,
      category.quantity
    ).subscribe({
      next: (response) => {
        this.setCurrentBuildFromResponse(response.data ?? this.currentBuild?.id);
        this.calculateTotal();
      },
      error: (err) => console.error('Error updating quantity:', err)
    });
  }

  calculateTotal() {
    this.totalPrice = this.buildCategories.reduce((sum, cat) => {
      if (cat.selectedProduct) {
        return sum + (cat.selectedProduct.price * cat.quantity);
      }
      return sum;
    }, 0);
  }

  closeModal() {
    this.showProductModal = false;
    this.selectedCategory = null;
    this.availableProducts = [];
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(price);
  }

  getProductImage(product: any): string {
    if (product.images && product.images.length > 0) {
      return product.images[0].imageUrl;
    }
    return '/placeholder.svg';
  }

  get selectedItems(): BuildCategory[] {
    return this.buildCategories.filter(cat => !!cat.selectedProduct);
  }

  printQuote() {
    if (this.selectedItems.length === 0) {
      alert('Chua co san pham de in bao gia.');
      return;
    }

    const rows = this.selectedItems.map(item => `
      <tr>
        <td style="padding:8px;border:1px solid #dbe7fb;text-align:center;">
          <img src="${this.getProductImage(item.selectedProduct)}" alt="${item.selectedProduct.name}" style="width:80px;height:80px;object-fit:cover;border:1px solid #eef4ff;border-radius:6px;">
        </td>
        <td style="padding:8px;border:1px solid #dbe7fb;">${item.selectedProduct.name}</td>
        <td style="padding:8px;border:1px solid #dbe7fb;text-align:center;">${item.quantity}</td>
        <td style="padding:8px;border:1px solid #dbe7fb;text-align:right;font-weight:600;">${this.formatPrice(item.selectedProduct.price)}</td>
        <td style="padding:8px;border:1px solid #dbe7fb;text-align:right;font-weight:700;">${this.formatPrice(item.selectedProduct.price * item.quantity)}</td>
      </tr>
    `).join('');

    const html = `
      <html>
        <head>
          <title>Bao gia PC</title>
          <style>
            body { font-family: Arial, sans-serif; padding: 16px; color: #1f2d3d; }
            h1 { text-align: center; margin-bottom: 16px; }
            table { width: 100%; border-collapse: collapse; }
            th { background: #f3f7ff; border: 1px solid #dbe7fb; padding: 8px; }
            td { background: #fff; }
            .footer { margin-top: 12px; text-align: right; font-size: 16px; font-weight: 700; }
          </style>
        </head>
        <body>
          <h1>Bao gia cau hinh PC</h1>
          <table>
            <thead>
              <tr>
                <th>Hinh anh</th>
                <th>Ten san pham</th>
                <th>So luong</th>
                <th>Don gia (VND)</th>
                <th>Tong cong (VND)</th>
              </tr>
            </thead>
            <tbody>
              ${rows}
            </tbody>
          </table>
          <div class="footer">Tong: ${this.formatPrice(this.totalPrice)}</div>
        </body>
      </html>
    `;

    const printWin = window.open('', '_blank', 'width=1000,height=800');
    if (!printWin) {
      console.error('Cannot open print window');
      return;
    }
    printWin.document.open();
    printWin.document.write(html);
    printWin.document.close();
    printWin.focus();
    setTimeout(() => {
      printWin.print();
      printWin.close();
    }, 300);
  }
}
