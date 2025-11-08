package com.trong.Computer_sell.DTO.response.product;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ProductTypeResponseDTO {
    private UUID id;
    private String name;
}
