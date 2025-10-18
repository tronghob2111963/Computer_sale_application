package com.trong.Computer_sell.DTO.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
@Builder
public class ProductDetailResponse {
    private String name;
    private BigDecimal price;
    private String description;
    private String brandName;
    private String categoryName;
    private String productType;
    private int warrantyPeriod;
    private List<String> image;
}
