package com.trong.Computer_sell.controller;


import com.trong.Computer_sell.DTO.request.auth.SignInRequest;
import com.trong.Computer_sell.DTO.response.auth.TokenResponse;
import com.trong.Computer_sell.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
        log.info("Access Token Request for user: {}", request.getUsername());
        return authenticationService.getAccessToken(request);
    }


    @Operation(summary = "Get Refresh Token", description = "Generate new access token and refresh token by username and password")
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> getRefreshToken(@RequestBody SignInRequest request) {
        log.info("Refresh Token Request: {}", request.getUsername());
        return ResponseEntity.ok(authenticationService.getRefreshToken(request));
    }

    @Operation(summary = "Remove Token", description = "Remove token by user name and password")
    @PostMapping("/remove-token")
    public ResponseEntity<String> removeToken(HttpServletRequest request) {
       try{
           log.info("Remove Token Request");
           return ResponseEntity.ok(authenticationService.removeToken(request));
       } catch (Exception e) {
           throw new RuntimeException(e.getMessage());
       }
    }


}
