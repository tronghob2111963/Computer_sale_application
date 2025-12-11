# Chatbot Module Setup Guide

## 📦 Complete Module Setup

### Option 1: Add to Existing App Module (Simple)

#### Step 1: Update `app.module.ts`

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

#### Step 2: Update `app.component.html`

```html
<!-- Your existing content -->
<div class="app-container">
  <!-- Your pages/components here -->
  <router-outlet></router-outlet>
</div>

<!-- Add Chatbot Widget -->
<app-chatbot></app-chatbot>
```

#### Step 3: Update `app.component.css` (Optional)

```css
.app-container {
  min-height: 100vh;
  padding-bottom: 20px;
}
```

---

### Option 2: Create Separate Chatbot Module (Recommended)

#### Step 1: Create Chatbot Module

Tạo file `src/app/modules/chatbot/chatbot.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { ChatbotComponent } from '../../components/chatbot/chatbot.component';
import { SanitizeHtmlPipe } from '../../pipes/sanitize-html.pipe';
import { ChatbotService } from '../../services/chatbot.service';

@NgModule({
  declarations: [
    ChatbotComponent,
    SanitizeHtmlPipe
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [ChatbotService],
  exports: [ChatbotComponent]
})
export class ChatbotModule { }
```

#### Step 2: Update `app.module.ts`

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { ChatbotModule } from './modules/chatbot/chatbot.module';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    ChatbotModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

#### Step 3: Update `app.component.html`

```html
<!-- Your existing content -->
<div class="app-container">
  <router-outlet></router-outlet>
</div>

<!-- Add Chatbot Widget -->
<app-chatbot></app-chatbot>
```

---

### Option 3: Lazy Load Chatbot Module (Advanced)

#### Step 1: Create Chatbot Module

Tạo `src/app/modules/chatbot/chatbot.module.ts` (như Option 2)

#### Step 2: Create Chatbot Routing

Tạo `src/app/modules/chatbot/chatbot-routing.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChatbotComponent } from '../../components/chatbot/chatbot.component';

const routes: Routes = [
  {
    path: '',
    component: ChatbotComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ChatbotRoutingModule { }
```

#### Step 3: Update Chatbot Module

```typescript
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { ChatbotComponent } from '../../components/chatbot/chatbot.component';
import { SanitizeHtmlPipe } from '../../pipes/sanitize-html.pipe';
import { ChatbotService } from '../../services/chatbot.service';
import { ChatbotRoutingModule } from './chatbot-routing.module';

@NgModule({
  declarations: [
    ChatbotComponent,
    SanitizeHtmlPipe
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    FormsModule,
    ChatbotRoutingModule
  ],
  providers: [ChatbotService],
  exports: [ChatbotComponent]
})
export class ChatbotModule { }
```

#### Step 4: Update App Routing

Cập nhật `src/app/app-routing.module.ts`:

```typescript
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'chatbot',
    loadChildren: () => import('./modules/chatbot/chatbot.module')
      .then(m => m.ChatbotModule)
  },
  // Other routes...
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
```

---

## 🔧 Configuration

### Update API URL

Chỉnh sửa `src/app/services/chatbot.service.ts`:

```typescript
export class ChatbotService {
  // For local development
  private apiUrl = 'http://localhost:8080/api/chatbot';
  
  // For production
  // private apiUrl = 'https://your-domain.com/api/chatbot';
  
  // Or use environment variables
  // private apiUrl = environment.apiUrl + '/chatbot';
}
```

### Use Environment Variables

Tạo `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

Tạo `src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-domain.com/api'
};
```

Cập nhật service:

```typescript
import { environment } from '../../../environments/environment';

export class ChatbotService {
  private apiUrl = environment.apiUrl + '/chatbot';
}
```

---

## 📁 File Structure

### Option 1 (Simple)
```
src/app/
├── components/
│   └── chatbot/
│       ├── chatbot.component.ts
│       ├── chatbot.component.html
│       └── chatbot.component.css
├── services/
│   └── chatbot.service.ts
├── pipes/
│   └── sanitize-html.pipe.ts
├── app.component.ts
├── app.component.html
├── app.module.ts
└── app.component.css
```

### Option 2 (Recommended)
```
src/app/
├── components/
│   └── chatbot/
│       ├── chatbot.component.ts
│       ├── chatbot.component.html
│       └── chatbot.component.css
├── services/
│   └── chatbot.service.ts
├── pipes/
│   └── sanitize-html.pipe.ts
├── modules/
│   └── chatbot/
│       └── chatbot.module.ts
├── app.component.ts
├── app.component.html
├── app.module.ts
└── app.component.css
```

### Option 3 (Advanced)
```
src/app/
├── components/
│   └── chatbot/
│       ├── chatbot.component.ts
│       ├── chatbot.component.html
│       └── chatbot.component.css
├── services/
│   └── chatbot.service.ts
├── pipes/
│   └── sanitize-html.pipe.ts
├── modules/
│   └── chatbot/
│       ├── chatbot.module.ts
│       └── chatbot-routing.module.ts
├── app-routing.module.ts
├── app.component.ts
├── app.component.html
├── app.module.ts
└── app.component.css
```

---

## 🚀 Installation Steps

### Step 1: Copy Files

```bash
# Copy component files
cp chatbot.component.* src/app/components/chatbot/

# Copy service
cp chatbot.service.ts src/app/services/

# Copy pipe
cp sanitize-html.pipe.ts src/app/pipes/
```

### Step 2: Choose Setup Option

- **Option 1**: Simple (add to app.module.ts)
- **Option 2**: Recommended (create chatbot.module.ts)
- **Option 3**: Advanced (lazy load)

### Step 3: Update Files

Follow the steps for your chosen option

### Step 4: Update API URL

Configure API URL in `chatbot.service.ts`

### Step 5: Test

```bash
ng serve
```

Open browser and test chatbot

---

## 🧪 Testing

### Test Component

Tạo `src/app/components/chatbot/chatbot.component.spec.ts`:

```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';

import { ChatbotComponent } from './chatbot.component';
import { SanitizeHtmlPipe } from '../../pipes/sanitize-html.pipe';

describe('ChatbotComponent', () => {
  let component: ChatbotComponent;
  let fixture: ComponentFixture<ChatbotComponent>;

  beforeEach(async () {
    await TestBed.configureTestingModule({
      declarations: [ ChatbotComponent, SanitizeHtmlPipe ],
      imports: [ HttpClientTestingModule, FormsModule ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChatbotComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle chat', () => {
    expect(component.isOpen).toBeFalsy();
    component.toggleChat();
    expect(component.isOpen).toBeTruthy();
  });

  it('should send message', () => {
    component.inputMessage = 'Test message';
    component.sendMessage();
    expect(component.messages.length).toBeGreaterThan(0);
  });
});
```

### Test Service

Tạo `src/app/services/chatbot.service.spec.ts`:

```typescript
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ChatbotService } from './chatbot.service';

describe('ChatbotService', () => {
  let service: ChatbotService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      providers: [ ChatbotService ]
    });
    service = TestBed.inject(ChatbotService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should send message', () => {
    const mockResponse = { message: 'Hello', timestamp: new Date().toISOString() };
    
    service.sendMessage('Test', 1).subscribe(response => {
      expect(response.message).toBe('Hello');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/chatbot/chat?userId=1');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });
});
```

---

## 🔐 Security

### CORS Configuration

Backend already has CORS configured. If you get CORS errors:

1. Check backend CORS configuration
2. Check API URL is correct
3. Check request headers

### Input Validation

Component validates:
- Message not empty
- Message length
- User ID exists

### XSS Protection

Component uses `sanitizeHtml` pipe to prevent XSS attacks

---

## 📊 Performance

### Optimization Tips

1. **Lazy Load Module**
   - Use Option 3 for lazy loading
   - Reduces initial bundle size

2. **OnPush Change Detection**
   ```typescript
   @Component({
     changeDetection: ChangeDetectionStrategy.OnPush
   })
   ```

3. **Unsubscribe from Observables**
   ```typescript
   ngOnDestroy() {
     this.subscription.unsubscribe();
   }
   ```

4. **Limit Message History**
   ```typescript
   if (this.messages.length > 100) {
     this.messages = this.messages.slice(-100);
   }
   ```

---

## 🐛 Troubleshooting

### Module not found
- Check file paths
- Check imports
- Check exports

### Component not rendering
- Check if component is declared
- Check if component is imported
- Check if component is added to template

### API errors
- Check backend is running
- Check API URL
- Check CORS configuration
- Check network tab

### Styling issues
- Check CSS file is loaded
- Check for CSS conflicts
- Clear browser cache

---

## ✅ Checklist

- [ ] Copy all files
- [ ] Choose setup option
- [ ] Update module files
- [ ] Configure API URL
- [ ] Add component to template
- [ ] Test locally
- [ ] Run tests
- [ ] Deploy

---

**Ready to setup? Choose your option above and follow the steps!**
