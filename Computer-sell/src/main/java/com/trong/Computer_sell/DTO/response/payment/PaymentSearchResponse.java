package com.trong.Computer_sell.DTO.response.payment;

import com.trong.Computer_sell.model.PaymentEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchResponse {
    private UUID id;
    private String customerName;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String transactionId;

    public static PaymentSearchResponse fromEntity(PaymentEntity entity) {
        return PaymentSearchResponse.builder()
                .id(entity.getId())
                .customerName(
                        entity.getOrder() != null && entity.getOrder().getUser() != null
                                ? entity.getOrder().getUser().getFirstName() + " " + entity.getOrder().getUser().getLastName()
                                : "Khách hàng ẩn danh"
                )
                .paymentMethod(entity.getPaymentMethod())
                .paymentStatus(entity.getPaymentStatus().name())
                .amount(entity.getAmount())
                .paymentDate(entity.getPaymentDate())
                .transactionId(entity.getTransactionId())
                .build();
    }
}