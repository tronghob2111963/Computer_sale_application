package com.trong.Computer_sell.model;

import com.trong.Computer_sell.common.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tbl_invoices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Số hóa đơn - theo chuẩn kế toán
    @Column(unique = true)
    private String invoiceNumber;  // VD: INV-2025-000123

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    private LocalDateTime issuedDate;  // Ngày xuất hóa đơn

    private BigDecimal totalBeforeVat;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;

    private String buyerName;
    private String buyerPhone;
    private String buyerEmail;
    private String buyerAddress;

    private String sellerName;
    private String sellerTaxCode;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status; // DRAFT / ISSUED / CANCELED

    private String pdfUrl; // Nếu bạn muốn lưu S3/Cloud
}