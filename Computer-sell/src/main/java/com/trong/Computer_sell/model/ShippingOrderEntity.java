package com.trong.Computer_sell.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_shipping_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private boolean paymentCompleted;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt = LocalDateTime.now();
}