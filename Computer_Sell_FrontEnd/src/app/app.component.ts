import { Component, ViewChild } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderLayoutComponent } from './shared/header-layout/header-layout.component';
import { BuildPcFabComponent } from './shared/build-pc-fab/build-pc-fab.component';
import { ChatbotComponent } from './components/chatbot/chatbot.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderLayoutComponent, BuildPcFabComponent, ChatbotComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  @ViewChild(ChatbotComponent) chatbotComponent!: ChatbotComponent;
  title = 'Computer_Sell_FrontEnd';

  ngAfterViewInit(): void {
    // Set user ID from auth service if available
    // Example: this.chatbotComponent.setUserId(currentUserId);
  }
}
