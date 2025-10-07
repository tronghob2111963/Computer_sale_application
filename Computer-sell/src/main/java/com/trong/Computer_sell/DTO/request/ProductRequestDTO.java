package com.trong.Computer_sell.DTO.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
public class ProductRequestDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private UUID brandId;
    private UUID categoryId;
    private int warrantyPeriod;
    private List<MultipartFile> image;
}
