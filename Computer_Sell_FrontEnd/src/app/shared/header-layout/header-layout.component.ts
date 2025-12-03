import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { AuthStateService } from '../../services/auth-state.service';
import { Subscription } from 'rxjs';
import { CategoryService, CategoryDTO } from '../../services/category.service';

@Component({
  selector: 'app-header-layout',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header-layout.component.html',
  styleUrls: ['./header-layout.component.scss']
})
export class HeaderLayoutComponent implements OnInit, OnDestroy {
  username = '';
  isLoggedIn = false;
  cartCount = 0;
  private sub?: Subscription;
  private cartSub?: Subscription;
  // categories bar state
  categories: CategoryDTO[] = [];
  isCategoryOpen = false;
  isAdminRoute = false;

  constructor(
    private authService: AuthService,
    private authState: AuthStateService,
    private cartService: CartService,
    private categoryService: CategoryService,
    public router: Router,

  ) { }
  goToLogin(): void {
    this.router.navigate(['/login']);
  }
  ngOnInit(): void {
    // Initial check
    this.updateAuthState();

    // preload categories for dropdown
    this.loadCategories();

    // detect admin pages to hide bar
    const checkRoute = () => this.isAdminRoute = this.router.url.startsWith('/admin');
    checkRoute();
    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd) {
        checkRoute();
        // Re-check auth state on navigation
        this.updateAuthState();
      }
    });

    // Subscribe to auth state changes
    this.sub = this.authState.username$.subscribe(name => {
      console.log('Auth state changed - username:', name); // Debug log
      this.username = name || this.authService.getUsername();
      this.isLoggedIn = !!this.username;
      if (this.isLoggedIn) {
        this.loadCartCount();
      }
    });

    // auto-refresh badge when cart changes anywhere
    this.cartSub = this.cartService.cartUpdated$.subscribe(() => this.loadCartCount());
  }

  private updateAuthState(): void {
    this.username = this.authService.getUsername();
    this.isLoggedIn = !!this.username;
    console.log('Updated auth state - username:', this.username, 'isLoggedIn:', this.isLoggedIn); // Debug log
    if (this.isLoggedIn) {
      this.loadCartCount();
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.cartSub?.unsubscribe();
  }

  private loadCartCount(): void {
    const uid = this.authService.getUserId();
    if (!uid) { this.cartCount = 0; return; }
    this.cartService.viewCart(uid).subscribe({
      next: (c) => this.cartCount = c.items.reduce((s, i) => s + i.quantity, 0),
      error: () => this.cartCount = 0
    });
  }

  // Category dropdown helpers
  toggleCategory(): void { this.isCategoryOpen = !this.isCategoryOpen; }
  closeCategory(): void { this.isCategoryOpen = false; }
  selectCategory(cat: CategoryDTO): void {
    this.isCategoryOpen = false;
    this.router.navigate(['/'], { queryParams: { categoryId: cat.id, categoryName: cat.name } });
  }

  private loadCategories(): void {
    this.categoryService.listCategories({ page: 0, size: 50, sortBy: 'name:asc' }).subscribe({
      next: (res) => this.categories = res?.data?.items ?? [],
      error: () => this.categories = []
    });
  }


}
