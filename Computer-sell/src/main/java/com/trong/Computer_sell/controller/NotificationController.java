package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.notification.NotificationCountResponse;
import com.trong.Computer_sell.DTO.response.notification.NotificationResponse;
import com.trong.Computer_sell.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "API quản lý thông báo")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    @Operation(summary = "Lấy danh sách thông báo của user")
    public ResponseEntity<Map<String, Object>> getNotifications(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        PageResponse<List<NotificationResponse>> response = 
                notificationService.getNotifications(userId, pageNo, pageSize);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Success");
        result.put("data", response);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    @Operation(summary = "Lấy thông báo chưa đọc của user")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(@PathVariable UUID userId) {
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Success");
        result.put("data", notifications);
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}/count")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    @Operation(summary = "Đếm số thông báo chưa đọc")
    public ResponseEntity<Map<String, Object>> getNotificationCount(@PathVariable UUID userId) {
        NotificationCountResponse count = notificationService.getNotificationCount(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Success");
        result.put("data", count);
        
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    @Operation(summary = "Đánh dấu thông báo đã đọc")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable UUID notificationId) {
        NotificationResponse notification = notificationService.markAsRead(notificationId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Đã đánh dấu đã đọc");
        result.put("data", notification);
        
        return ResponseEntity.ok(result);
    }

    @PutMapping("/user/{userId}/read-all")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@PathVariable UUID userId) {
        int count = notificationService.markAllAsRead(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Đã đánh dấu " + count + " thông báo đã đọc");
        result.put("data", Map.of("updatedCount", count));
        
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin', 'Staff','User')")
    @Operation(summary = "Xóa thông báo")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable UUID notificationId) {
        notificationService.deleteNotification(notificationId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "Đã xóa thông báo");
        
        return ResponseEntity.ok(result);
    }
}
