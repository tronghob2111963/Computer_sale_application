import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService, CategoryDTO } from '../../../services/category.service';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-categories.component.html',
  styleUrls: ['./admin-categories.component.scss']
})
export class AdminCategoriesComponent implements OnInit {
  loading = false;
  error: string | null = null;
  categories: CategoryDTO[] = [];

  keyword = '';
  page = 0;
  size = 10;
  sortBy = 'name:asc';
  pageNo = 1;
  totalPages = 0;
  totalElements = 0;

  showModal = false;
  form = { name: '', description: '' };

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true; this.error = null;
    this.categoryService.listCategories({ keyword: this.keyword || undefined, page: this.page, size: this.size, sortBy: this.sortBy }).subscribe({
      next: (res) => {
        const payload: any = (res as any)?.data ?? res;
        const page: any = payload?.data ?? payload;
        const items = Array.isArray(page?.items) ? page.items : (Array.isArray(page?.content) ? page.content : []);
        this.categories = items;
        this.pageNo = page?.pageNo ?? (this.page + 1);
        this.totalPages = page?.totalPages ?? 0;
        this.totalElements = page?.totalElements ?? items.length;
        this.loading = false;
      },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Không tải được danh mục'; }
    });
  }

  search(){ this.page = 0; this.load(); }
  changePage(delta: number){ const next = this.page + delta; if(next < 0 || (this.totalPages && next >= this.totalPages)) return; this.page = next; this.load(); }
  changePageSize(){ this.page = 0; this.load(); }

  openCreate(){ this.form = { name: '', description: '' }; this.showModal = true; }
  save(){
    if(!this.form.name?.trim()) { this.error = 'Vui lòng nhập tên danh mục'; return; }
    this.loading = true;
    this.categoryService.createCategory({ name: this.form.name.trim(), description: this.form.description || undefined }).subscribe({
      next: () => { this.loading = false; this.showModal = false; this.load(); },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Lưu danh mục thất bại (có thể BE chưa mở endpoint /category/save)'; }
    });
  }
}
