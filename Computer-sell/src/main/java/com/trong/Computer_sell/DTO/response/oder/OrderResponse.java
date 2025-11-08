package com.trong.Computer_sell.DTO.response.oder;

import com.trong.Computer_sell.model.OrderEntity;
import com.trong.Computer_sell.model.OrderPromotionEntity;
import com.trong.Computer_sell.model.PromotionEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.trong.Computer_sell.common.PaymentMethod;
import com.trong.Computer_sell.common.PaymentStatus;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private UUID id;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime orderDate;
    private List<OrderDetailResponse> details;

    public static OrderResponse fromEntity(OrderEntity entity) {
        String promoCode = entity.getOrderPromotions().isEmpty()
                ? null
                : entity.getOrderPromotions().stream().map(OrderPromotionEntity::getPromotion).map(PromotionEntity::getPromoCode).findFirst().orElse(null);

        return OrderResponse.builder()
                .id(entity.getId())
                .status(entity.getStatus().name())
                .paymentStatus(entity.getPaymentStatus())
                .totalAmount(entity.getTotalAmount())
                .orderDate(entity.getOrderDate())
//                .promoCode(promoCode)
                .build();
    }
}