package com.trong.Computer_sell.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trong.Computer_sell.config.OpenAIConfig;
import com.trong.Computer_sell.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai", matchIfMissing = false)
public class OpenAIServiceImpl implements OpenAIService {

    private final OpenAIConfig openAIConfig;
    private final ObjectMapper objectMapper;
    private WebClient webClient;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(openAIConfig.getBaseUrl())
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAIConfig.getApiKey())
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
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("model", openAIConfig.getEmbeddingModel());
                requestBody.put("input", text);
                requestBody.put("dimensions", openAIConfig.getEmbeddingDimension());

                String response = getWebClient()
                        .post()
                        .uri("/embeddings")
                        .bodyValue(requestBody.toString())
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                            if (clientResponse.statusCode().value() == 429) {
                                log.warn("Rate limited by OpenAI, will retry...");
                            }
                            return clientResponse.createException();
                        })
                        .bodyToMono(String.class)
                        .block();

                JsonNode responseJson = objectMapper.readTree(response);
                JsonNode embeddingArray = responseJson.path("data").get(0).path("embedding");

                float[] embedding = new float[embeddingArray.size()];
                for (int i = 0; i < embeddingArray.size(); i++) {
                    embedding[i] = (float) embeddingArray.get(i).asDouble();
                }

                log.debug("Created embedding with {} dimensions", embedding.length);
                return embedding;

            } catch (WebClientResponseException.TooManyRequests e) {
                lastException = e;
                retries++;
                log.warn("Rate limited (429). Retry {}/{} after {}ms", retries, MAX_RETRIES, RETRY_DELAY_MS * retries);
                try {
                    Thread.sleep(RETRY_DELAY_MS * retries);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for retry", ie);
                }
            } catch (Exception e) {
                log.error("Error creating embedding: {}", e.getMessage());
                throw new RuntimeException("Failed to create embedding: " + e.getMessage(), e);
            }
        }

        log.error("Failed to create embedding after {} retries", MAX_RETRIES);
        throw new RuntimeException("Failed to create embedding after retries. OpenAI rate limit exceeded. Please wait a moment and try again.", lastException);
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
                ObjectNode requestBody = objectMapper.createObjectNode();
                requestBody.put("model", openAIConfig.getChatModel());
                requestBody.put("max_tokens", openAIConfig.getMaxTokens());
                requestBody.put("temperature", openAIConfig.getTemperature());

                ArrayNode messages = requestBody.putArray("messages");

                // System message
                ObjectNode systemMsg = messages.addObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);

                // Conversation history
                for (ChatMessage msg : conversationHistory) {
                    ObjectNode historyMsg = messages.addObject();
                    historyMsg.put("role", msg.role());
                    historyMsg.put("content", msg.content());
                }

                // Current user message
                ObjectNode userMsg = messages.addObject();
                userMsg.put("role", "user");
                userMsg.put("content", userMessage);

                String response = getWebClient()
                        .post()
                        .uri("/chat/completions")
                        .bodyValue(requestBody.toString())
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                            if (clientResponse.statusCode().value() == 429) {
                                log.warn("Rate limited by OpenAI, will retry...");
                            }
                            return clientResponse.createException();
                        })
                        .bodyToMono(String.class)
                        .block();

                JsonNode responseJson = objectMapper.readTree(response);
                String content = responseJson.path("choices").get(0).path("message").path("content").asText();

                log.debug("Chat completion successful");
                return content;

            } catch (WebClientResponseException.TooManyRequests e) {
                lastException = e;
                retries++;
                log.warn("Rate limited (429). Retry {}/{} after {}ms", retries, MAX_RETRIES, RETRY_DELAY_MS * retries);
                try {
                    Thread.sleep(RETRY_DELAY_MS * retries);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for retry", ie);
                }
            } catch (Exception e) {
                log.error("Error in chat completion: {}", e.getMessage());
                throw new RuntimeException("Failed to get chat completion: " + e.getMessage(), e);
            }
        }

        log.error("Failed to get chat completion after {} retries", MAX_RETRIES);
        throw new RuntimeException("Failed to get chat completion after retries. OpenAI rate limit exceeded. Please wait a moment and try again.", lastException);
    }
}
