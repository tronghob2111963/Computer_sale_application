package com.trong.Computer_sell.model;

import com.trong.Computer_sell.common.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity extends AbstractEntity {

    // ====== ORDER ======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    // ====== TRANSACTION INFO ======
    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod; // CASH, VNPAY, MOMO, VIETQR,...

    @Column(name = "provider", length = 50)
    private String provider;      // "VNPay", "Momo", "VietQR"

    @Column(name = "bank_code", length = 50)
    private String bankCode;      // BankCode trả về từ VNPay/Momo

    @Column(name = "payment_content", length = 255)
    private String paymentContent; // Nội dung thanh toán / orderInfo

    // ====== AMOUNT ======
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // ====== DATE ======
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    // ====== STATUS ======
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;

    // ====== OPTIONAL ======
    @Column(length = 255)
    private String note;

    // ====== VIETQR PROOF IMAGE ======
    @Column(name = "proof_image_url", length = 500)
    private String proofImageUrl;  // URL ảnh xác nhận chuyển khoản (VietQR)
}

