package com.trong.Computer_sell.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionEntity extends AbstractEntity {

    @Column(name = "promo_code", unique = true, nullable = false)
    private String promoCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderPromotionEntity> orderPromotions = new ArrayList<>();
}