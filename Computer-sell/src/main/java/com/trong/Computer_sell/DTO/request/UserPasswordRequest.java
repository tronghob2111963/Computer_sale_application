package com.trong.Computer_sell.DTO.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordRequest {
    private Long id;
    private String password;
    private String confirmPassword;
}
