import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PromotionService, PromotionResponse, PromotionRequest, ResponseEnvelope } from '../../../services/promotion.service';

@Component({
  selector: 'app-admin-promotions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-promotions.component.html',
  styleUrls: ['./admin-promotions.component.scss']
})
export class AdminPromotionsComponent implements OnInit {
  loading = false;
  error: string | null = null;
  success: string | null = null;

  promotions: PromotionResponse[] = [];

  showModal = false;
  isEditing = false;
  form: (PromotionRequest & { id?: string }) = {
    promoCode: '',
    description: '',
    discountPercent: 0,
    startDate: new Date().toISOString().slice(0, 10),
    endDate: new Date().toISOString().slice(0, 10),
    isActive: true
  };

  constructor(private promoService: PromotionService) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.error = null;
    this.promoService.listAll().subscribe({
      next: (res: ResponseEnvelope<PromotionResponse[]>) => {
        this.promotions = Array.isArray(res?.data) ? res.data : [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Không tải được danh sách khuyến mãi';
      }
    });
  }

  openCreate(): void {
    this.isEditing = false;
    this.showModal = true;
    this.error = null;
    this.success = null;
    this.form = {
      promoCode: '',
      description: '',
      discountPercent: 0,
      startDate: new Date().toISOString().slice(0, 10),
      endDate: new Date().toISOString().slice(0, 10),
      isActive: true
    };
  }

  openEdit(p: PromotionResponse): void {
    this.isEditing = true;
    this.showModal = true;
    this.error = null;
    this.success = null;
    this.form = {
      id: p.id,
      promoCode: p.promoCode,
      description: p.description || '',
      discountPercent: p.discountPercent,
      startDate: (p.startDate || '').slice(0, 10),
      endDate: (p.endDate || '').slice(0, 10),
      isActive: p.isActive
    };
  }

  submit(): void {
    this.error = null;
    this.success = null;
    // basic validation
    if (!this.form.promoCode && !this.isEditing) {
      this.error = 'Vui lòng nhập mã khuyến mãi';
      return;
    }
    if (this.form.discountPercent == null || this.form.discountPercent < 0) {
      this.error = 'Phần trăm giảm không hợp lệ';
      return;
    }
    if (!this.form.startDate || !this.form.endDate) {
      this.error = 'Vui lòng chọn ngày bắt đầu/kết thúc';
      return;
    }

    const toLocalDateTime = (d: string) => {
      if (!d) return d;
      // If input is date-only (yyyy-MM-dd), convert to ISO_LOCAL_DATE_TIME expected by BE
      return d.length === 10 ? `${d}T00:00:00` : d;
    };

    const payload: PromotionRequest = {
      promoCode: this.form.promoCode,
      description: this.form.description || '',
      discountPercent: Number(this.form.discountPercent),
      startDate: toLocalDateTime(this.form.startDate) as string,
      endDate: toLocalDateTime(this.form.endDate) as string,
      isActive: this.form.isActive ?? true
    };

    this.loading = true;
    const obs = this.isEditing && this.form.id
      ? this.promoService.update(this.form.id, payload)
      : this.promoService.create(payload);

    obs.subscribe({
      next: (res) => {
        this.loading = false;
        this.success = res?.message || (this.isEditing ? 'Cập nhật thành công' : 'Tạo mới thành công');
        this.showModal = false;
        this.loadAll();
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || (this.isEditing ? 'Cập nhật thất bại' : 'Tạo mới thất bại');
      }
    });
  }

  remove(p: PromotionResponse): void {
    if (!confirm(`Xóa khuyến mãi ${p.promoCode}?`)) return;
    this.loading = true;
    this.error = null;
    this.promoService.delete(p.id).subscribe({
      next: () => { this.loading = false; this.loadAll(); },
      error: (err) => { this.loading = false; this.error = err?.error?.message || 'Xóa thất bại'; }
    });
  }
}
