package com.trong.Computer_sell.DTO.response.brand;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class BrandResponseDTO {
    private UUID id;
    private String name;
    private String country;
    private String description;
}
