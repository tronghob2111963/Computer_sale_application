package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.common.TokenType;
import com.trong.Computer_sell.exception.InvalidDataException;
import com.trong.Computer_sell.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

import static com.trong.Computer_sell.common.TokenType.ACCESS_TOKEN;
import static com.trong.Computer_sell.common.TokenType.REFRESH_TOKEN;
import static io.jsonwebtoken.Jwts.*;


@Service
@Slf4j(topic = "JWT-SERVICE")
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.expMinutes}")
    private long expMinutes;

    @Value("${jwt.expDate}")
    private long expDate;

    @Value("${jwt.accessKey}")
    private String accessKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    @Override
    public String generateAccessToken(String username, List<String> authorities) {
        log.info("Generating access token for user: {}", username, authorities);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", authorities);
        log.info("Generating access token for user: {}", authorities);

        return generateAccessToken(claims, username);

    }

    @Override
    public String generateRefreshToken(String username, List<String> authorities) {
        log.info("Generating refresh token for user: {}", username);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", authorities);
        return generateRefreshToken(claims, username);
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        log.info("Extracting username from token: {}", token);
        return extractClaim(tokenType, token, Claims::getSubject);
    }

    @Override
    public List<String> extractAuthorities(String token, TokenType tokenType) {
        log.info("Extracting authorities from token: {}, type: {}", token, tokenType);
        Claims claims = extractAllClaim(token, tokenType);
        Object roles = claims.get("role");
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        return Collections.emptyList();
    }

    private <T> T extractClaim(TokenType type, String token, Function<Claims, T> claimsExtractor) {
        log.info("Extracting claims from token: {}, type: {}", token, type);
        final Claims claims = extractAllClaim(token, type);
        return claimsExtractor.apply(claims);
    }

private Claims extractAllClaim(String token, TokenType type) {
    try {
        Key key = getKey(type);
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    } catch (SignatureException | ExpiredJwtException e) {
        throw new AccessDeniedException("Access denied!!! error " + e.getMessage());
    }
}

    private String generateAccessToken(Map<String, Object> claims, String username){
        log.info("Generating token for user: {}", username);
        return builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * expMinutes))
                .signWith(getKey(ACCESS_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }
    private String generateRefreshToken(Map<String, Object> claims, String username){
        log.info("Generating token for user: {}", username);
        return builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * expDate))
                .signWith(getKey(REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey(TokenType type){
        switch (type){
            case ACCESS_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
            }
            case REFRESH_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            }
            default -> throw new InvalidDataException("Invalid token type");


        }

    }

}
