# 🤖 AI Chatbot - Computer Sell Application

## ⭐ Bắt Đầu Tại Đây

Chào mừng! Bạn vừa cài đặt một **AI Chatbot** cho ứng dụng bán máy tính.

### 🚀 Bắt Đầu Nhanh (5 phút)

Nếu bạn muốn chạy chatbot ngay lập tức, hãy làm theo:

**[👉 CHATBOT_QUICK_START.md](CHATBOT_QUICK_START.md)**

### 📚 Tất Cả Tài Liệu

Xem danh sách đầy đủ tất cả tài liệu:

**[📖 CHATBOT_DOCUMENTATION_INDEX.md](CHATBOT_DOCUMENTATION_INDEX.md)**

## 🎯 Chọn Đường Dẫn Của Bạn

### 👨‍💻 Tôi là Developer

1. **Bắt đầu nhanh**: [CHATBOT_QUICK_START.md](CHATBOT_QUICK_START.md) (5 min)
2. **Hiểu chi tiết**: [CHATBOT_README.md](CHATBOT_README.md) (15 min)
3. **Tích hợp frontend**: [CHATBOT_FRONTEND_INTEGRATION.md](CHATBOT_FRONTEND_INTEGRATION.md) (20 min)
4. **Test API**: [CHATBOT_TEST_EXAMPLES.md](CHATBOT_TEST_EXAMPLES.md) (25 min)

### 🔧 Tôi gặp lỗi

1. **Xem troubleshooting**: [CHATBOT_TROUBLESHOOTING.md](CHATBOT_TROUBLESHOOTING.md)
2. **Tìm lỗi của bạn**
3. **Làm theo giải pháp**

### 📋 Tôi muốn deploy

1. **Kiểm tra checklist**: [CHATBOT_FINAL_CHECKLIST.md](CHATBOT_FINAL_CHECKLIST.md)
2. **Làm theo deployment steps**
3. **Verify endpoints**

### 🎨 Tôi muốn tích hợp frontend

1. **Xem hướng dẫn**: [CHATBOT_FRONTEND_INTEGRATION.md](CHATBOT_FRONTEND_INTEGRATION.md)
2. **Copy code**
3. **Test integration**

## 📊 Tính Năng

✅ **Chat AI** - Trò chuyện với AI bằng tiếng Việt  
✅ **Kiểm Tra Sản Phẩm** - Xem sản phẩm còn hàng không  
✅ **Lấy Giá** - Hỏi giá sản phẩm  
✅ **Lưu Lịch Sử** - Lưu tất cả cuộc trò chuyện  
✅ **Real-time UI** - Giao diện cập nhật real-time  

## 🔧 Công Nghệ

- **Backend**: Java Spring Boot 3.5.5
- **Frontend**: Angular
- **Database**: PostgreSQL
- **AI**: Google Gemini API
- **HTTP**: RestTemplate

## 📁 Cấu Trúc

```
Chatbot Files:
├── START_HERE.md ⭐ (file này)
├── CHATBOT_QUICK_START.md
├── CHATBOT_README.md
├── CHATBOT_SETUP_GUIDE.md
├── CHATBOT_FRONTEND_INTEGRATION.md
├── CHATBOT_TEST_EXAMPLES.md
├── CHATBOT_TROUBLESHOOTING.md
├── CHATBOT_IMPLEMENTATION_SUMMARY.md
├── CHATBOT_FINAL_CHECKLIST.md
└── CHATBOT_DOCUMENTATION_INDEX.md

Backend:
├── Computer-sell/src/main/java/com/trong/Computer_sell/
│   ├── controller/ChatBotController.java
│   ├── service/ChatBotService.java
│   ├── service/impl/ChatBotServiceImpl.java
│   ├── model/ChatLog.java
│   ├── repository/ChatLogRepository.java
│   ├── DTO/ChatMessageDTO.java
│   ├── DTO/ChatResponseDTO.java
│   ├── config/RestTemplateConfig.java
│   └── config/CorsConfig.java
└── pom.xml

Frontend:
├── Computer_Sell_FrontEnd/src/app/
│   ├── services/chatbot.service.ts
│   └── components/chatbot/
│       ├── chatbot.component.ts
│       ├── chatbot.component.html
│       └── chatbot.component.css
└── app.module.ts
```

## 🚀 5 Bước Để Chạy

### 1️⃣ Lấy API Key (2 min)
```
Truy cập: https://aistudio.google.com/app/apikeys
Tạo API key mới
Copy API key
```

### 2️⃣ Set Environment Variable (1 min)
```bash
# Windows CMD
set GEMINI_API_KEY=your-api-key-here

# Windows PowerShell
$env:GEMINI_API_KEY="your-api-key-here"

# Linux/Mac
export GEMINI_API_KEY=your-api-key-here
```

### 3️⃣ Build Project (2 min)
```bash
cd Computer-sell
mvn clean install -DskipTests
```

### 4️⃣ Run Application (1 min)
```bash
mvn spring-boot:run
```

### 5️⃣ Test Chatbot (1 min)
```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Xin chào"}'
```

## 📖 Tài Liệu

| Document | Mục Đích | Thời Gian |
|----------|---------|----------|
| [CHATBOT_QUICK_START.md](CHATBOT_QUICK_START.md) | Bắt đầu nhanh | 5 min |
| [CHATBOT_README.md](CHATBOT_README.md) | Tổng quan | 15 min |
| [CHATBOT_SETUP_GUIDE.md](CHATBOT_SETUP_GUIDE.md) | Cài đặt chi tiết | 10 min |
| [CHATBOT_FRONTEND_INTEGRATION.md](CHATBOT_FRONTEND_INTEGRATION.md) | Tích hợp Angular | 20 min |
| [CHATBOT_TEST_EXAMPLES.md](CHATBOT_TEST_EXAMPLES.md) | Test API | 25 min |
| [CHATBOT_TROUBLESHOOTING.md](CHATBOT_TROUBLESHOOTING.md) | Khắc phục lỗi | 30 min |
| [CHATBOT_IMPLEMENTATION_SUMMARY.md](CHATBOT_IMPLEMENTATION_SUMMARY.md) | Chi tiết implementation | 15 min |
| [CHATBOT_FINAL_CHECKLIST.md](CHATBOT_FINAL_CHECKLIST.md) | Deployment checklist | 10 min |

## 🎯 API Endpoints

### Chat
```
POST /api/chatbot/chat?userId=1
Content-Type: application/json

{
  "message": "Laptop Dell XPS 13 bao nhiêu tiền?"
}
```

### Product Availability
```
GET /api/chatbot/product-availability?productName=Laptop
```

### Product Price
```
GET /api/chatbot/product-price?productName=Laptop
```

## 🔐 Bảo Mật

- ✅ API key trong environment variable
- ✅ Không commit API key vào git
- ✅ Input validation
- ✅ Error handling
- ✅ CORS configured

## 🐛 Gặp Lỗi?

1. **Build Error**: Xem [CHATBOT_TROUBLESHOOTING.md](CHATBOT_TROUBLESHOOTING.md)
2. **API Error**: Xem [CHATBOT_TEST_EXAMPLES.md](CHATBOT_TEST_EXAMPLES.md)
3. **Setup Error**: Xem [CHATBOT_QUICK_START.md](CHATBOT_QUICK_START.md)

## 📞 Hỗ Trợ

- 📖 Xem tài liệu: [CHATBOT_DOCUMENTATION_INDEX.md](CHATBOT_DOCUMENTATION_INDEX.md)
- 🔧 Troubleshooting: [CHATBOT_TROUBLESHOOTING.md](CHATBOT_TROUBLESHOOTING.md)
- 🧪 Test Examples: [CHATBOT_TEST_EXAMPLES.md](CHATBOT_TEST_EXAMPLES.md)

## ✅ Checklist

- [ ] Lấy Gemini API key
- [ ] Set environment variable
- [ ] Build project
- [ ] Run application
- [ ] Test chatbot
- [ ] Tích hợp frontend
- [ ] Deploy

## 🎉 Tiếp Theo

1. **Bắt đầu**: [CHATBOT_QUICK_START.md](CHATBOT_QUICK_START.md)
2. **Tích hợp**: [CHATBOT_FRONTEND_INTEGRATION.md](CHATBOT_FRONTEND_INTEGRATION.md)
3. **Deploy**: [CHATBOT_FINAL_CHECKLIST.md](CHATBOT_FINAL_CHECKLIST.md)

---

## 📚 Tất Cả Tài Liệu

### Quick Start
- [CHATBOT_QUICK_START.md](CHATBOT_QUICK_START.md) ⭐ **BẮT ĐẦU TỪ ĐÂY**

### Setup & Configuration
- [CHATBOT_SETUP_GUIDE.md](CHATBOT_SETUP_GUIDE.md)
- [CHATBOT_README.md](CHATBOT_README.md)

### Development
- [CHATBOT_FRONTEND_INTEGRATION.md](CHATBOT_FRONTEND_INTEGRATION.md)
- [CHATBOT_TEST_EXAMPLES.md](CHATBOT_TEST_EXAMPLES.md)
- [CHATBOT_IMPLEMENTATION_SUMMARY.md](CHATBOT_IMPLEMENTATION_SUMMARY.md)

### Troubleshooting & Deployment
- [CHATBOT_TROUBLESHOOTING.md](CHATBOT_TROUBLESHOOTING.md)
- [CHATBOT_FINAL_CHECKLIST.md](CHATBOT_FINAL_CHECKLIST.md)

### Index
- [CHATBOT_DOCUMENTATION_INDEX.md](CHATBOT_DOCUMENTATION_INDEX.md)

---

**Sẵn sàng? 👉 [Bắt đầu với CHATBOT_QUICK_START.md](CHATBOT_QUICK_START.md)**

---

**Last Updated**: 2024-12-07  
**Version**: 1.0.0  
**Status**: ✅ Production Ready
