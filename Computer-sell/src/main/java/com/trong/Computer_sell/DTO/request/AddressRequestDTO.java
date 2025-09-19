package com.trong.Computer_sell.DTO.request;


import com.trong.Computer_sell.common.AddressType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AddressRequestDTO implements Serializable {
    private String apartmentNumber;
    private String streetNumber;
    private String ward;
    private String city;
    private AddressType addressType;
}
