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

    /**
     * Mối quan hệ với đơn hàng (Order)
     * Một đơn hàng có thể có nhiều giao dịch thanh toán
     * (ví dụ: thanh toán nhiều đợt hoặc trả góp)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    /**
     * Mã giao dịch từ cổng thanh toán (nếu là online).
     * Nếu thanh toán tiền mặt thì để null.
     */
    @Column(name = "transaction_id", length = 255)
    private String transactionId;

    /**
     * Hình thức thanh toán: "CASH", "VNPAY", "MOMO", ...
     */
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    /**
     * Tổng tiền thanh toán cho giao dịch này.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Thời điểm thanh toán.
     */
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    /**
     * Trạng thái thanh toán: PENDING, SUCCESS, FAILED, REFUNDED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus;

    /**
     * Ghi chú thêm cho giao dịch (tùy chọn).
     */
    @Column(length = 255)
    private String note;
}
