package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.request.SignInRequest;
import com.trong.Computer_sell.DTO.request.SignInRequest;
import com.trong.Computer_sell.DTO.request.TokenResponse;
import com.trong.Computer_sell.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Get Access Token", description = "Get access token and refresh token by user name and password")
    @PostMapping("/access-token")
    public TokenResponse getAccessToken(@RequestBody SignInRequest request){
       try{
           log.info("Access Token Request");
           return authenticationService.getAccessToken(request);
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }


    @Operation(summary = "Get refresh Token", description = "Get refresh token and refresh token by user name and password")
    @PostMapping("/refresh-token")
    public TokenResponse getRefreshToken(@RequestBody SignInRequest request){
        log.info("Refresh Token Request");
        return TokenResponse.builder().accessToken("accesToken").refreshToken("refreshToken").build();
    }
}
