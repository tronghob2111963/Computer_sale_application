# Quick Reference: 403 Forbidden Fix

## What Was Fixed

### Backend
- ✅ Added `@PreAuthorize` annotations to NotificationController
- ✅ Improved JWT filter error handling and logging
- ✅ Changed error response status from 200 to 403

### Frontend  
- ✅ Updated cart loading to use `getUserIdSafe()`
- ✅ Updated notification loading to use `getUserIdSafe()`
- ✅ Added better error handling and logging

## Key Files Modified

**Backend:**
- `Computer-sell/src/main/java/com/trong/Computer_sell/controller/NotificationController.java`
- `Computer-sell/src/main/java/com/trong/Computer_sell/config/CustomizeRequestFilter.java`

**Frontend:**
- `Computer_Sell_FrontEnd/src/app/shared/header-layout/header-layout.component.ts`
- `Computer_Sell_FrontEnd/src/app/shared/notification-dropdown/notification-dropdown.component.ts`

## How to Test

1. **Rebuild Backend:**
   ```bash
   cd Computer-sell
   mvn clean install
   ```

2. **Restart Backend:**
   - Stop the running backend
   - Start it again (or use Docker)

3. **Test Frontend:**
   - Clear browser cache/cookies
   - Login again
   - Check that cart count and notification count load without errors
   - Open DevTools console to see debug logs

## Expected Behavior

After login:
- Cart count badge appears in header ✅
- Notification bell shows unread count ✅
- No 403 errors in console ✅
- Debug logs show successful operations ✅

## If Still Getting 403

1. **Check Token:**
   - Open DevTools → Application → Cookies
   - Verify `accessToken` cookie exists
   - Token should be a long JWT string

2. **Check Logs:**
   - Backend: Look for "✅ Username extracted" or "❌ Token validation failed"
   - Frontend: Look for "✅ Cart count updated" or "❌ Error loading cart"

3. **Try Logout/Login:**
   - Logout completely
   - Clear all cookies
   - Login again with fresh token

4. **Check Token Expiration:**
   - Tokens expire after 1 hour
   - If app has been idle, token may have expired
   - Login again to get fresh token
