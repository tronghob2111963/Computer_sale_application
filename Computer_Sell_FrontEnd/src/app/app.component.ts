import { Component, ViewChild, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HeaderLayoutComponent } from './shared/header-layout/header-layout.component';
import { BuildPcFabComponent } from './shared/build-pc-fab/build-pc-fab.component';
import { ChatbotComponent } from './components/chatbot/chatbot.component';
import { AuthService } from './services/auth.service';
import { AuthStateService } from './services/auth-state.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, HeaderLayoutComponent, BuildPcFabComponent, ChatbotComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  @ViewChild(ChatbotComponent) chatbotComponent!: ChatbotComponent;
  title = 'Computer_Sell_FrontEnd';
  isLoggedIn = false;
  isAdminRoute = false;

  constructor(
    private authService: AuthService,
    private authState: AuthStateService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Initial check
    this.isLoggedIn = !!this.authService.getUsername();
    this.checkAdminRoute();

    // Subscribe to auth state changes
    this.authState.username$.subscribe(name => {
      this.isLoggedIn = !!name || !!this.authService.getUsername();
    });

    // Subscribe to route changes to detect admin pages
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.checkAdminRoute();
      }
    });
  }

  private checkAdminRoute(): void {
    this.isAdminRoute = this.router.url.startsWith('/admin');
  }

  ngAfterViewInit(): void {
    // Set user ID from auth service if available
    // Example: this.chatbotComponent.setUserId(currentUserId);
  }
}
