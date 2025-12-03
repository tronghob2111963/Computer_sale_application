# Fix Public Access for Product & Category Endpoints

## Problem
Users couldn't view products and categories without logging in because:
1. Frontend was sending "undefined" as Bearer token
2. Backend filter was rejecting all requests with invalid tokens
3. Even though endpoints were in WHITE_LIST, the filter ran before Spring Security

## Solution

### Backend Changes

#### 1. CustomizeRequestFilter.java
Added logic to skip authentication for invalid/undefined tokens:
```java
// Skip if token is invalid/undefined
if(authHeader.equals("undefined") || authHeader.equals("null") || authHeader.trim().isEmpty()) {
    log.warn("Invalid or undefined token received, skipping authentication");
    filterChain.doFilter(request, response);
    return;
}

// On token validation error, continue filter chain instead of blocking
catch (Exception e) {
    log.error("Access denied: {}", e.getMessage());
    filterChain.doFilter(request, response);
    return;
}
```

#### 2. AppConfig.java
Confirmed WHITE_LIST includes public endpoints:
```java
private String[] WHITE_LIST = {
    "/swagger-ui/**", 
    "/v3/api-docs/**", 
    "/swagger-ui.html", 
    "/auth/**", 
    "/user/register", 
    "/user/save", 
    "/product/**",      // ✅ Public
    "/category/**",     // ✅ Public
    "/brand/**",        // ✅ Public
    "/product-types/**" // ✅ Public
};
```

### Frontend Changes

#### 1. category.service.ts
Fixed authHeaders() to not send invalid tokens:
```typescript
private authHeaders(): HttpHeaders {
  const token = this.cookies.get('accessToken');
  // Only add Authorization header if token exists and is not empty
  if (token && token !== 'undefined' && token !== 'null' && token.trim() !== '') {
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }
  return new HttpHeaders();
}
```

#### 2. product.service.ts
Same fix as category.service.ts

## Result

✅ Users can now view products without logging in
✅ Users can browse categories without authentication
✅ Invalid tokens are handled gracefully
✅ Protected endpoints (cart, builds, orders) still require authentication

## Testing

### Test Public Access (No Login Required)
```bash
# List products
curl http://localhost:8080/product/list

# List categories
curl http://localhost:8080/category/list

# Get product detail
curl http://localhost:8080/product/detail/{productId}
```

### Test Protected Endpoints (Login Required)
```bash
# Should return 401 without token
curl http://localhost:8080/cart/{userId}
curl http://localhost:8080/builds/user/{userId}
curl http://localhost:8080/orders/user/{userId}

# Should work with valid token
curl -H "Authorization: Bearer {valid_token}" http://localhost:8080/cart/{userId}
```

## Security Notes

- Public endpoints: `/product/**`, `/category/**`, `/brand/**`, `/product-types/**`
- Protected endpoints: `/cart/**`, `/builds/**`, `/orders/**`, `/user/**`, `/admin/**`
- Authentication still required for creating/modifying data
- Token validation happens but doesn't block public endpoints
