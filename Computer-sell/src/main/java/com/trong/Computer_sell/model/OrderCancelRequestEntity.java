package com.trong.Computer_sell.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_order_cancel_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCancelRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(nullable = false, length = 255)
    private String reason; // Lý do hủy đơn

    @Column(nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();

    @Column(nullable = false)
    private boolean processed = false; // Đã duyệt hay chưa
}