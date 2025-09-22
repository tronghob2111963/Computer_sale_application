package com.trong.Computer_sell.service.impl;



import com.trong.Computer_sell.DTO.request.SignInRequest;
import com.trong.Computer_sell.DTO.request.TokenResponse;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.AuthenticationService;
import com.trong.Computer_sell.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j(topic = "AUTHENTICATION-SERVICE")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

//    @Override
//    public TokenResponse getAccessToken(SignInRequest request) {
//        log.info("Generating access token for user: {}", request.getUsername());
//
//        UserEntity user = userRepository.findByUsername(request.getUsername());
//        List<String> authorities = new ArrayList<>();
//        try{
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//            );
//            log.info("Authentication successful for user: {}", request.getUsername());
//            log.info("Is authenticated: {}", authentication.isAuthenticated());
//            log.info("Authorities: {}", authentication.getAuthorities().toString());
//
//            //neu xac thuc thanh cong, luu thong tin vao SecurityContext
//            authorities.add(authentication.getAuthorities().toString());
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        } catch (BadCredentialsException | DisabledException e) {
//            log.error("Failed to authenticate user: {}", request.getUsername());
//            throw new AccessDeniedException("Access denied!!! Invalid username or password " + e.getMessage());
//        }
//
//
//        String accessToken = jwtService.generateAccessToken(request.getUsername(), authorities);
//        String refreshToken = jwtService.generateRefreshToken(request.getUsername(), authorities);
//
//        return TokenResponse.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .build();
//    }
@Override
public TokenResponse getAccessToken(SignInRequest request) {
    log.info("Generating access token for user: {}", request.getUsername());

    UserEntity user = userRepository.findByUsername(request.getUsername());
    List<String> authorities = new ArrayList<>();
    try {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        log.info("Authentication successful for user: {}", request.getUsername());
        log.info("Is authenticated: {}", authentication.isAuthenticated());
        log.info("Authorities: {}", authentication.getAuthorities().toString());

        // Lấy từng quyền riêng lẻ
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            authorities.add(authority.getAuthority());
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (BadCredentialsException | DisabledException e) {
        log.error("Failed to authenticate user: {}", request.getUsername());
        throw new AccessDeniedException("Access denied!!! Invalid username or password " + e.getMessage());
    }

    String accessToken = jwtService.generateAccessToken(request.getUsername(), authorities);
    log.info("authorities: {}", accessToken);
    String refreshToken = jwtService.generateRefreshToken(request.getUsername(), authorities);

    return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
}

    @Override
    public TokenResponse getRefreshToken(String signInRequest) {
        return null;
    }
}
