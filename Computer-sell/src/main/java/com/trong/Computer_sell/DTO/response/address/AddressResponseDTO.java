package com.trong.Computer_sell.DTO.response.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDTO {
    private UUID id;
    private String apartmentNumber;
    private String streetNumber;
    private String ward;
    private String city;
    private String addressType;
    private String fullAddress;
}
