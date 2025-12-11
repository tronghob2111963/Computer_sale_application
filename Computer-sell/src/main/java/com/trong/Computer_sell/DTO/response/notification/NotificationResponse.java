package com.trong.Computer_sell.DTO.response.notification;

import com.trong.Computer_sell.common.NotificationType;
import com.trong.Computer_sell.model.NotificationEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class NotificationResponse {
    private UUID id;
    private NotificationType type;
    private String title;
    private String message;
    private UUID referenceId;
    private String referenceType;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String timeAgo;

    public static NotificationResponse fromEntity(NotificationEntity entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .referenceId(entity.getReferenceId())
                .referenceType(entity.getReferenceType())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .timeAgo(calculateTimeAgo(entity.getCreatedAt()))
                .build();
    }

    private static String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";
        
        long days = hours / 24;
        if (days < 7) return days + " ngày trước";
        
        long weeks = days / 7;
        if (weeks < 4) return weeks + " tuần trước";
        
        long months = days / 30;
        if (months < 12) return months + " tháng trước";
        
        return days / 365 + " năm trước";
    }
}
