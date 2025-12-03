package com.trong.Computer_sell.DTO.request.product;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StockAdjustmentRequest {
    private UUID productId;
    private Integer newStock;
    private String note;
    private String createdBy;
}
