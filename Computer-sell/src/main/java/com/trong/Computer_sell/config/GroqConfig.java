package com.trong.Computer_sell.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "groq")
@Getter
@Setter
public class GroqConfig {
    
    private String apiKey;
    private String baseUrl;
    private String chatModel;
    private int maxTokens;
    private double temperature;
}
