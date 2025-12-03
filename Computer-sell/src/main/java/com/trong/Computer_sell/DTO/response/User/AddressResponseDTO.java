package com.trong.Computer_sell.DTO.response.User;

import com.trong.Computer_sell.common.AddressType;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponseDTO implements Serializable {
    private java.util.UUID id;
    private String apartmentNumber;
    private String streetNumber;
    private String ward;
    private String city;
    private AddressType addressType;
    
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (apartmentNumber != null && !apartmentNumber.isEmpty()) {
            sb.append(apartmentNumber).append(", ");
        }
        if (streetNumber != null && !streetNumber.isEmpty()) {
            sb.append(streetNumber).append(", ");
        }
        if (ward != null && !ward.isEmpty()) {
            sb.append(ward).append(", ");
        }
        if (city != null && !city.isEmpty()) {
            sb.append(city);
        }
        return sb.toString();
    }
}
