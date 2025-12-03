package com.trong.Computer_sell.model;

import com.trong.Computer_sell.common.StockMovementType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bảng lịch sử biến động kho
 * Ghi lại mọi thay đổi tồn kho: nhập, xuất, hoàn trả, điều chỉnh
 */
@Entity
@Table(name = "tbl_stock_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockHistoryEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private StockMovementType movementType;

    @Column(nullable = false)
    private Integer quantity; // Số lượng thay đổi (+ nhập, - xuất)

    @Column(name = "stock_before", nullable = false)
    private Integer stockBefore; // Tồn kho trước khi thay đổi

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter; // Tồn kho sau khi thay đổi

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice; // Giá nhập/xuất

    @Column(name = "reference_id")
    private String referenceId; // ID tham chiếu (mã phiếu nhập, mã đơn hàng)

    @Column(name = "reference_type", length = 50)
    private String referenceType; // Loại tham chiếu: IMPORT_RECEIPT, ORDER

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_by")
    private String createdBy; // Người thực hiện

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;
}
