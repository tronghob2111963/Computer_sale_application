package com.trong.Computer_sell.service.impl;



import com.trong.Computer_sell.DTO.request.auth.SignInRequest;
import com.trong.Computer_sell.DTO.response.auth.TokenResponse;
import com.trong.Computer_sell.exception.InvalidDataException;
import com.trong.Computer_sell.model.Role;
import com.trong.Computer_sell.model.TokenEntity;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.RoleRepository;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.AuthenticationService;
import com.trong.Computer_sell.service.JwtService;
import com.trong.Computer_sell.service.TokenService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.trong.Computer_sell.common.TokenType.ACCESS_TOKEN;


@Service
@Slf4j(topic = "AUTHENTICATION-SERVICE")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;

    @Override
    public TokenResponse getAccessToken(SignInRequest request) {
        log.info("Generating access token for user: {}", request.getUsername());

        UserEntity user = userRepository.findByUsername(request.getUsername());
        List<String> authorities = new ArrayList<>();
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            log.info("Authentication successful for user: {}", request.getUsername());
            log.info("Is authenticated: {}", authentication.isAuthenticated());
            log.info("Authorities: {}", authentication.getAuthorities().toString());

            //neu xac thuc thanh cong, luu thong tin vao SecurityContext
            authorities.add(authentication.getAuthorities().toString());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException | DisabledException e) {
            log.error("Failed to authenticate user: {}", request.getUsername());
            throw new AccessDeniedException("Access denied!!! Invalid username or password " + e.getMessage());
        }


        String accessToken = jwtService.generateAccessToken(request.getUsername(), authorities);
        String refreshToken = jwtService.generateRefreshToken(request.getUsername(), authorities);


        tokenService.save(TokenEntity.builder().username(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());

        return TokenResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(authorities)
                .build();
    }


    @Override
    public TokenResponse getRefreshToken(SignInRequest request) {
        log.info("Generating access token for user: {}", request.getUsername());

        UserEntity user = userRepository.findByUsername(request.getUsername());
        List<String> authorities = new ArrayList<>();
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            log.info("Authentication successful for user: {}", request.getUsername());
            log.info("Is authenticated: {}", authentication.isAuthenticated());
            log.info("Authorities: {}", authentication.getAuthorities());

            //neu xac thuc thanh cong, luu thong tin vao SecurityContext
            authorities.add(authentication.getAuthorities().toString());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException | DisabledException e) {
            log.error("Failed to authenticate user: {}", request.getUsername());
            throw new AccessDeniedException("Access denied!!! Invalid username or password " + e.getMessage());
        }


        String accessToken = jwtService.generateAccessToken(request.getUsername(), authorities);
        String refreshToken = jwtService.generateRefreshToken(request.getUsername(), authorities);

        tokenService.save(TokenEntity.builder().username(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());

        return TokenResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Logout
     *
     * @param request
     * @return
     */
    public String removeToken(HttpServletRequest request) {
        log.info("---------- removeToken ----------");

        final String token = request.getHeader("Authorization"); // đổi header
        if (StringUtils.isBlank(token)) {
            throw new InvalidDataException("Token must not be blank");
        }

        // Nếu header có dạng: Bearer <token>
        String jwtToken = token.replace("Bearer ", "").trim();

        final String userName = jwtService.extractUsername(jwtToken, ACCESS_TOKEN);
        log.info("Username: {}", userName);
        tokenService.delete(userName);
        return "Removed!";
    }

}