package com.trong.Computer_sell.DTO.response.auth;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TokenResponse implements Serializable {
    private UUID id;
    private String username;
    private String accessToken;
    private String refreshToken;
    private List<String> role;
}
