import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReviewResponse, ReviewService } from '../../../services/review.service';
import { CommentResponse, CommentService } from '../../../services/comment.service';

@Component({
  standalone: true,
  selector: 'app-admin-feedback',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-feedback.component.html',
  styleUrls: ['./admin-feedback.component.scss']
})
export class AdminFeedbackComponent implements OnInit {
  loading = false;
  error: string | null = null;
  items: ReviewResponse[] = [];
  commentItems: CommentResponse[] = [];

  keyword = '';
  status: string | null = null;
  pageNo = 1;
  pageSize = 10;
  totalPages = 1;
  totalElements = 0;
  commentPageNo = 1;
  commentTotalPages = 1;
  commentTotalElements = 0;
  viewMode: 'reviews' | 'comments' = 'reviews';

  readonly statusOptions = [
    { value: null, label: 'Tat ca' },
    { value: 'APPROVED', label: 'Da duyet' },
    { value: 'PENDING', label: 'Cho duyet' },
    { value: 'REJECTED', label: 'Tu choi' }
  ];

  constructor(
    private reviewService: ReviewService,
    private commentService: CommentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReviews();
  }

  switchView(mode: 'reviews' | 'comments') {
    if (this.viewMode === mode) return;
    this.viewMode = mode;
    this.error = null;
    if (mode === 'reviews') {
      this.loadReviews(1);
    } else {
      this.loadComments(1);
    }
  }

  loadReviews(page = this.pageNo) {
    this.loading = true;
    this.error = null;
    this.reviewService
      .adminSearch({
        keyword: this.keyword || undefined,
        status: this.status || undefined,
        pageNo: page,
        pageSize: this.pageSize,
        sortBy: 'createdAt'
      })
      .subscribe({
        next: (res) => {
          const data: any = res?.data ?? res;
          const pageData: any = data?.data ?? data?.page ?? data;
          this.items = Array.isArray(pageData?.items) ? pageData.items : Array.isArray(pageData?.content) ? pageData.content : [];
          this.pageNo = pageData?.pageNo ?? page ?? 1;
          this.totalPages = pageData?.totalPages ?? 1;
          this.totalElements = pageData?.totalElements ?? this.items.length;
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'Khong tai duoc danh sach danh gia.';
          this.loading = false;
          this.items = [];
        }
      });
  }

  loadComments(page = this.commentPageNo) {
    this.loading = true;
    this.error = null;
    this.commentService
      .adminSearch({
        keyword: this.keyword || undefined,
        status: this.status || undefined,
        pageNo: page,
        pageSize: this.pageSize,
        sortBy: 'createdAt'
      })
      .subscribe({
        next: (res) => {
          const data: any = res?.data ?? res;
          const pageData: any = data?.data ?? data?.page ?? data;
          this.commentItems = Array.isArray(pageData?.items) ? pageData.items : Array.isArray(pageData?.content) ? pageData.content : [];
          this.commentPageNo = pageData?.pageNo ?? page ?? 1;
          this.commentTotalPages = pageData?.totalPages ?? 1;
          this.commentTotalElements = pageData?.totalElements ?? this.commentItems.length;
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'Khong tai duoc binh luan.';
          this.loading = false;
          this.commentItems = [];
        }
      });
  }

  applyFilters() {
    if (this.viewMode === 'reviews') {
      this.pageNo = 1;
      this.loadReviews(1);
    } else {
      this.commentPageNo = 1;
      this.loadComments(1);
    }
  }

  changePage(delta: number) {
    if (this.viewMode === 'reviews') {
      const next = this.pageNo + delta;
      if (next < 1 || (this.totalPages && next > this.totalPages)) return;
      this.pageNo = next;
      this.loadReviews(next);
    } else {
      const next = this.commentPageNo + delta;
      if (next < 1 || (this.commentTotalPages && next > this.commentTotalPages)) return;
      this.commentPageNo = next;
      this.loadComments(next);
    }
  }

  changeStatus(item: ReviewResponse, status: string) {
    this.reviewService.updateStatus(item.id, status).subscribe({
      next: () => this.loadReviews(this.pageNo),
      error: () => alert('Cap nhat trang thai that bai')
    });
  }

  goToReviewProduct(item: ReviewResponse) {
    const pid = item?.productId;
    if (!pid) return;
    this.router.navigate(['/product', pid]);
  }

  changeCommentStatus(item: CommentResponse, status: string) {
    this.commentService.updateStatus(item.id, status).subscribe({
      next: () => this.loadComments(this.commentPageNo),
      error: () => alert('Cap nhat trang thai binh luan that bai')
    });
  }

  goToProductDetail(item: CommentResponse) {
    if (!item?.productId) return;
    this.router.navigate(['/product', item.productId]);
  }

  get currentPage(): number {
    return this.viewMode === 'reviews' ? this.pageNo : this.commentPageNo;
  }

  get currentTotalPages(): number {
    return this.viewMode === 'reviews' ? this.totalPages : this.commentTotalPages;
  }

  get currentTotalElements(): number {
    return this.viewMode === 'reviews' ? this.totalElements : this.commentTotalElements;
  }
}
