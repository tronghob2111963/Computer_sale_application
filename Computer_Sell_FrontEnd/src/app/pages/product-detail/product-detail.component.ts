import { CommonModule } from '@angular/common';
import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService, ResponseEnvelope } from '../../services/product.service';
import { environment } from '../../enviroment';
import { buildImageUrl } from '../../utils/image.util';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { ReviewResponse, ReviewService, ReviewSummary } from '../../services/review.service';
import { CommentResponse, CommentService } from '../../services/comment.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {

  /* ======================= BASE STATE ======================= */
  loading = false;
  error: string | null = null;
  product: any = null;
  qty = 1;

  /* ======================= REVIEWS ======================= */
  reviews: ReviewResponse[] = [];
  reviewSummary: ReviewSummary | null = null;
  reviewError: string | null = null;
  submittingReview = false;
  newReview = { rating: 5, comment: '' };

  /* ⭐ Load More Reviews */
  visibleReviews = 5;
  loadMoreReviews() {
    this.visibleReviews += 5;
  }

  /* ======================= COMMENTS ======================= */
  comments: CommentResponse[] = [];
  commentError: string | null = null;
  submittingComment = false;
  newComment = { content: '' };
  replyingTo: string | null = null;
  replyInputs: Record<string, string> = {};

  /* ⭐ Load More Comments */
  visibleComments = 5;
  loadMoreComments() {
    this.visibleComments += 5;
  }

  /* ======================= RELATED PRODUCTS ======================= */
  similarProducts: any[] = [];
  similarLoading = false;
  similarError: string | null = null;
  @ViewChild('similarTrack') similarTrack?: ElementRef<HTMLDivElement>;

  /* ======================= GALLERY STATE ======================= */
  activeImageIndex = 0;
  lightboxOpen = false;
  imageSwitching = false;
  private imageSwitchTimer: any;
  // ⭐ Hướng animation: next / prev
  imageAnimationDirection: 'next' | 'prev' | null = null;

  /* ======================= OTHER ======================= */
  private userRole = '';
  private readonly baseUrl = environment.apiUrl || 'http://localhost:8080';
  private routeId = '';
  readonly ratingLabels: Record<number, string> = {
    1: 'Tệ',
    2: 'Không hài lòng',
    3: 'Bình thường',
    4: 'Tốt',
    5: 'Xuất sắc'
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private auth: AuthService,
    private reviewService: ReviewService,
    private commentService: CommentService
  ) { }

  ngOnInit(): void {
    this.userRole = this.auth.getRole?.() || '';

    // Subscribe to route params to reload when navigating to different product
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id && id !== this.routeId) {
        this.routeId = id;
        this.fetch(id);
        // Scroll to top when navigating to new product
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else if (id && !this.routeId) {
        this.routeId = id;
        this.fetch(id);
      }
    });
  }

  /* ======================= PRODUCT LOAD ======================= */
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
          stock: p?.stock ?? p?.quantity ?? undefined
        };
        this.loading = false;
        this.activeImageIndex = 0;
        this.triggerImageSwitch(null);

        /* Load review + comments + similar */
        this.loadReviews();
        this.loadComments();
        this.loadSimilarProducts();
      },
      error: () => {
        this.error = 'Khong tai duoc chi tiet san pham';
        this.loading = false;
      }
    });
  }

  /* ======================= CART ======================= */
  addToCart() {
    const userId = this.auth.getUserId?.() || '';
    if (!userId) {
      alert('Vui long dang nhap de mua hang');
      return;
    }
    const pid = this.product?.id ?? this.product?.productId ?? this.routeId;
    if (!pid) {
      alert('Khong xac dinh duoc san pham');
      return;
    }
    this.cartService.addToCart(userId, pid, this.qty).subscribe({
      next: () => alert('Da them vao gio hang'),
      error: () => alert('Them vao gio hang that bai')
    });
  }

  buyNow() {
    const userId = this.auth.getUserId?.() || '';
    if (!userId) {
      alert('Vui long dang nhap de mua hang');
      return;
    }
    const pid = this.product?.id ?? this.product?.productId ?? this.routeId;
    if (!pid) {
      alert('Khong xac dinh duoc san pham');
      return;
    }
    this.cartService.addToCart(userId, pid, this.qty).subscribe({
      next: () => this.router.navigate(['/cart']),
      error: () => alert('Them vao gio hang that bai')
    });
  }

  /* ======================= IMAGE SELECT ======================= */
  selectImage(img: string) {
    if (!this.product?.images) return;
    const idx = this.product.images.findIndex((i: string) => i === img);
    this.setActiveIndex(idx >= 0 ? idx : 0, null);
  }

  selectImageByIndex(idx: number) {
    if (idx === this.activeImageIndex) return;

    let direction: 'next' | 'prev' | null = null;
    if (idx > this.activeImageIndex) {
      direction = 'next';
    } else if (idx < this.activeImageIndex) {
      direction = 'prev';
    }

    this.setActiveIndex(idx, direction);
  }

  get currentImage(): string {
    return this.product?.images?.[this.activeImageIndex] || '/placeholder/product.png';
  }

  prevImage() {
    this.setActiveIndex(this.activeImageIndex - 1, 'prev');
  }

  nextImage() {
    this.setActiveIndex(this.activeImageIndex + 1, 'next');
  }

  openLightbox(idx: number) {
    this.setActiveIndex(idx, null);
    this.lightboxOpen = true;
  }

  closeLightbox() {
    this.lightboxOpen = false;
  }

  private setActiveIndex(idx: number, direction: 'next' | 'prev' | null = null) {
    if (!this.product?.images?.length) return;

    // Nếu đang animate thì bỏ qua để tránh spam click
    if (this.imageSwitching) return;

    const len = this.product.images.length;
    const next = ((idx % len) + len) % len;

    if (next === this.activeImageIndex) return;

    this.activeImageIndex = next;
    this.triggerImageSwitch(direction);
  }

  private triggerImageSwitch(direction: 'next' | 'prev' | null = null) {
    this.imageAnimationDirection = direction;
    this.imageSwitching = true;

    clearTimeout(this.imageSwitchTimer);
    this.imageSwitchTimer = setTimeout(() => {
      this.imageSwitching = false;
      this.imageAnimationDirection = null;
    }, 260); // khớp với thời gian animation CSS
  }

  decQty() {
    if (this.qty > 1) this.qty -= 1;
  }

  incQty() {
    this.qty += 1;
  }

  /* ======================= REVIEWS ======================= */
  loadReviews() {
    const pid = this.product?.id ?? this.routeId;
    if (!pid) return;

    this.reviewService.getProductReviews(pid).subscribe({
      next: (res) => {
        const data = res?.data;
        this.reviews = data?.reviews || [];
        this.reviewSummary = data?.summary || null;
        this.reviewError = null;
      },
      error: () => {
        this.reviewError = 'Khong tai duoc danh gia.';
      }
    });
  }

  submitReview() {
    const userId = this.auth.getUserId?.() || '';
    if (!userId) {
      alert('Vui long dang nhap de gui danh gia');
      return;
    }

    const pid = this.product?.id ?? this.routeId;
    if (!pid) return;

    if (!this.newReview.comment?.trim()) {
      alert('Vui long nhap nhan xet.');
      return;
    }

    this.submittingReview = true;

    this.reviewService.submitReview({
      productId: pid,
      rating: Number(this.newReview.rating),
      comment: this.newReview.comment.trim()
    }).subscribe({
      next: () => {
        this.newReview = { rating: 5, comment: '' };
        this.loadReviews();
        alert('Da gui danh gia. Cam on ban!');
        this.submittingReview = false;
      },
      error: () => {
        this.submittingReview = false;
        alert('Gui danh gia that bai.');
      }
    });
  }

  /* ======================= COMMENTS ======================= */
  loadComments() {
    const pid = this.product?.id ?? this.routeId;
    if (!pid) return;

    this.commentService.getProductComments(pid).subscribe({
      next: (res) => {
        const data = res?.data ?? [];
        this.comments = Array.isArray(data) ? data : [];
        this.commentError = null;
      },
      error: () => {
        this.commentError = 'Khong tai duoc binh luan.';
        this.comments = [];
      }
    });
  }

  submitComment() {
    const userId = this.auth.getUserId?.() || '';
    if (!userId) {
      alert('Vui long dang nhap de binh luan');
      return;
    }

    const pid = this.product?.id ?? this.routeId;
    if (!pid) return;

    if (!this.newComment.content?.trim()) {
      alert('Vui long nhap noi dung.');
      return;
    }

    this.submittingComment = true;

    this.commentService.addComment({
      productId: pid,
      content: this.newComment.content.trim()
    }).subscribe({
      next: () => {
        this.newComment = { content: '' };
        this.submittingComment = false;
        this.loadComments();
      },
      error: () => {
        this.submittingComment = false;
        alert('Gui binh luan that bai.');
      }
    });
  }

  startReply(commentId: string) {
    if (!this.canReplyAsStaff) return;
    this.replyingTo = commentId;
    this.replyInputs[commentId] = this.replyInputs[commentId] || '';
  }

  cancelReply() {
    this.replyingTo = null;
  }

  submitReply(parentId: string) {
    const userId = this.auth.getUserId?.() || '';
    if (!userId) {
      alert('Vui long dang nhap de tra loi');
      return;
    }

    const pid = this.product?.id ?? this.routeId;
    const content = (this.replyInputs[parentId] || '').trim();

    if (!pid || !content) {
      alert('Nhap noi dung tra loi.');
      return;
    }

    this.submittingComment = true;

    this.commentService.addComment({
      productId: pid,
      parentId,
      content
    }).subscribe({
      next: () => {
        this.submittingComment = false;
        this.replyInputs[parentId] = '';
        this.replyingTo = null;
        this.loadComments();
      },
      error: () => {
        this.submittingComment = false;
        alert('Gui tra loi that bai.');
      }
    });
  }

  /* ======================= ACCESS HELPERS ======================= */
  get canReplyAsStaff(): boolean {
    const role = (this.auth.getRole?.() || this.userRole || '').toUpperCase();
    const staffRoles = ['ADMIN', 'SYSADMIN', 'SYS_ADMIN', 'STAFF', 'ROLE_ADMIN', 'ROLE_SYSADMIN', 'ROLE_STAFF'];
    return staffRoles.some(r => role.includes(r));
  }

  get inStockLabel(): string {
    const s = Number(this.product?.stock ?? 0);
    return s > 0 ? 'Con hang' : 'Het hang';
  }

  get discountPercent(): number | null {
    const price = Number(this.product?.price ?? 0);
    const orig = Number(this.product?.originalPrice ?? 0);
    if (!orig || !price || orig <= price) return null;
    return Math.round(((orig - price) / orig) * 100);
  }

  get descriptionLines(): string[] {
    const raw = (this.product?.description || '').trim();
    if (!raw) return [];

    // Normalize whitespace/newlines then split by sentence delimiters; fallback to comma split if still single chunk.
    const normalized = raw.replace(/\s+/g, ' ');
    let parts = normalized.split(/(?<=[.;])\s+/).map((p: string) => p.trim()).filter(Boolean);
    if (parts.length <= 1) {
      parts = normalized.split(/\s*,\s*/).map((p: string) => p.trim()).filter(Boolean);
    }
    return parts.slice(0, 30); // safety limit
  }

  starCount(star: number): number {
    if (!this.reviewSummary) return 0;
    return {
      5: this.reviewSummary.fiveStar,
      4: this.reviewSummary.fourStar,
      3: this.reviewSummary.threeStar,
      2: this.reviewSummary.twoStar,
      1: this.reviewSummary.oneStar
    }[star] || 0;
  }

  ratingPercent(star: number): number {
    const total = this.reviewSummary?.totalReviews || 0;
    if (!total) return 0;
    return Math.round((this.starCount(star) / total) * 100);
  }

  /* ======================= RELATED LOAD ======================= */
  private loadSimilarProducts() {
    const p: any = this.product || {};
    const pid = p?.id ?? p?.productId ?? this.routeId;

    const typeId =
      p?.productTypeId ||
      p?.productType?.id ||
      p?.productType?.productTypeId ||
      p?.productType?.typeId ||
      p?.productTypeID;

    const categoryId =
      p?.categoryId ||
      p?.category?.id ||
      p?.category?.categoryId ||
      p?.categoryID;

    const brandId =
      p?.brandId ||
      p?.brand?.id ||
      p?.brand?.brandId ||
      p?.brandID;

    let source$;
    if (typeId) {
      source$ = this.productService.listProductsByProductType<any>(typeId, { page: 0, size: 8, sortBy: 'createdAt:desc' });
    } else if (categoryId) {
      source$ = this.productService.listProductsByCategory<any>(categoryId, { page: 0, size: 8, sortBy: 'createdAt:desc' });
    } else if (brandId) {
      source$ = this.productService.listProductsByBrand<any>(brandId, { page: 0, size: 8, sortBy: 'createdAt:desc' });
    } else {
      source$ = this.productService.listProducts<any>({ page: 0, size: 8, sortBy: 'createdAt:desc' });
    }

    this.similarLoading = true;
    this.similarError = null;

    source$.subscribe({
      next: (res) => {
        const items = res?.data?.items ?? res?.data ?? [];
        const list = Array.isArray(items) ? items : [];
        this.similarProducts = list
          .filter((item: any) => (item?.id ?? item?.productId) !== pid)
          .map((item: any) => ({
            ...item,
            id: item?.id ?? item?.productId,
            name: item?.name,
            price: item?.price,
            displayImage: buildImageUrl(
              this.baseUrl,
              Array.isArray(item?.image) && item.image.length ? item.image[0] : undefined
            )
          }));
        this.similarLoading = false;
      },
      error: () => {
        this.similarError = 'Khong tai duoc san pham tuong tu.';
        this.similarLoading = false;
        this.similarProducts = [];
      }
    });
  }

  scrollSimilar(direction: number) {
    const el = this.similarTrack?.nativeElement;
    if (!el) return;
    const step = Math.max(el.clientWidth * 0.7, 240);
    el.scrollBy({ left: direction * step, behavior: 'smooth' });
  }
}
