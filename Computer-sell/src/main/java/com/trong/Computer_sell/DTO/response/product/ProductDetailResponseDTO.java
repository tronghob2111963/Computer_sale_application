package com.trong.Computer_sell.DTO.response.product;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
public class ProductDetailResponseDTO {
    private UUID id;
    private String name;
    private BigDecimal price;
    private String description;
    private String brandName;
    private String categoryName;
    private String productType;
    private Integer stock;
    private int warrantyPeriod;
    private List<String> image;
}
