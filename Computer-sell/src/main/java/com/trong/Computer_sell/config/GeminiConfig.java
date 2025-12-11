package com.trong.Computer_sell.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiConfig {
    
    private String apiKey;
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private String chatModel = "gemini-2.0-flash";
    private String embeddingModel = "text-embedding-004";
    private int maxTokens = 2048;
    private double temperature = 0.7;
}
