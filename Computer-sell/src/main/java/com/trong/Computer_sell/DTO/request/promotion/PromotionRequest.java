package com.trong.Computer_sell.DTO.request.promotion;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PromotionRequest {
    private String promoCode;
    private String description;
    private BigDecimal discountPercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
}