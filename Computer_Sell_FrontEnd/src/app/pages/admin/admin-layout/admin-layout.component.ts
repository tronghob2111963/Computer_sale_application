import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-admin-layout',
  imports: [CommonModule, RouterLink, RouterOutlet, RouterLinkActive],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss'],
})
export class AdminLayoutComponent {
  sidebarOpen = true;
  submenuOpen = false;
  inventorySubmenuOpen = false;
  mobileSidebar = false;
  isMobile = false;

  constructor() {
    this.checkScreenSize();
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
