package com.trong.Computer_sell.DTO.response.product;

import com.trong.Computer_sell.common.StockMovementType;
import com.trong.Computer_sell.model.StockHistoryEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockHistoryResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private StockMovementType movementType;
    private Integer quantity;
    private Integer stockBefore;
    private Integer stockAfter;
    private BigDecimal unitPrice;
    private String referenceId;
    private String referenceType;
    private String note;
    private String createdBy;
    private LocalDateTime movementDate;
    private LocalDateTime createdAt;

    public static StockHistoryResponse fromEntity(StockHistoryEntity entity) {
        return StockHistoryResponse.builder()
                .id(entity.getId())
                .productId(entity.getProduct().getId())
                .productName(entity.getProduct().getName())
                .movementType(entity.getMovementType())
                .quantity(entity.getQuantity())
                .stockBefore(entity.getStockBefore())
                .stockAfter(entity.getStockAfter())
                .unitPrice(entity.getUnitPrice())
                .referenceId(entity.getReferenceId())
                .referenceType(entity.getReferenceType())
                .note(entity.getNote())
                .createdBy(entity.getCreatedBy())
                .movementDate(entity.getMovementDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
