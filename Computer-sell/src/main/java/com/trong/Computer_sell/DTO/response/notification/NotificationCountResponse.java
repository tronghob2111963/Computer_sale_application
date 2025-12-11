package com.trong.Computer_sell.DTO.response.notification;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationCountResponse {
    private long unreadCount;
    private long totalCount;
}
