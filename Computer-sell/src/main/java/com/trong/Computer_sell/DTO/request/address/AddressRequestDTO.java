package com.trong.Computer_sell.DTO.request.address;

import com.trong.Computer_sell.common.AddressType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequestDTO {
    
    @NotBlank(message = "Apartment number is required")
    private String apartmentNumber;
    
    @NotBlank(message = "Street number is required")
    private String streetNumber;
    
    @NotBlank(message = "Ward is required")
    private String ward;
    
    @NotBlank(message = "City is required")
    private String city;
    
    private String addressType = "HOME";
}
