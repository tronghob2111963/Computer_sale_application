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
public class PaymentResponse {

    private UUID id;
    private UUID orderId;
    private String transactionId;       // Mã giao dịch (nếu online)
    private String paymentMethod;       // CASH / VNPAY / MOMO
    private BigDecimal amount;          // Tổng số tiền thanh toán
    private String paymentStatus;       // PENDING / SUCCESS / FAILED
    private LocalDateTime paymentDate;  // Thời điểm thanh toán
    private String customerName;        // (optional) Dễ xem trong admin

    // Convert từ Entity sang DTO
    public static PaymentResponse fromEntity(PaymentEntity entity) {
        return PaymentResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .transactionId(entity.getTransactionId())
                .paymentMethod(entity.getPaymentMethod())
                .amount(entity.getAmount())
                .paymentStatus(entity.getPaymentStatus().name())
                .paymentDate(entity.getPaymentDate())
                .customerName(entity.getOrder().getUser() != null
                        ? entity.getOrder().getUser().getFirstName() + " " + entity.getOrder().getUser().getLastName()
                        : null)
                .build();
    }
}