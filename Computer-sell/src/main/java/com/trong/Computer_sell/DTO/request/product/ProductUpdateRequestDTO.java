package com.trong.Computer_sell.DTO.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
public class ProductUpdateRequestDTO {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private UUID brandId;
    private UUID categoryId;
    private UUID productTypeId;
    private int warrantyPeriod;
    @Schema(type = "array", format = "binary")
    private List<MultipartFile> image = new ArrayList<>();
}
