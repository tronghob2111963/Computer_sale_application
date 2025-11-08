package com.trong.Computer_sell.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tbl_order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;
}
