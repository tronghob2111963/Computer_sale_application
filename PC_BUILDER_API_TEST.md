# PC Builder API Testing Guide

## Prerequisites
- Backend server running on port 8080
- Valid JWT token for authentication
- User account created

## API Endpoints Testing

### 1. Create a New Build
```bash
curl -X POST http://localhost:8080/builds/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "userId": "USER_UUID",
    "name": "Gaming PC 2024"
  }'
```

Expected Response:
```json
{
  "status": 202,
  "message": "Build created successfully",
  "data": {
    "id": "BUILD_UUID",
    "name": "Gaming PC 2024",
    "totalPrice": 0,
    "isPublic": false,
    "details": []
  }
}
```

### 2. Add Product to Build
```bash
curl -X POST "http://localhost:8080/builds/BUILD_UUID/add-product?productId=PRODUCT_UUID&quantity=1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Expected Response:
```json
{
  "status": 202,
  "message": "Product added to build successfully",
  "data": "BUILD_UUID"
}
```

### 3. Update Product Quantity
```bash
curl -X PUT "http://localhost:8080/builds/BUILD_UUID/update-quantity?productId=PRODUCT_UUID&quantity=2" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Remove Product from Build
```bash
curl -X DELETE "http://localhost:8080/builds/BUILD_UUID/remove-product/PRODUCT_UUID" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5. Get User's Builds
```bash
curl -X GET "http://localhost:8080/builds/user/USER_UUID" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

Expected Response:
```json
{
  "status": 202,
  "message": "Builds found successfully",
  "data": [
    {
      "id": "BUILD_UUID",
      "name": "Gaming PC 2024",
      "totalPrice": 25000000,
      "isPublic": false,
      "details": [
        {
          "productId": "PRODUCT_UUID",
          "productName": "Intel Core i7-13700K",
          "price": 10000000,
          "quantity": 1,
          "imageUrl": "http://localhost:8080/uploads/cpu.jpg"
        }
      ]
    }
  ]
}
```

### 6. Get Build Details
```bash
curl -X GET "http://localhost:8080/builds/BUILD_UUID" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 7. Delete Build
```bash
curl -X DELETE "http://localhost:8080/builds/BUILD_UUID" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Testing Workflow

### Complete Build Creation Flow
1. **Create Build**
   - POST /builds/create
   - Save returned BUILD_UUID

2. **Add CPU**
   - Find CPU product ID from /product/list/category/CPU_CATEGORY_ID
   - POST /builds/{buildId}/add-product?productId={cpuId}&quantity=1

3. **Add Mainboard**
   - Find Mainboard product ID
   - POST /builds/{buildId}/add-product?productId={mainboardId}&quantity=1

4. **Add RAM**
   - Find RAM product ID
   - POST /builds/{buildId}/add-product?productId={ramId}&quantity=2

5. **Add GPU**
   - Find GPU product ID
   - POST /builds/{buildId}/add-product?productId={gpuId}&quantity=1

6. **Add Storage**
   - Find Storage product ID
   - POST /builds/{buildId}/add-product?productId={storageId}&quantity=1

7. **Verify Build**
   - GET /builds/{buildId}
   - Check totalPrice is calculated correctly

## Common Issues

### Issue: "Build not found"
- Verify BUILD_UUID is correct
- Check if build was deleted

### Issue: "Product not found"
- Verify PRODUCT_UUID exists
- Check product is in stock

### Issue: "User not found"
- Verify USER_UUID is correct
- Check user authentication

### Issue: Total price not updating
- Check backend calculation logic
- Verify product prices are set correctly

## Frontend Testing

### Navigate to PC Builder
```
http://localhost:4200/pc-builder
```

### Test Steps:
1. Login with valid credentials
2. Navigate to /pc-builder
3. Click "Chọn CPU" button
4. Select a CPU from modal
5. Verify CPU appears in build list
6. Change quantity
7. Verify total price updates
8. Repeat for other components
9. Click "Lưu cấu hình"
10. Navigate to /my-builds
11. Verify build appears in list

## Database Verification

### Check Build Created
```sql
SELECT * FROM tbl_user_builds WHERE user_id = 'USER_UUID';
```

### Check Build Details
```sql
SELECT ubd.*, p.name, p.price 
FROM tbl_user_build_details ubd
JOIN tbl_products p ON ubd.product_id = p.id
WHERE ubd.build_id = 'BUILD_UUID';
```

### Verify Total Price Calculation
```sql
SELECT 
  ub.id,
  ub.name,
  ub.total_price as stored_total,
  SUM(p.price * ubd.quantity) as calculated_total
FROM tbl_user_builds ub
LEFT JOIN tbl_user_build_details ubd ON ub.id = ubd.build_id
LEFT JOIN tbl_products p ON ubd.product_id = p.id
WHERE ub.id = 'BUILD_UUID'
GROUP BY ub.id, ub.name, ub.total_price;
```

## Performance Testing

### Load Test - Create Multiple Builds
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/builds/create \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer YOUR_TOKEN" \
    -d "{\"userId\": \"USER_UUID\", \"name\": \"Test Build $i\"}"
done
```

### Load Test - Add Products
```bash
for i in {1..5}; do
  curl -X POST "http://localhost:8080/builds/BUILD_UUID/add-product?productId=PRODUCT_UUID&quantity=1" \
    -H "Authorization: Bearer YOUR_TOKEN"
done
```
