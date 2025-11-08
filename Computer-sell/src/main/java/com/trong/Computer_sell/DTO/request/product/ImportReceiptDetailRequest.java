package com.trong.Computer_sell.DTO.request.product;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ImportReceiptDetailRequest {
    private UUID productId;
    private Integer quantity;
    private BigDecimal importPrice;
}
