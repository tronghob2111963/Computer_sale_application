package com.trong.Computer_sell.DTO.response.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ImportReceiptDetailResponse {
    private String productName;
    private Integer quantity;
    private BigDecimal importPrice;
}

