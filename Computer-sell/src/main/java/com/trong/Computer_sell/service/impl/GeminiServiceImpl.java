package com.trong.Computer_sell.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trong.Computer_sell.config.GeminiConfig;
import com.trong.Computer_sell.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiServiceImpl implements OpenAIService {

    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;
    private WebClient webClient;

    private static final int MAX_RETRIES = 20;
    private static final long INITIAL_RETRY_DELAY_MS = 5000; // 5 seconds

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(geminiConfig.getBaseUrl())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
        }
        return webClient;
    }

    @Override
    public float[] createEmbedding(String text) {
        int retries = 0;
        Exception lastException = null;

        while (retries < MAX_RETRIES) {
            try {
                return doCreateEmbedding(text);
            } catch (WebClientResponseException.TooManyRequests e) {
                lastException = e;
                retries++;
                long delay = INITIAL_RETRY_DELAY_MS * retries;
                log.warn("Gemini rate limited (429). Retry {}/{} after {}ms", retries, MAX_RETRIES, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for retry", ie);
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    lastException = e;
                    retries++;
                    long delay = INITIAL_RETRY_DELAY_MS * retries;
                    log.warn("Gemini rate limited. Retry {}/{} after {}ms", retries, MAX_RETRIES, delay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting for retry", ie);
                    }
                } else {
                    throw e;
                }
            }
        }

        log.error("Failed to create embedding after {} retries", MAX_RETRIES);
        throw new RuntimeException("Gemini rate limit exceeded. Please wait a moment and try again.", lastException);
    }

    private float[] doCreateEmbedding(String text) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            ObjectNode content = requestBody.putObject("content");
            ArrayNode parts = content.putArray("parts");
            ObjectNode part = parts.addObject();
            part.put("text", text);

            String url = "/models/" + geminiConfig.getEmbeddingModel() + ":embedContent?key=" + geminiConfig.getApiKey();

            log.info("Calling Gemini Embedding API");

            String response = getWebClient()
                    .post()
                    .uri(url)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        if (clientResponse.statusCode().value() == 429) {
                            log.warn("Rate limited by Gemini, will retry...");
                        }
                        return clientResponse.createException();
                    })
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);
            
            if (responseJson.has("error")) {
                String errorMsg = responseJson.path("error").path("message").asText();
                log.error("Gemini Embedding API error: {}", errorMsg);
                throw new RuntimeException("Gemini Embedding API error: " + errorMsg);
            }
            
            JsonNode embeddingArray = responseJson.path("embedding").path("values");

            if (embeddingArray.isEmpty()) {
                log.error("Empty embedding response: {}", response);
                throw new RuntimeException("Empty embedding from Gemini");
            }

            float[] embedding = new float[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                embedding[i] = (float) embeddingArray.get(i).asDouble();
            }

            log.debug("Created Gemini embedding with {} dimensions", embedding.length);
            return embedding;

        } catch (WebClientResponseException e) {
            throw e; // Re-throw to be handled by retry logic
        } catch (Exception e) {
            log.error("Error creating Gemini embedding: {}", e.getMessage());
            throw new RuntimeException("Failed to create embedding with Gemini: " + e.getMessage(), e);
        }
    }

    @Override
    public String chatCompletion(String systemPrompt, String userMessage) {
        return chatCompletionWithHistory(systemPrompt, List.of(), userMessage);
    }

    @Override
    public String chatCompletionWithHistory(String systemPrompt, List<ChatMessage> conversationHistory, String userMessage) {
        int retries = 0;
        Exception lastException = null;

        while (retries < MAX_RETRIES) {
            try {
                return doChatCompletion(systemPrompt, conversationHistory, userMessage);
            } catch (WebClientResponseException.TooManyRequests e) {
                lastException = e;
                retries++;
                long delay = INITIAL_RETRY_DELAY_MS * retries;
                log.warn("Gemini rate limited (429). Retry {}/{} after {}ms", retries, MAX_RETRIES, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for retry", ie);
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    lastException = e;
                    retries++;
                    long delay = INITIAL_RETRY_DELAY_MS * retries;
                    log.warn("Gemini rate limited. Retry {}/{} after {}ms", retries, MAX_RETRIES, delay);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting for retry", ie);
                    }
                } else {
                    throw e;
                }
            }
        }

        log.error("Failed to get chat completion after {} retries", MAX_RETRIES);
        throw new RuntimeException("Gemini rate limit exceeded. Please wait a moment and try again.", lastException);
    }

    private String doChatCompletion(String systemPrompt, List<ChatMessage> conversationHistory, String userMessage) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            ArrayNode contents = requestBody.putArray("contents");
            
            // Add conversation history
            for (ChatMessage msg : conversationHistory) {
                ObjectNode historyContent = contents.addObject();
                historyContent.put("role", msg.role().equals("assistant") ? "model" : "user");
                ArrayNode historyParts = historyContent.putArray("parts");
                ObjectNode historyPart = historyParts.addObject();
                historyPart.put("text", msg.content());
            }
            
            // Add current user message with system prompt prepended
            ObjectNode userContent = contents.addObject();
            userContent.put("role", "user");
            ArrayNode userParts = userContent.putArray("parts");
            ObjectNode userPart = userParts.addObject();
            String combinedMessage = "Hướng dẫn hệ thống: " + systemPrompt + "\n\nCâu hỏi của khách hàng: " + userMessage;
            userPart.put("text", combinedMessage);
            
            // Generation config
            ObjectNode generationConfig = requestBody.putObject("generationConfig");
            generationConfig.put("temperature", geminiConfig.getTemperature());
            generationConfig.put("maxOutputTokens", geminiConfig.getMaxTokens());

            String apiKey = geminiConfig.getApiKey();
            log.info("Using Gemini API key: {}...", apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) : "NULL");
            String model = geminiConfig.getChatModel();
            String url = "/models/" + model + ":generateContent?key=" + apiKey;
            
            log.info("Calling Gemini API with model {}: {}", model, geminiConfig.getBaseUrl() + url.split("\\?")[0]);
            
            String response = getWebClient()
                    .post()
                    .uri(url)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        if (clientResponse.statusCode().value() == 429) {
                            log.warn("Rate limited by Gemini, will retry...");
                        }
                        return clientResponse.createException();
                    })
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);
            
            if (responseJson.has("error")) {
                String errorMsg = responseJson.path("error").path("message").asText();
                log.error("Gemini API error: {}", errorMsg);
                throw new RuntimeException("Gemini API error: " + errorMsg);
            }
            
            JsonNode candidates = responseJson.path("candidates");
            if (candidates.isEmpty() || candidates.get(0) == null) {
                log.error("No candidates in Gemini response: {}", response);
                throw new RuntimeException("No response from Gemini");
            }
            
            String content = candidates.get(0)
                    .path("content").path("parts").get(0).path("text").asText();

            log.debug("Gemini chat completion successful");
            return content;

        } catch (WebClientResponseException e) {
            throw e; // Re-throw to be handled by retry logic
        } catch (Exception e) {
            log.error("Error in Gemini chat completion: {}", e.getMessage());
            throw new RuntimeException("Failed to get chat completion from Gemini: " + e.getMessage(), e);
        }
    }
}
