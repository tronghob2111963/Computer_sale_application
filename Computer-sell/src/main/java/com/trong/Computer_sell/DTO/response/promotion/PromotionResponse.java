package com.trong.Computer_sell.DTO.response.promotion;

import com.trong.Computer_sell.model.PromotionEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {
    private UUID id;
    private String promoCode;
    private String description;
    private BigDecimal discountPercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;

    public static PromotionResponse fromEntity(PromotionEntity entity) {
        return PromotionResponse.builder()
                .id(entity.getId())
                .promoCode(entity.getPromoCode())
                .description(entity.getDescription())
                .discountPercent(entity.getDiscountPercent())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isActive(entity.getIsActive())
                .build();
    }
}