# Chatbot Frontend - Custom UI Guide

## 📱 Tính Năng UI

### ✨ Design Features
- ✅ Floating button với animation
- ✅ Unread message badge
- ✅ Minimize/Maximize window
- ✅ Suggested questions
- ✅ Real-time typing indicator
- ✅ Message timestamps
- ✅ Responsive design (mobile, tablet, desktop)
- ✅ Dark mode support
- ✅ Smooth animations

### 🎨 Color Scheme
- **Primary**: Gradient (Purple #667eea → #764ba2)
- **Background**: Light gray #f8f9fa
- **Text**: Dark gray #333
- **Borders**: Light gray #e9ecef
- **Accent**: Red #ff4757 (for unread badge)

## 📁 File Structure

```
Computer_Sell_FrontEnd/src/app/
├── components/
│   └── chatbot/
│       ├── chatbot.component.ts
│       ├── chatbot.component.html
│       └── chatbot.component.css
├── services/
│   └── chatbot.service.ts
└── pipes/
    └── sanitize-html.pipe.ts
```

## 🚀 Installation Steps

### Step 1: Copy Files

Copy các file sau vào project Angular của bạn:

```
src/app/components/chatbot/
├── chatbot.component.ts
├── chatbot.component.html
└── chatbot.component.css

src/app/services/
└── chatbot.service.ts

src/app/pipes/
└── sanitize-html.pipe.ts
```

### Step 2: Update App Module

Cập nhật `src/app/app.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { ChatbotComponent } from './components/chatbot/chatbot.component';
import { SanitizeHtmlPipe } from './pipes/sanitize-html.pipe';

@NgModule({
  declarations: [
    AppComponent,
    ChatbotComponent,
    SanitizeHtmlPipe
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

### Step 3: Add to App Component

Thêm vào `src/app/app.component.html`:

```html
<!-- Your existing content -->
<div class="app-container">
  <!-- Your pages/components here -->
</div>

<!-- Add Chatbot at the end -->
<app-chatbot></app-chatbot>
```

### Step 4: Configure API URL

Nếu backend chạy trên port khác, cập nhật `src/app/services/chatbot.service.ts`:

```typescript
private apiUrl = 'http://your-backend-url:8080/api/chatbot';
```

## 🎨 Customization

### Change Colors

Chỉnh sửa `chatbot.component.css`:

```css
/* Change primary gradient */
.chatbot-button {
  background: linear-gradient(135deg, #YOUR_COLOR_1 0%, #YOUR_COLOR_2 100%);
}

.send-btn {
  background: linear-gradient(135deg, #YOUR_COLOR_1 0%, #YOUR_COLOR_2 100%);
}

.chatbot-header {
  background: linear-gradient(135deg, #YOUR_COLOR_1 0%, #YOUR_COLOR_2 100%);
}
```

### Change Size

```css
/* Chatbot window size */
.chatbot-window {
  width: 500px;  /* Change width */
  height: 700px; /* Change height */
}

/* Floating button size */
.chatbot-button {
  width: 70px;   /* Change size */
  height: 70px;
}
```

### Change Position

```css
/* Change position */
.chatbot-widget {
  bottom: 30px;  /* Distance from bottom */
  right: 30px;   /* Distance from right */
  /* Or use: left: 30px; for left side */
}
```

### Change Messages

Chỉnh sửa `chatbot.component.ts`:

```typescript
suggestedQuestions: string[] = [
  'Your custom question 1?',
  'Your custom question 2?',
  'Your custom question 3?',
  'Your custom question 4?'
];

private initializeChat(): void {
  this.addBotMessage('Your custom welcome message here');
}
```

## 🔧 Advanced Customization

### Add Custom Styling

Tạo file `chatbot-custom.css` và import vào component:

```typescript
// In chatbot.component.ts
import './chatbot-custom.css';
```

### Customize Message Bubbles

Chỉnh sửa CSS cho message bubbles:

```css
.message-content {
  border-radius: 20px;  /* Change border radius */
  padding: 15px 20px;   /* Change padding */
  font-size: 15px;      /* Change font size */
}
```

### Add Custom Icons

Thay đổi emoji hoặc SVG icons trong HTML:

```html
<!-- Change bot icon -->
<div class="message-avatar">
  <span>🤖</span>  <!-- Change this emoji -->
</div>

<!-- Change user icon -->
<div class="message-avatar user">
  <span>👤</span>  <!-- Change this emoji -->
</div>
```

## 📱 Responsive Breakpoints

Component tự động responsive cho:
- **Desktop**: 420px width
- **Tablet**: 100vw - 20px (max 420px)
- **Mobile**: 100vw - 16px

Chỉnh sửa breakpoints trong CSS:

```css
@media (max-width: 768px) {
  .chatbot-window {
    width: calc(100vw - 20px);
    height: calc(100vh - 100px);
  }
}

@media (max-width: 480px) {
  .chatbot-window {
    width: calc(100vw - 16px);
    height: calc(100vh - 80px);
  }
}
```

## 🌙 Dark Mode

Component hỗ trợ dark mode tự động. Để test:

```css
@media (prefers-color-scheme: dark) {
  /* Dark mode styles */
}
```

Hoặc thêm class `.dark-mode` vào component:

```typescript
// In component
isDarkMode = false;

toggleDarkMode() {
  this.isDarkMode = !this.isDarkMode;
}
```

```html
<div class="chatbot-widget" [class.dark-mode]="isDarkMode">
  <!-- content -->
</div>
```

## 🎯 Features Explanation

### Floating Button
- Nổi ở góc phải dưới
- Có animation float
- Hiển thị unread badge
- Click để mở/đóng chat

### Chat Window
- Hiển thị messages
- Suggested questions
- Input area
- Minimize button

### Messages
- User messages: Gradient background, right aligned
- Bot messages: White background, left aligned
- Timestamps
- Typing indicator

### Suggested Questions
- Hiển thị khi chat mới bắt đầu
- Click để gửi câu hỏi
- Tự động ẩn khi có messages

## 🔌 Integration with Backend

### API Endpoints

Component gọi 3 endpoints:

1. **Chat**
```
POST /api/chatbot/chat?userId={userId}
```

2. **Product Availability**
```
GET /api/chatbot/product-availability?productName={productName}
```

3. **Product Price**
```
GET /api/chatbot/product-price?productName={productName}
```

### Error Handling

Component tự động xử lý lỗi:
- Network errors
- Timeout errors
- Server errors
- Validation errors

## 🧪 Testing

### Test Locally

1. Ensure backend is running:
```bash
mvn spring-boot:run
```

2. Run Angular app:
```bash
ng serve
```

3. Open browser:
```
http://localhost:4200
```

4. Click chatbot button and test

### Test Messages

```
User: "Laptop Dell XPS 13 bao nhiêu tiền?"
Bot: "Giá của Laptop Dell XPS 13 là 25,000,000 VND"

User: "Laptop Dell XPS 13 còn hàng không?"
Bot: "Sản phẩm 'Laptop Dell XPS 13' hiện có sẵn với 5 sản phẩm trong kho."
```

## 📊 Performance Tips

1. **Lazy Load Component**
```typescript
// In routing module
{
  path: 'chatbot',
  loadChildren: () => import('./components/chatbot/chatbot.module')
    .then(m => m.ChatbotModule)
}
```

2. **Optimize Messages**
- Limit message history
- Paginate old messages
- Clear messages on close

3. **Debounce Input**
```typescript
import { debounceTime } from 'rxjs/operators';

// In component
inputMessage$ = new Subject<string>();

ngOnInit() {
  this.inputMessage$.pipe(
    debounceTime(300)
  ).subscribe(msg => this.sendMessage());
}
```

## 🔐 Security

1. **Sanitize HTML**
- Component uses `sanitizeHtml` pipe
- Prevents XSS attacks

2. **Validate Input**
- Check message length
- Validate user ID
- Escape special characters

3. **CORS Configuration**
- Backend has CORS enabled
- Frontend can call API

## 📚 Additional Resources

- [Angular Documentation](https://angular.io/docs)
- [RxJS Documentation](https://rxjs.dev/)
- [CSS Grid Guide](https://css-tricks.com/snippets/css/complete-guide-grid/)
- [Responsive Design](https://developer.mozilla.org/en-US/docs/Learn/CSS/CSS_layout/Responsive_Design)

## 🐛 Troubleshooting

### Chatbot not showing
- Check if component is added to app.module.ts
- Check if component is added to app.component.html
- Check browser console for errors

### Messages not sending
- Check if backend is running
- Check if API URL is correct
- Check browser network tab
- Check backend logs

### Styling issues
- Clear browser cache
- Check CSS file is loaded
- Check for CSS conflicts
- Use browser DevTools

### CORS errors
- Check backend CORS configuration
- Check API URL matches backend
- Check request headers

## 📞 Support

For issues or questions:
1. Check CHATBOT_TROUBLESHOOTING.md
2. Check browser console
3. Check backend logs
4. Check network requests

## ✅ Checklist

- [ ] Copy all files to project
- [ ] Update app.module.ts
- [ ] Add component to app.component.html
- [ ] Configure API URL
- [ ] Test locally
- [ ] Customize colors/text
- [ ] Test on mobile
- [ ] Deploy to production

---

**Ready to integrate? Start with Step 1: Copy Files**
