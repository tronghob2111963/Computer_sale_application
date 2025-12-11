package com.trong.Computer_sell.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
@Getter
@Setter
public class OpenAIConfig {
    private String apiKey;
    private String embeddingModel = "text-embedding-3-large";
    private String chatModel = "gpt-4o-mini";
    private String baseUrl = "https://api.openai.com/v1";
    private int embeddingDimension = 1536;
    private int maxTokens = 2000;
    private double temperature = 0.7;
}
