import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProductService, ResponseEnvelope } from '../../../services/product.service';
import { BrandService, BrandDTO } from '../../../services/brand.service';
import { ProductTypeService, ProductTypeDTO } from '../../../services/product-type.service';
import { CategoryService, CategoryDTO } from '../../../services/category.service';

// Fallback environment
const environment = { apiBaseUrl: 'http://localhost:8080' };

@Component({
  standalone: true,
  selector: 'app-admin-products',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-products.component.html',
  styleUrls: ['./admin-products.component.scss']
})
export class AdminProductsComponent implements OnInit {
  products: any[] = [];
  loading = false;
  error: string | null = null;

  keyword = '';
  sortBy = 'name:asc';
  page = 0;
  size = 10;

  pageNo = 1;
  totalPages = 0;
  totalElements = 0;

  // Filters
  filterBrandId = '';
  filterProductTypeId = '';
  filterCategoryId = '';

  showModal = false;
  isEditing = false;
  isViewing = false;
  selectedFiles: File[] = [];
  formModel: any = {
    id: '',
    name: '',
    price: null,
    stock: null,
    warrantyPeriod: null,
    description: '',
    categoryId: '',
    brandId: '',
    productTypeId: ''
  };

  // ? Th�m bi?n cho Toast
  toastMessage: { type: 'success' | 'error'; text: string } | null = null;

  // ?? Th�m baseUrl d? gh�p ?nh
  baseUrl = environment.apiBaseUrl || 'http://localhost:8080';
  // Options for filters
  brandOptions: BrandDTO[] = [];
  productTypeOptions: ProductTypeDTO[] = [];
  categoryOptions: CategoryDTO[] = [];

  constructor(
    private productService: ProductService,
    private brandService: BrandService,
    private productTypeService: ProductTypeService,
    private categoryService: CategoryService
  ) { }

  ngOnInit(): void {
    this.loadProducts();
    this.loadBrandOptions();
    this.loadProductTypeOptions();
    this.loadCategoryOptions();
  }

  // ? H�m hi?n th? toast
  showToast(type: 'success' | 'error', text: string | null) {
    const msg = text ?? (type === 'error' ? '�� x?y ra l?i' : 'Thao t�c th�nh c�ng');
    this.toastMessage = { type, text: msg };
    setTimeout(() => (this.toastMessage = null), 3000);
  }

  loadProducts(): void {
    this.loading = true;
    this.error = null;
    this.productService
      .listProducts({
        keyword: this.keyword || undefined,
        page: this.page,
        size: this.size,
        sortBy: this.sortBy || undefined
      })
      .subscribe({
        next: (res: ResponseEnvelope<any>) => {
          const data: any = (res as any)?.data ?? {};
          const items = Array.isArray(data?.items) ? data.items : [];

          this.products = items.map((p: any) => ({
            ...p,
            image: Array.isArray(p.image)
              ? p.image.map((url: string) => `${this.baseUrl}${url}`)
              : []
          }));

          this.pageNo = data.pageNo ?? 1;
          this.totalPages = data.totalPages ?? 0;
          this.totalElements = data.totalElements ?? 0;
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'Kh�ng t?i du?c danh s�ch s?n ph?m';
          this.loading = false;
          this.showToast('error', this.error);
        }
      });
  }

  onSearch(): void {
    this.page = 0;
    this.loadProducts();
  }

  onSortChange(): void {
    this.page = 0;
    this.loadProducts();
  }

  changePage(delta: number): void {
    const next = this.page + delta;
    if (next < 0 || next >= this.totalPages) return;
    this.page = next;
    this.loadProducts();
  }

  changePageSize(): void {
    this.page = 0;
    this.loadProducts();
  }

  openCreate(): void {
    this.isEditing = false;
    this.showModal = true;
    this.selectedFiles = [];
    // ensure combobox options are loaded when opening the modal
    this.loadBrandOptions();
    this.loadCategoryOptions();
    this.loadProductTypeOptions();
    this.formModel = {
      id: '',
      name: '',
      price: null,
      stock: null,
      warrantyPeriod: null,
      description: '',
      categoryId: '',
      brandId: '',
      productTypeId: ''
    };
  }

  openEdit(item: any): void {
    if (!item?.id) {
      this.error = 'Kh�ng th? ch?nh s?a: thi?u product id trong danh s�ch';
      this.showToast('error', this.error);
      return;
    }
    this.isEditing = true;
    this.showModal = true;
    this.selectedFiles = [];
    // ensure options available when editing
    this.loadBrandOptions();
    this.loadCategoryOptions();
    this.loadProductTypeOptions();
    this.productService.getProductDetail<any>(item.id).subscribe({
      next: (res) => {
        const d = res.data || {};
        this.formModel = {
          id: item.id,
          name: d.name ?? item.name ?? '',
          price: d.price ?? item.price ?? null,
          stock: d.stock ?? null,
          warrantyPeriod: d.warrantyPeriod ?? item.warrantyPeriod ?? null,
          description: d.description ?? '',
          categoryId: d.categoryId ?? '',
          brandId: d.brandId ?? '',
          productTypeId: d.productTypeId ?? ''
        };
      },
      error: (err) => {
        this.error = err?.error?.message || 'Kh�ng t?i du?c chi ti?t s?n ph?m';
        this.showToast('error', this.error);
      }
    });
  }

  onFileChange(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    this.selectedFiles = files;
  }

  submitForm(): void {
    // KHÔNG gửi stock - stock chỉ được thay đổi qua phiếu nhập kho
    const payload: any = {
      name: this.formModel.name,
      price: this.formModel.price,
      // stock: KHÔNG GỬI - quản lý qua phiếu nhập kho
      warrantyPeriod: this.formModel.warrantyPeriod,
      description: this.formModel.description,
      categoryId: this.formModel.categoryId || undefined,
      brandId: this.formModel.brandId || undefined,
      productTypeId: this.formModel.productTypeId || undefined,
      images: this.selectedFiles && this.selectedFiles.length ? this.selectedFiles : undefined
    };

    const obs = this.isEditing
      ? this.productService.updateProduct({ id: this.formModel.id, ...payload })
      : this.productService.createProduct(payload);

    this.loading = true;
    obs.subscribe({
      next: () => {
        this.loading = false;
        this.showModal = false;
        this.loadProducts();
        this.showToast('success', this.isEditing ? 'C?p nh?t s?n ph?m th�nh c�ng!' : 'Th�m s?n ph?m m?i th�nh c�ng!');
      },
      error: (err) => {
        this.loading = false;
        const msg = err?.error?.message || 'Luu s?n ph?m th?t b?i';
        this.error = msg;
        this.showToast('error', msg);
      }
    });
  }

  delete(item: any): void {
    if (!item?.id) {
      this.error = 'Không thể xóa: thiếu product id trong danh sách';
      this.showToast('error', this.error);
      return;
    }
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return;
    this.loading = true;
    this.productService.deleteProduct(item.id).subscribe({
      next: () => {
        this.loading = false;
        this.loadProducts();
        this.showToast('success', 'Đã xóa sản phẩm thành công!');
      },
      error: (err) => {
        this.loading = false;
        const msg = err?.error?.message || 'Xóa sản phẩm thất bại';
        this.error = msg;
        this.showToast('error', msg);
      }
    });
  }

  // Soft delete - chuyển trạng thái sang DELETED
  softDelete(item: any): void {
    if (!item?.id) {
      this.showToast('error', 'Không thể xóa: thiếu product id');
      return;
    }
    if (!confirm('Bạn có chắc muốn ngưng bán sản phẩm này? (Sản phẩm sẽ được ẩn đi)')) return;
    this.loading = true;
    this.productService.softDeleteProduct(item.id).subscribe({
      next: () => {
        this.loading = false;
        this.loadProducts();
        this.showToast('success', 'Đã ngưng bán sản phẩm!');
      },
      error: (err) => {
        this.loading = false;
        this.showToast('error', err?.error?.message || 'Thao tác thất bại');
      }
    });
  }

  // Khôi phục sản phẩm đã xóa mềm
  restoreProduct(item: any): void {
    if (!item?.id) {
      this.showToast('error', 'Không thể khôi phục: thiếu product id');
      return;
    }
    this.loading = true;
    this.productService.restoreProduct(item.id).subscribe({
      next: () => {
        this.loading = false;
        this.loadProducts();
        this.showToast('success', 'Đã khôi phục sản phẩm!');
      },
      error: (err) => {
        this.loading = false;
        this.showToast('error', err?.error?.message || 'Khôi phục thất bại');
      }
    });
  }

  // Cập nhật trạng thái sản phẩm
  updateStatus(item: any, status: 'ACTIVE' | 'INACTIVE' | 'DELETED'): void {
    if (!item?.id) {
      this.showToast('error', 'Không thể cập nhật: thiếu product id');
      return;
    }
    this.loading = true;
    this.productService.updateProductStatus(item.id, status).subscribe({
      next: () => {
        this.loading = false;
        this.loadProducts();
        this.showToast('success', `Đã cập nhật trạng thái thành ${status}!`);
      },
      error: (err) => {
        this.loading = false;
        this.showToast('error', err?.error?.message || 'Cập nhật thất bại');
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'bg-green-100 text-green-800';
      case 'INACTIVE': return 'bg-yellow-100 text-yellow-800';
      case 'DELETED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'Đang bán';
      case 'INACTIVE': return 'Tạm ngưng';
      case 'DELETED': return 'Đã xóa';
      default: return status || 'Đang bán';
    }
  }


  private loadBrandOptions(): void {
    this.brandService.listBrands({ page: 0, size: 100, sortBy: 'name:asc' }).subscribe({
      next: (res) => {
        const data: any = (res as any)?.data || {};
        this.brandOptions = Array.isArray(data.items) ? data.items : [];
      },
      error: () => {
        this.brandOptions = [];
      }
    });
  }

  private loadProductTypeOptions(): void {
    this.productTypeService.listProductTypes().subscribe({
      next: (res) => {
        const data: any = (res as any)?.data;
        this.productTypeOptions = Array.isArray(data) ? data : (Array.isArray(data?.items) ? data.items : []);
      },
      error: () => {
        this.productTypeOptions = [];
      }
    });
  }
  private loadCategoryOptions(): void {
    this.categoryService.listCategories({ page: 0, size: 100, sortBy: 'name:asc' }).subscribe({
      next: (res) => {
        const data: any = (res as any)?.data ?? {};
        const items = Array.isArray(data?.items)
          ? data.items
          : (Array.isArray((data as any)?.content) ? (data as any).content : (Array.isArray(data) ? data : []));
        this.categoryOptions = items;
      },
      error: () => {
        this.categoryOptions = [];
      }
    });
  }
  showDetailModal = false;
  detailModel: any = null;

  onFilterBrand(): void {
    if (!this.filterBrandId) { this.loadProducts(); return; }
    this.loading = true;
    this.error = null;
    this.productService
      .listProductsByBrand(this.filterBrandId, {
        keyword: this.keyword || undefined,
        page: this.page,
        size: this.size,
        sortBy: this.sortBy || undefined
      })
      .subscribe({
        next: (res: ResponseEnvelope<any>) => {
          const data: any = (res as any)?.data ?? {};
          const items = Array.isArray(data?.items) ? data.items : [];
          this.products = items.map((p: any) => ({
            ...p,
            image: Array.isArray(p.image) ? p.image.map((url: string) => `${this.baseUrl}${url}`) : []
          }));
          this.pageNo = data.pageNo ?? 1;
          this.totalPages = data.totalPages ?? 0;
          this.totalElements = data.totalElements ?? 0;
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'L?c theo h�ng th?t b?i';
          this.loading = false;
          this.showToast('error', this.error);
        }
      });
  }

  onFilterProductType(): void {
    if (!this.filterProductTypeId) { this.loadProducts(); return; }
    this.loading = true;
    this.error = null;
    this.productService
      .listProductsByProductType(this.filterProductTypeId, { keyword: this.keyword || undefined, page: this.page, size: this.size, sortBy: this.sortBy || undefined })
      .subscribe({
        next: (res: ResponseEnvelope<any>) => {
          const data: any = (res as any)?.data ?? {};
          const items = Array.isArray(data?.items) ? data.items : [];
          this.products = items.map((p: any) => ({
            ...p,
            image: Array.isArray(p.image) ? p.image.map((url: string) => `${this.baseUrl}${url}`) : []
          }));
          this.pageNo = data.pageNo ?? 1;
          this.totalPages = data.totalPages ?? 0;
          this.totalElements = data.totalElements ?? items.length;
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'L?c theo lo?i s?n ph?m th?t b?i';
          this.loading = false;
          this.showToast('error', this.error);
        }
      });
  }

  onFilterCategory(): void {
    if (!this.filterCategoryId) { this.loadProducts(); return; }
    this.loading = true;
    this.error = null;
    this.productService
      .listProductsByCategory(this.filterCategoryId, {
        keyword: this.keyword || undefined,
        page: this.page,
        size: this.size,
        sortBy: this.sortBy || undefined
      })
      .subscribe({
        next: (res: ResponseEnvelope<any>) => {
          const data: any = (res as any)?.data ?? {};
          const items = Array.isArray(data?.items) ? data.items : [];
          this.products = items.map((p: any) => ({
            ...p,
            image: Array.isArray(p.image) ? p.image.map((url: string) => `${this.baseUrl}${url}`) : []
          }));
          this.pageNo = data.pageNo ?? 1;
          this.totalPages = data.totalPages ?? 0;
          this.totalElements = data.totalElements ?? 0;
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'Lọc theo danh mục thất bại';
          this.loading = false;
          this.showToast('error', this.error);
        }
      });
  }

  clearFilters(): void {
    this.filterBrandId = '';
    this.filterProductTypeId = '';
    this.filterCategoryId = '';
    this.page = 0;
    this.loadProducts();
  }

  // ===============
  // Detail support
  // ===============
  openDetail(item: any): void {
    if (!item?.id) {
      this.error = 'Kh�ng th? xem chi ti?t: thi?u product id trong danh s�ch';
      this.showToast('error', this.error);
      return;
    }
    this.showDetailModal = true;
    this.detailModel = null;
    this.productService.getProductDetail<any>(item.id).subscribe({
      next: (res) => {
        this.detailModel = res.data || item;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Kh�ng t?i du?c chi ti?t s?n ph?m';
        this.showToast('error', this.error);
      }
    });
  }

  closeDetail(): void {
    this.showDetailModal = false;
    this.detailModel = null;
  }
}

// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { ProductService, ResponseEnvelope } from '../../../services/product.service';
// // Fallback environment object to avoid missing environments file during compile.
// // If you have a proper src/environments/environment.ts, replace this with the import again.
// const environment = { apiBaseUrl: 'http://localhost:8080' };

// @Component({
//   standalone: true,
//   selector: 'app-admin-products',
//   imports: [CommonModule, FormsModule],
//   templateUrl: './admin-products.component.html',
//   styleUrls: ['./admin-products.component.scss']
// })
// export class AdminProductsComponent implements OnInit {
//   products: any[] = [];
//   loading = false;
//   error: string | null = null;

//   keyword = '';
//   sortBy = 'name:asc';
//   page = 0;
//   size = 10;

//   pageNo = 1;
//   totalPages = 0;
//   totalElements = 0;

//   showModal = false;
//   isEditing = false;
//   selectedFiles: File[] = [];
//   formModel: any = {
//     id: '',
//     name: '',
//     price: null,
//     stock: null,
//     warrantyPeriod: null,
//     description: '',
//     categoryId: '',
//     brandId: '',
//     productTypeId: ''
//   };

//   // ?? Th�m baseUrl d? gh�p ?nh
//   baseUrl = environment.apiBaseUrl || 'http://localhost:8080';

//   constructor(private productService: ProductService) {}

//   ngOnInit(): void {
//     this.loadProducts();
//   }

//   loadProducts(): void {
//     this.loading = true;
//     this.error = null;
//     this.productService
//       .listProducts({
//         keyword: this.keyword || undefined,
//         page: this.page,
//         size: this.size,
//         sortBy: this.sortBy || undefined
//       })
//       .subscribe({
//         next: (res: ResponseEnvelope<any>) => {
//           const data: any = (res as any)?.data ?? {};
//           const items = Array.isArray(data?.items) ? data.items : [];

//           // GH�P baseUrl cho c�c du?ng d?n ?nh
//           this.products = items.map((p: any) => ({
//             ...p,
//             image: Array.isArray(p.image)
//               ? p.image.map((url: string) => `${this.baseUrl}${url}`)
//               : []
//           }));

//           this.pageNo = data.pageNo ?? 1;
//           this.totalPages = data.totalPages ?? 0;
//           this.totalElements = data.totalElements ?? 0;
//           this.loading = false;
//         },
//         error: (err) => {
//           this.error = err?.error?.message || 'Kh�ng t?i du?c danh s�ch s?n ph?m';
//           this.loading = false;
//         }
//       });
//   }

//   onSearch(): void {
//     this.page = 0;
//     this.loadProducts();
//   }

//   onSortChange(): void {
//     this.page = 0;
//     this.loadProducts();
//   }

//   changePage(delta: number): void {
//     const next = this.page + delta;
//     if (next < 0 || next >= this.totalPages) return;
//     this.page = next;
//     this.loadProducts();
//   }

//   changePageSize(): void {
//     this.page = 0;
//     this.loadProducts();
//   }

//   openCreate(): void {
//     this.isEditing = false;
//     this.showModal = true;
//     this.selectedFiles = [];
//     this.formModel = {
//       id: '',
//       name: '',
//       price: null,
//       stock: null,
//       warrantyPeriod: null,
//       description: '',
//       categoryId: '',
//       brandId: '',
//       productTypeId: ''
//     };
//   }

//   openEdit(item: any): void {
//     if (!item?.id) {
//       this.error = 'Kh�ng th? ch?nh s?a: thi?u product id trong danh s�ch';
//       return;
//     }
//     this.isEditing = true;
//     this.showModal = true;
//     this.selectedFiles = [];
//     this.productService.getProductDetail<any>(item.id).subscribe({
//       next: (res) => {
//         const d = res.data || {};
//         this.formModel = {
//           id: item.id,
//           name: d.name ?? item.name ?? '',
//           price: d.price ?? item.price ?? null,
//           stock: d.stock ?? null,
//           warrantyPeriod: d.warrantyPeriod ?? item.warrantyPeriod ?? null,
//           description: d.description ?? '',
//           categoryId: d.categoryId ?? '',
//           brandId: d.brandId ?? '',
//           productTypeId: d.productTypeId ?? ''
//         };
//       },
//       error: (err) => {
//         this.error = err?.error?.message || 'Kh�ng t?i du?c chi ti?t s?n ph?m';
//       }
//     });
//   }

//   onFileChange(ev: Event): void {
//     const input = ev.target as HTMLInputElement;
//     const files = input.files ? Array.from(input.files) : [];
//     this.selectedFiles = files;
//   }

//   submitForm(): void {
//     const payload: any = {
//       name: this.formModel.name,
//       price: this.formModel.price,
//       stock: this.formModel.stock,
//       warrantyPeriod: this.formModel.warrantyPeriod,
//       description: this.formModel.description,
//       categoryId: this.formModel.categoryId || undefined,
//       brandId: this.formModel.brandId || undefined,
//       productTypeId: this.formModel.productTypeId || undefined,
//       images: this.selectedFiles && this.selectedFiles.length ? this.selectedFiles : undefined
//     };

//     const obs = this.isEditing
//       ? this.productService.updateProduct({ id: this.formModel.id, ...payload })
//       : this.productService.createProduct(payload);

//     this.loading = true;
//     obs.subscribe({
//       next: () => {
//         this.loading = false;
//         this.showModal = false;
//         this.loadProducts();
//       },
//       error: (err) => {
//         this.loading = false;
//         this.error = err?.error?.message || 'Luu s?n ph?m th?t b?i';
//       }
//     });
//   }

//   delete(item: any): void {
//     if (!item?.id) {
//       this.error = 'Kh�ng th? x�a: thi?u product id trong danh s�ch';
//       return;
//     }
//     if (!confirm('B?n c� ch?c mu?n x�a s?n ph?m n�y?')) return;
//     this.loading = true;
//     this.productService.deleteProduct(item.id).subscribe({
//       next: () => {
//         this.loading = false;
//         this.loadProducts();
//       },
//       error: (err) => {
//         this.loading = false;
//         this.error = err?.error?.message || 'X�a s?n ph?m th?t b?i';
//       }
//     });
//   }
// }
