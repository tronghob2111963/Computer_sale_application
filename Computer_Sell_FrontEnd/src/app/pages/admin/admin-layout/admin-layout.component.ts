import { Component, HostListener, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationDropdownComponent } from '../../../shared/notification-dropdown/notification-dropdown.component';
import { NotificationService } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  standalone: true,
  selector: 'app-admin-layout',
  imports: [CommonModule, RouterLink, RouterOutlet, RouterLinkActive, NotificationDropdownComponent],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss'],
})
export class AdminLayoutComponent implements OnInit, OnDestroy {
  sidebarOpen = true;
  submenuOpen = false;
  inventorySubmenuOpen = false;
  mobileSidebar = false;
  isMobile = false;
  unreadCount = 0;

  private subscriptions: Subscription[] = [];

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {
    this.checkScreenSize();
  }

  ngOnInit(): void {
    const userId = this.authService.getUserId();
    if (userId) {
      // Subscribe to unread count
      this.subscriptions.push(
        this.notificationService.unreadCount$.subscribe(count => {
          this.unreadCount = count;
        })
      );

      // Start polling
      this.subscriptions.push(
        this.notificationService.startPolling(userId).subscribe()
      );
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  toggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }

  toggleSubmenu() {
    this.submenuOpen = !this.submenuOpen;
  }

  toggleInventorySubmenu() {
    this.inventorySubmenuOpen = !this.inventorySubmenuOpen;
  }

  toggleMobileSidebar() {
    this.mobileSidebar = !this.mobileSidebar;
  }

  @HostListener('window:resize', [])
  checkScreenSize() {
    this.isMobile = window.innerWidth < 768;
    if (this.isMobile) {
      this.sidebarOpen = true;
    }
  }
}
