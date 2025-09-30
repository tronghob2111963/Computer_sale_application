package com.trong.Computer_sell.DTO.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class TokenResponse implements Serializable {
    private String username;
    private String accessToken;
    private String refreshToken;
}
