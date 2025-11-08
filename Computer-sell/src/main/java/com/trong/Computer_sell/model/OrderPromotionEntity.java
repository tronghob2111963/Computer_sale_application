package com.trong.Computer_sell.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tbl_order_promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPromotionEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_id")
    private PromotionEntity promotion;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;
}
