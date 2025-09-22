package com.trong.Computer_sell.service;

import com.trong.Computer_sell.common.TokenType;
import org.springframework.boot.actuate.autoconfigure.wavefront.WavefrontProperties;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public interface JwtService {

    String generateAccessToken(String username, List<String> authorities);
    String generateRefreshToken(String username, List<String> authorities);

    String extractUsername(String token, TokenType tokenType);
    List<String> extractAuthorities(String token, TokenType tokenType);

}
