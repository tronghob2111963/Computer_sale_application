package com.trong.Computer_sell.config;

import com.trong.Computer_sell.common.TokenType;
import com.trong.Computer_sell.service.JwtService;
import com.trong.Computer_sell.service.UserServiceDetail;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;


@Component
@RequiredArgsConstructor
@Slf4j(topic = "CUSTOMIZE-REQUEST-FILTER")
@EnableMethodSecurity(prePostEnabled = true)
public class CustomizeRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserServiceDetail userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Customize request filter - {} {}", request.getMethod(), request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            authHeader = authHeader.substring(7);
            log.info("Token found, extracting username...");
            String username = "";
            try {
                username = jwtService.extractUsername(authHeader, TokenType.ACCESS_TOKEN);
                log.info("✅ Username extracted: {}", username);
            }catch (Exception e) {
                log.error("❌ Token validation failed: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"Access denied - Invalid or expired token\"}");
                return;
            }

            try {
                UserDetails userDetails = userDetailsService.UserServiceDetail().loadUserByUsername(username);
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);
                log.info("✅ Authentication set for user: {}", username);
            } catch (Exception e) {
                log.error("❌ User loading failed: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"Access denied - User not found\"}");
                return;
            }
            
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("No Authorization header found");
        filterChain.doFilter(request, response);

    }

    @Setter
    @Getter
    private class ErrorResponse {
        private Date timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
    }
}