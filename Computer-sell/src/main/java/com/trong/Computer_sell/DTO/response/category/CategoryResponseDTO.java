package com.trong.Computer_sell.DTO.response.category;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CategoryResponseDTO {
    private UUID id;
    private String name;
}
