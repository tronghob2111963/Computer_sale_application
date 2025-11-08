package com.trong.Computer_sell.DTO.response.oder;

import com.trong.Computer_sell.DTO.response.payment.PaymentResponse;
import com.trong.Computer_sell.model.OrderEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAdminResponse {
    private UUID id;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime orderDate;
    private List<OrderItemResponse> items;
    private List<PaymentResponse> payments;

    public static OrderAdminResponse fromEntity(OrderEntity entity) {
        return OrderAdminResponse.builder()
                .id(entity.getId())
                .customerName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus().name())
                .paymentStatus(entity.getPaymentStatus().name())
                .paymentMethod(entity.getPaymentMethod())
                .orderDate(entity.getOrderDate())
                .items(entity.getOrderDetails().stream()
                        .map(OrderItemResponse::fromEntity)
                        .toList())
                .payments(entity.getPayments().stream()
                        .map(PaymentResponse::fromEntity)
                        .toList())
                .build();
    }
}