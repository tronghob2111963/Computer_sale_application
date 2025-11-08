package com.trong.Computer_sell.model;


import com.trong.Computer_sell.common.ImportReceiptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_import_receipts")
@Getter
@Setter
public class ImportReceiptEntity extends AbstractEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "receipt_code", unique = true, nullable = false)
    private String receiptCode; // Mã phiếu nhập ví dụ: "PN-2025-0001"

    @Column(name = "receipt_date", nullable = false)
    private LocalDateTime receiptDate; // Ngày nhập hàng

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ImportReceiptStatus status = ImportReceiptStatus.PENDING;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImportReceiptDetailEntity> details = new ArrayList<>();

    // ================== HÀM HỖ TRỢ ==================
    public void addDetail(ImportReceiptDetailEntity detail) {
        details.add(detail);
        detail.setReceipt(this);
    }

    public void removeDetail(ImportReceiptDetailEntity detail) {
        details.remove(detail);
        detail.setReceipt(null);
    }
}
