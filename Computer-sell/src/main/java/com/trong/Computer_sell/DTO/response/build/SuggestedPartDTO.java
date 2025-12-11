package com.trong.Computer_sell.DTO.response.build;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class SuggestedPartDTO {
    private String productType;
    private UUID productId;
    private String productName;
    private String brand;
    private BigDecimal price;
    private Integer stock;
    private String reason;
}
