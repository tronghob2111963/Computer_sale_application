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
    private String baseUrl = "https://api.groq.com/openai/v1";
    private String chatModel = "llama-3.1-70b-versatile";
    private int maxTokens = 2048;
    private double temperature = 0.7;
}
