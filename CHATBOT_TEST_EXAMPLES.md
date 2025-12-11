# Ví Dụ Test Chatbot

## Chuẩn Bị

1. Đảm bảo backend chạy: `http://localhost:8080`
2. Đảm bảo có API key Gemini
3. Đảm bảo database có dữ liệu sản phẩm

## Test với cURL

### 1. Chat với Chatbot

```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Laptop Dell XPS 13 còn hàng không?"}'
```

**Response mong đợi**:
```json
{
  "message": "Sản phẩm 'Laptop Dell XPS 13' hiện có sẵn với 5 sản phẩm trong kho.",
  "timestamp": "2024-12-07T10:30:00"
}
```

### 2. Kiểm Tra Tính Khả Dụng

```bash
curl "http://localhost:8080/api/chatbot/product-availability?productName=Laptop%20Dell%20XPS%2013"
```

**Response mong đợi**:
```
Sản phẩm 'Laptop Dell XPS 13' hiện có sẵn với 5 sản phẩm trong kho.
```

### 3. Lấy Giá Sản Phẩm

```bash
curl "http://localhost:8080/api/chatbot/product-price?productName=Laptop%20Dell%20XPS%2013"
```

**Response mong đợi**:
```
Giá của Laptop Dell XPS 13: 25000000 VND
```

## Test với Postman

### 1. Tạo Request Chat

**Method**: POST
**URL**: `http://localhost:8080/api/chatbot/chat?userId=1`

**Headers**:
```
Content-Type: application/json
```

**Body** (raw JSON):
```json
{
  "message": "Laptop Dell XPS 13 bao nhiêu tiền?"
}
```

**Click Send** → Xem response

### 2. Tạo Request Availability

**Method**: GET
**URL**: `http://localhost:8080/api/chatbot/product-availability?productName=Laptop`

**Click Send** → Xem response

### 3. Tạo Request Price

**Method**: GET
**URL**: `http://localhost:8080/api/chatbot/product-price?productName=Laptop`

**Click Send** → Xem response

## Test với JavaScript

### Fetch API

```javascript
// Test chat
async function testChat() {
  const response = await fetch('http://localhost:8080/api/chatbot/chat?userId=1', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      message: 'Laptop Dell XPS 13 bao nhiêu tiền?'
    })
  });
  
  const data = await response.json();
  console.log('Response:', data);
}

// Test availability
async function testAvailability() {
  const response = await fetch('http://localhost:8080/api/chatbot/product-availability?productName=Laptop');
  const data = await response.text();
  console.log('Availability:', data);
}

// Test price
async function testPrice() {
  const response = await fetch('http://localhost:8080/api/chatbot/product-price?productName=Laptop');
  const data = await response.text();
  console.log('Price:', data);
}

// Chạy tests
testChat();
testAvailability();
testPrice();
```

### Axios

```javascript
const axios = require('axios');

// Test chat
async function testChat() {
  try {
    const response = await axios.post('http://localhost:8080/api/chatbot/chat?userId=1', {
      message: 'Laptop Dell XPS 13 bao nhiêu tiền?'
    });
    console.log('Chat Response:', response.data);
  } catch (error) {
    console.error('Error:', error.message);
  }
}

// Test availability
async function testAvailability() {
  try {
    const response = await axios.get('http://localhost:8080/api/chatbot/product-availability', {
      params: { productName: 'Laptop' }
    });
    console.log('Availability:', response.data);
  } catch (error) {
    console.error('Error:', error.message);
  }
}

// Test price
async function testPrice() {
  try {
    const response = await axios.get('http://localhost:8080/api/chatbot/product-price', {
      params: { productName: 'Laptop' }
    });
    console.log('Price:', response.data);
  } catch (error) {
    console.error('Error:', error.message);
  }
}

// Chạy tests
testChat();
testAvailability();
testPrice();
```

## Test Scenarios

### Scenario 1: Hỏi về Sản Phẩm Có Sẵn

**Input**: "Laptop Dell XPS 13 còn hàng không?"

**Expected**: Chatbot trả lời về tính khả dụng

```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Laptop Dell XPS 13 còn hàng không?"}'
```

### Scenario 2: Hỏi về Giá

**Input**: "Giá của MacBook Pro bao nhiêu?"

**Expected**: Chatbot trả lời giá sản phẩm

```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Giá của MacBook Pro bao nhiêu?"}'
```

### Scenario 3: Hỏi Chung Chung

**Input**: "Bạn có sản phẩm nào dưới 10 triệu không?"

**Expected**: Chatbot trả lời một cách thân thiện

```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Bạn có sản phẩm nào dưới 10 triệu không?"}'
```

### Scenario 4: Sản Phẩm Không Tồn Tại

**Input**: "Bạn có sản phẩm XYZ không?"

**Expected**: Chatbot thông báo sản phẩm không tìm thấy

```bash
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Bạn có sản phẩm XYZ không?"}'
```

## Kiểm Tra Database

### Xem Chat Logs

```sql
-- Kết nối database
psql -U postgres -d computer_sell

-- Xem tất cả chat logs
SELECT * FROM tbl_chatlogs;

-- Xem chat logs của user 1
SELECT * FROM tbl_chatlogs WHERE user_id = 1;

-- Xem chat logs gần đây nhất
SELECT * FROM tbl_chatlogs ORDER BY timestamp DESC LIMIT 10;
```

### Xem Sản Phẩm

```sql
-- Xem tất cả sản phẩm
SELECT id, name, price, stock FROM tbl_products LIMIT 10;

-- Tìm sản phẩm theo tên
SELECT id, name, price, stock FROM tbl_products WHERE name ILIKE '%Laptop%';

-- Xem sản phẩm có sẵn
SELECT id, name, price, stock FROM tbl_products WHERE stock > 0;
```

## Performance Test

### Load Test với Apache Bench

```bash
# Test 100 requests, 10 concurrent
ab -n 100 -c 10 "http://localhost:8080/api/chatbot/product-availability?productName=Laptop"

# Test 1000 requests, 50 concurrent
ab -n 1000 -c 50 "http://localhost:8080/api/chatbot/product-availability?productName=Laptop"
```

### Load Test với wrk

```bash
# Cài đặt wrk (nếu chưa có)
# macOS: brew install wrk
# Linux: apt-get install wrk

# Test 4 threads, 100 connections, 30 seconds
wrk -t4 -c100 -d30s "http://localhost:8080/api/chatbot/product-availability?productName=Laptop"
```

## Monitoring

### Xem Logs Real-time

```bash
# Linux/Mac
tail -f Computer-sell/logs/application.log

# Windows PowerShell
Get-Content Computer-sell/logs/application.log -Wait
```

### Xem Metrics

```bash
# Actuator endpoint
curl http://localhost:8080/actuator

# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics
```

## Troubleshooting Test

### Test không nhận được response

1. Kiểm tra backend chạy:
```bash
curl http://localhost:8080/actuator/health
```

2. Kiểm tra API key:
```bash
echo $GEMINI_API_KEY
```

3. Kiểm tra database:
```bash
psql -U postgres -d computer_sell -c "SELECT COUNT(*) FROM tbl_products;"
```

### Test nhận được lỗi 401

1. API key không hợp lệ
2. Kiểm tra lại API key tại https://aistudio.google.com/app/apikeys

### Test nhận được lỗi 500

1. Xem logs: `tail -f Computer-sell/logs/application.log`
2. Kiểm tra database connection
3. Kiểm tra Gemini API status

## Automation Test

### Tạo test script

Tạo file `test_chatbot.sh`:

```bash
#!/bin/bash

echo "Testing Chatbot API..."

# Test 1: Chat
echo -e "\n=== Test 1: Chat ==="
curl -X POST http://localhost:8080/api/chatbot/chat?userId=1 \
  -H "Content-Type: application/json" \
  -d '{"message":"Laptop Dell XPS 13 bao nhiêu tiền?"}'

# Test 2: Availability
echo -e "\n\n=== Test 2: Availability ==="
curl "http://localhost:8080/api/chatbot/product-availability?productName=Laptop"

# Test 3: Price
echo -e "\n\n=== Test 3: Price ==="
curl "http://localhost:8080/api/chatbot/product-price?productName=Laptop"

echo -e "\n\nTests completed!"
```

Chạy:
```bash
chmod +x test_chatbot.sh
./test_chatbot.sh
```
