# Fix for 403 Forbidden Errors on Cart and Notification Endpoints

## Problem
The frontend was receiving 403 (Forbidden) errors when trying to access:
- `GET /cart/{userId}` - Cart count loading
- `GET /api/notifications/user/{userId}/count` - Notification count
- `GET /api/notifications/user/{userId}/unread` - Unread notifications

## Root Causes

1. **Missing Authorization Annotations**: The NotificationController endpoints didn't have `@PreAuthorize` annotations, causing inconsistent authorization handling
2. **Token Validation Issues**: The JWT filter wasn't providing clear error messages when tokens were invalid/expired
3. **Timing Issues**: Components were trying to load data before userId was properly initialized

## Changes Made

### Backend (Java)

#### 1. NotificationController.java
- Added `@PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")` to all endpoints:
  - `getNotifications()`
  - `getUnreadNotifications()`
  - `getNotificationCount()`
  - `markAsRead()`
  - `markAllAsRead()`
  - `deleteNotification()`

#### 2. CustomizeRequestFilter.java
- Improved error handling and logging:
  - Changed HTTP status from 200 to 403 for auth failures
  - Added detailed error messages for token validation failures
  - Added logging for successful authentication
  - Better exception handling for user loading

### Frontend (Angular)

#### 1. header-layout.component.ts
- Updated `loadCartCount()` to use `getUserIdSafe()` instead of `getUserId()`
- Added better error logging and handling
- Added console logs for debugging

#### 2. notification-dropdown.component.ts
- Updated `ngOnInit()` to use `getUserIdSafe()` for userId retrieval
- Added error handling to polling subscription
- Improved logging for debugging
- Added null checks before making API calls

## How It Works

1. **Auth Interceptor** (`auth.interceptor.ts`): Automatically adds `Authorization: Bearer {token}` header to all HTTP requests
2. **JWT Filter** (`CustomizeRequestFilter.java`): Validates the token and extracts the username
3. **Authorization Annotations** (`@PreAuthorize`): Ensures user has required roles
4. **Safe ID Retrieval** (`getUserIdSafe()`): Falls back to JWT parsing if cookie is missing

## Testing

After these changes:

1. Login to the application
2. The header should load cart count without 403 errors
3. The notification dropdown should load unread count without 403 errors
4. Check browser console for debug logs starting with ✅, ❌, 🔔, 📦, 📥

## Debugging

If you still see 403 errors:

1. Check browser DevTools → Application → Cookies for `accessToken`
2. Check browser console for logs from auth service
3. Check backend logs for JWT validation errors
4. Verify the token hasn't expired (tokens expire after 1 hour by default)
5. Try logging out and logging back in to get a fresh token

## Token Expiration

- Access tokens expire after 1 hour (configurable in `application.yaml` via `jwt.expMinutes`)
- If you see 403 errors after extended use, the token may have expired
- Implement token refresh logic if needed
