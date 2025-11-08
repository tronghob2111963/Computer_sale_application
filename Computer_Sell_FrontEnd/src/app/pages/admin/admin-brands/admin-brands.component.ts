import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BrandService, BrandDTO } from '../../../services/brand.service';

@Component({
  selector: 'app-admin-brands',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-brands.component.html',
  styleUrls: ['./admin-brands.component.scss']
})
export class AdminBrandsComponent implements OnInit {
  loading = false;
  error: string | null = null;
  brands: BrandDTO[] = [];

  keyword = '';
  page = 0;
  size = 10;
  sortBy = 'name:asc';
  pageNo = 1;
  totalPages = 0;
  totalElements = 0;

  showModal = false;
  form = { name: '', country: '', description: '' };

  constructor(private brandService: BrandService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true; this.error = null;
    this.brandService.listBrands({ keyword: this.keyword || undefined, page: this.page, size: this.size, sortBy: this.sortBy }).subscribe({
      next: (res) => {
        const payload: any = (res as any)?.data ?? res;
        const page: any = payload?.data ?? payload;
        const items = Array.isArray(page?.items) ? page.items : (Array.isArray(page?.content) ? page.content : []);
        this.brands = items;
        this.pageNo = page?.pageNo ?? (this.page + 1);
        this.totalPages = page?.totalPages ?? 0;
        this.totalElements = page?.totalElements ?? items.length;
        this.loading = false;
      },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Không tải được danh sách thương hiệu'; }
    });
  }

  search(){ this.page = 0; this.load(); }
  changePage(delta: number){ const next = this.page + delta; if(next < 0 || (this.totalPages && next >= this.totalPages)) return; this.page = next; this.load(); }
  changePageSize(){ this.page = 0; this.load(); }

  openCreate(){ this.form = { name: '', country: '', description: '' }; this.showModal = true; }
  save(){
    if(!this.form.name?.trim()) { this.error = 'Vui lòng nhập tên thương hiệu'; return; }
    this.loading = true;
    this.brandService.createBrand({ name: this.form.name.trim(), country: this.form.country || undefined, description: this.form.description || undefined })
      .subscribe({
        next: () => { this.loading = false; this.showModal = false; this.load(); },
        error: (err) => { this.loading = false; this.error = err?.error?.message || 'Lưu thương hiệu thất bại'; }
      });
  }
}
