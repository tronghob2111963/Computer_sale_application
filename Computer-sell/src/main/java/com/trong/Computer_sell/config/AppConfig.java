package com.trong.Computer_sell.config;


import com.sendgrid.SendGrid;
import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@RequiredArgsConstructor

public class AppConfig {

    @Value("${spring.sendgrid.api-key}")
    private String apiKey;
    //khoi tao spring web security
    private String[] WHITELIST = {"/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/auth/**","/user/**"};
    @Bean
    public SecurityFilterChain configure(@NonNull HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/**").permitAll() /// cho phép truy cập vào các đường dẫn này mà không cần xác thực
                                ) //Tất cả các yêu cầu khác cần xác thực
                .sessionManagement(
                        management ->
                        management.sessionCreationPolicy(STATELESS));
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return webSecurity ->
                webSecurity.ignoring()
                        .requestMatchers("/actuator/**", "/v3/**", "/webjars/**", "/swagger-ui*/*swagger-initializer.js", "/swagger-ui*/**");
    }

    //config spring web configuer

    //khoi tao bean cho password endcoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //sendgrid
    @Bean
    public SendGrid sendEmail(){
        return new SendGrid(apiKey);
    }

}
