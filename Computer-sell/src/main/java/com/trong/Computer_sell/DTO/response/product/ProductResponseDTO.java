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
public class ProductResponseDTO {
    private UUID id;
    private String name;
    private BigDecimal price;
    private String brandName;
    private String categoryName;
    private int warrantyPeriod;
    private int stock;
    private String productType;
    private List<String> image;
}
