package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.SignInRequest;
import com.trong.Computer_sell.DTO.request.TokenResponse;

public interface AuthenticationService {

    TokenResponse getAccessToken(SignInRequest signInRequest);
    TokenResponse getRefreshToken(String signInRequest);

}
