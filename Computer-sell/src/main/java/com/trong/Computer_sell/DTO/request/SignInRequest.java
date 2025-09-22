package com.trong.Computer_sell.DTO.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class SignInRequest implements Serializable {
    private String username;
    private String password;
    private String platform;
    private String version;
    private String deviceToken;
}
