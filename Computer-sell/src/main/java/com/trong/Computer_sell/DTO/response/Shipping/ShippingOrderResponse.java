package com.trong.Computer_sell.DTO.response.Shipping;

import com.trong.Computer_sell.model.ShippingOrderEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOrderResponse {

    private UUID id;
    private UUID orderId;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private boolean paymentCompleted;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public static ShippingOrderResponse fromEntity(ShippingOrderEntity entity) {
        return ShippingOrderResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .recipientName(entity.getRecipientName())
                .recipientPhone(entity.getRecipientPhone())
                .shippingAddress(entity.getShippingAddress())
                .paymentCompleted(entity.isPaymentCompleted())
                .totalAmount(entity.getTotalAmount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}