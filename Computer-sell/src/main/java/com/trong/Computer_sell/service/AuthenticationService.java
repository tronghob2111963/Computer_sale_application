package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.SignInRequest;
import com.trong.Computer_sell.DTO.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    TokenResponse getAccessToken(SignInRequest signInRequest);
    TokenResponse getRefreshToken(SignInRequest request);
    String removeToken(HttpServletRequest request);


}
