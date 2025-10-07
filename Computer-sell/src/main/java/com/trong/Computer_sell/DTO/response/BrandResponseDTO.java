package com.trong.Computer_sell.DTO.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BrandResponseDTO {
    private String name;
    private String country;
    private String description;
}
