package com.trong.Computer_sell.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVectorDTO {
    private UUID id;
    private UUID productId;
    private String name;
    private BigDecimal price;
    private String category;
    private String brand;
    private String productType;
    private String description;
    private Integer stock;
    private Integer warrantyPeriod;
    private Double similarityScore;
}
