# Tích Hợp Chatbot vào Frontend Angular

## Tạo Chatbot Service

Tạo file `src/app/services/chatbot.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  message: string;
}

export interface ChatResponse {
  message: string;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private apiUrl = 'http://localhost:8080/api/chatbot';

  constructor(private http: HttpClient) { }

  sendMessage(message: string, userId: number): Observable<ChatResponse> {
    const payload: ChatMessage = { message };
    return this.http.post<ChatResponse>(
      `${this.apiUrl}/chat?userId=${userId}`,
      payload
    );
  }

  getProductAvailability(productName: string): Observable<string> {
    return this.http.get<string>(
      `${this.apiUrl}/product-availability?productName=${encodeURIComponent(productName)}`
    );
  }

  getProductPrice(productName: string): Observable<string> {
    return this.http.get<string>(
      `${this.apiUrl}/product-price?productName=${encodeURIComponent(productName)}`
    );
  }
}
```

## Tạo Chatbot Component

Tạo file `src/app/components/chatbot/chatbot.component.ts`:

```typescript
import { Component, OnInit } from '@angular/core';
import { ChatbotService, ChatResponse } from '../../services/chatbot.service';

interface Message {
  text: string;
  sender: 'user' | 'bot';
  timestamp: Date;
}

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements OnInit {
  messages: Message[] = [];
  inputMessage: string = '';
  isLoading: boolean = false;
  userId: number = 1; // Lấy từ auth service

  constructor(private chatbotService: ChatbotService) { }

  ngOnInit(): void {
    this.addBotMessage('Xin chào! Tôi là trợ lý bán hàng. Bạn cần giúp gì?');
  }

  sendMessage(): void {
    if (!this.inputMessage.trim()) return;

    // Thêm tin nhắn của user
    this.addUserMessage(this.inputMessage);
    const userInput = this.inputMessage;
    this.inputMessage = '';
    this.isLoading = true;

    // Gửi đến chatbot
    this.chatbotService.sendMessage(userInput, this.userId).subscribe({
      next: (response: ChatResponse) => {
        this.addBotMessage(response.message);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error:', error);
        this.addBotMessage('Xin lỗi, tôi gặp lỗi. Vui lòng thử lại sau.');
        this.isLoading = false;
      }
    });
  }

  private addUserMessage(text: string): void {
    this.messages.push({
      text,
      sender: 'user',
      timestamp: new Date()
    });
  }

  private addBotMessage(text: string): void {
    this.messages.push({
      text,
      sender: 'bot',
      timestamp: new Date()
    });
  }

  scrollToBottom(): void {
    setTimeout(() => {
      const chatContainer = document.querySelector('.chat-messages');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    }, 0);
  }
}
```

## Template HTML

Tạo file `src/app/components/chatbot/chatbot.component.html`:

```html
<div class="chatbot-container">
  <div class="chat-header">
    <h2>Trợ Lý Bán Hàng</h2>
    <p class="subtitle">Hỏi tôi về sản phẩm, giá cả, hoặc tính khả dụng</p>
  </div>

  <div class="chat-messages" #chatMessages>
    <div *ngFor="let msg of messages" [ngClass]="'message-' + msg.sender">
      <div class="message-content">
        <p>{{ msg.text }}</p>
        <span class="timestamp">{{ msg.timestamp | date:'HH:mm' }}</span>
      </div>
    </div>
    <div *ngIf="isLoading" class="message-bot">
      <div class="message-content">
        <p class="typing">Đang gõ...</p>
      </div>
    </div>
  </div>

  <div class="chat-input-area">
    <div class="input-group">
      <input
        type="text"
        [(ngModel)]="inputMessage"
        (keyup.enter)="sendMessage()"
        placeholder="Nhập câu hỏi của bạn..."
        class="chat-input"
        [disabled]="isLoading"
      />
      <button
        (click)="sendMessage()"
        [disabled]="!inputMessage.trim() || isLoading"
        class="send-btn"
      >
        <span *ngIf="!isLoading">Gửi</span>
        <span *ngIf="isLoading">...</span>
      </button>
    </div>
  </div>
</div>
```

## CSS Styling

Tạo file `src/app/components/chatbot/chatbot.component.css`:

```css
.chatbot-container {
  display: flex;
  flex-direction: column;
  height: 600px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chat-header {
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px 8px 0 0;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
}

.chat-header .subtitle {
  margin: 4px 0 0 0;
  font-size: 12px;
  opacity: 0.9;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-user,
.message-bot {
  display: flex;
  margin-bottom: 8px;
}

.message-user {
  justify-content: flex-end;
}

.message-bot {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 8px;
  word-wrap: break-word;
}

.message-user .message-content {
  background: #667eea;
  color: white;
  border-radius: 18px 18px 4px 18px;
}

.message-bot .message-content {
  background: #f0f0f0;
  color: #333;
  border-radius: 18px 18px 18px 4px;
}

.message-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
}

.timestamp {
  display: block;
  font-size: 11px;
  opacity: 0.7;
  margin-top: 4px;
}

.typing {
  animation: typing 1.4s infinite;
}

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.5;
  }
  30% {
    opacity: 1;
  }
}

.chat-input-area {
  padding: 12px;
  border-top: 1px solid #eee;
  background: #fafafa;
  border-radius: 0 0 8px 8px;
}

.input-group {
  display: flex;
  gap: 8px;
}

.chat-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}

.chat-input:focus {
  border-color: #667eea;
}

.chat-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.send-btn {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.3s;
}

.send-btn:hover:not(:disabled) {
  background: #764ba2;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
```

## Thêm vào Module

Cập nhật `app.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { ChatbotComponent } from './components/chatbot/chatbot.component';

@NgModule({
  declarations: [
    AppComponent,
    ChatbotComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

## Sử Dụng Component

Thêm vào template của bạn:

```html
<app-chatbot></app-chatbot>
```

## Ví Dụ Câu Hỏi

Người dùng có thể hỏi:
- "Laptop Dell XPS 13 còn hàng không?"
- "Giá của MacBook Pro bao nhiêu?"
- "Bạn có sản phẩm nào dưới 10 triệu không?"
- "Tôi muốn mua một chiếc laptop gaming"
- "Có khuyến mãi nào không?"

## CORS Configuration

Nếu gặp lỗi CORS, thêm vào backend:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173", "http://localhost:4200")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

## Tối Ưu Hóa

1. **Lazy Loading**: Load chatbot component khi cần
2. **Message Pagination**: Phân trang tin nhắn cũ
3. **Debouncing**: Debounce input để tránh gửi quá nhiều request
4. **Caching**: Cache các câu trả lời phổ biến
