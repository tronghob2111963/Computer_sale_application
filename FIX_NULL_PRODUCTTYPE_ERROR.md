# Fix for Null ProductType Error in Product Listing

## Problem
When viewing the product list without filters, the application was throwing a 400 error:
```
Cannot invoke "com.trong.Computer_sell.model.ProductTypeEntity.getName()" 
because the return value of "com.trong.Computer_sell.model.ProductEntity.getProductTypeId()" is null
```

This occurred because some products in the database don't have a `productTypeId` assigned (it's null).

## Root Cause
The `ProductServiceImpl` was calling `.getName()` on potentially null objects without null checks:
- `product.getProductTypeId().getName()` - throws NPE if productTypeId is null
- `product.getBrandId().getName()` - throws NPE if brandId is null
- `product.getCategory().getName()` - throws NPE if category is null

## Solution
Added null checks before accessing properties on related entities. Now the code safely handles missing relationships.

## Changes Made

### ProductServiceImpl.java

#### 1. `getAllProducts()` method
- Added null checks for `brandId`, `category`, and `productTypeId`
- Returns null for missing relationships instead of throwing exception
- Products without productType now display correctly

#### 2. `getAllProductsByBrandId()` method
- Added null checks for all related entities
- Added missing `id` and `stock` fields to response
- Improved consistency with other methods

#### 3. `getAllProductsByCategoryId()` method
- Added null checks for all related entities
- Added missing `id` and `stock` fields to response
- Improved consistency with other methods

#### 4. `getProductById()` method
- Added null checks for `brandId`, `category`, and `productTypeId`
- Safely handles products with missing relationships

## How It Works

Before (causes error):
```java
.productType(product.getProductTypeId().getName())  // NPE if null
```

After (safe):
```java
String productType = (product.getProductTypeId() != null)
        ? product.getProductTypeId().getName()
        : null;
.productType(productType)
```

## Testing

1. **Rebuild Backend:**
   ```bash
   cd Computer-sell
   mvn clean install
   ```

2. **Restart Backend**

3. **Test Product Listing:**
   - Navigate to product list page
   - Should display all products without errors
   - Products without productType show `null` for that field
   - No 400 errors in console

## Data Quality Note

While this fix prevents errors, it's recommended to:
1. Ensure all products have a valid `productTypeId` assigned
2. Run a migration to populate missing productType values
3. Add database constraints to prevent null productTypeId in future

Example migration:
```sql
-- Assign a default product type to products without one
UPDATE products 
SET product_type_id = (SELECT id FROM product_types LIMIT 1)
WHERE product_type_id IS NULL;
```

## Files Modified
- `Computer-sell/src/main/java/com/trong/Computer_sell/service/impl/ProductServiceImpl.java`
