package com.trong.Computer_sell.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trong.Computer_sell.config.GeminiConfig;
import com.trong.Computer_sell.config.GroqConfig;
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
@ConditionalOnProperty(name = "ai.provider", havingValue = "groq")
public class GroqServiceImpl implements OpenAIService {

    private final GroqConfig groqConfig;
    private final GeminiConfig geminiConfig; // For embeddings (Groq doesn't have embedding API)
    private final ObjectMapper objectMapper;
    
    private WebClient groqWebClient;
    private WebClient geminiWebClient;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private WebClient getGroqWebClient() {
        if (groqWebClient == null) {
            groqWebClient = WebClient.builder()
                    .baseUrl(groqConfig.getBaseUrl())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + groqConfig.getApiKey())
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
        }
        return groqWebClient;
    }

    private WebClient getGeminiWebClient() {
        if (geminiWebClient == null) {
            geminiWebClient = WebClient.builder()
                    .baseUrl(geminiConfig.getBaseUrl())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
        }
        return geminiWebClient;
    }


    @Override
    public float[] createEmbedding(String text) {
        // Groq doesn't have embedding API, use Gemini for embeddings
        int retries = 0;
        Exception lastException = null;

        while (retries < MAX_RETRIES) {
            try {
                return doCreateEmbeddingWithGemini(text);
            } catch (WebClientResponseException.TooManyRequests e) {
                lastException = e;
                retries++;
                log.warn("Gemini embedding rate limited. Retry {}/{}", retries, MAX_RETRIES);
                sleep(RETRY_DELAY_MS * retries);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    lastException = e;
                    retries++;
                    sleep(RETRY_DELAY_MS * retries);
                } else {
                    throw new RuntimeException("Failed to create embedding: " + e.getMessage(), e);
                }
            }
        }
        throw new RuntimeException("Embedding rate limit exceeded", lastException);
    }

    private float[] doCreateEmbeddingWithGemini(String text) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            ObjectNode content = requestBody.putObject("content");
            ArrayNode parts = content.putArray("parts");
            parts.addObject().put("text", text);

            String url = "/models/" + geminiConfig.getEmbeddingModel() + ":embedContent?key=" + geminiConfig.getApiKey();

            String response = getGeminiWebClient()
                    .post()
                    .uri(url)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode embeddingArray = responseJson.path("embedding").path("values");

            if (embeddingArray.isEmpty()) {
                throw new RuntimeException("Empty embedding from Gemini");
            }

            float[] embedding = new float[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                embedding[i] = (float) embeddingArray.get(i).asDouble();
            }

            log.debug("Created Gemini embedding with {} dimensions", embedding.length);
            return embedding;

        } catch (WebClientResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create embedding: " + e.getMessage(), e);
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
                log.warn("Groq rate limited. Retry {}/{}", retries, MAX_RETRIES);
                sleep(RETRY_DELAY_MS * retries);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    lastException = e;
                    retries++;
                    sleep(RETRY_DELAY_MS * retries);
                } else {
                    throw new RuntimeException("Failed to get chat completion: " + e.getMessage(), e);
                }
            }
        }
        throw new RuntimeException("Groq rate limit exceeded", lastException);
    }


    private String doChatCompletion(String systemPrompt, List<ChatMessage> conversationHistory, String userMessage) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", groqConfig.getChatModel());
            requestBody.put("temperature", groqConfig.getTemperature());
            requestBody.put("max_tokens", groqConfig.getMaxTokens());

            ArrayNode messages = requestBody.putArray("messages");

            // System message - truncate if too long (Groq has context limits)
            String truncatedSystemPrompt = systemPrompt;
            if (systemPrompt != null && systemPrompt.length() > 8000) {
                truncatedSystemPrompt = systemPrompt.substring(0, 8000) + "\n...(truncated)";
                log.warn("System prompt truncated from {} to 8000 chars", systemPrompt.length());
            }
            
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", truncatedSystemPrompt != null ? truncatedSystemPrompt : "You are a helpful assistant.");

            // Conversation history
            for (ChatMessage msg : conversationHistory) {
                ObjectNode historyMsg = messages.addObject();
                historyMsg.put("role", msg.role());
                historyMsg.put("content", msg.content());
            }

            // User message
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            log.info("Calling Groq API with model: {}", groqConfig.getChatModel());
            log.debug("Request body: {}", requestBody.toString());

            String response = getGroqWebClient()
                    .post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Groq API error {}: {}", clientResponse.statusCode(), body);
                                return clientResponse.createException();
                            });
                    })
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);

            if (responseJson.has("error")) {
                String errorMsg = responseJson.path("error").path("message").asText();
                log.error("Groq API error: {}", errorMsg);
                throw new RuntimeException("Groq API error: " + errorMsg);
            }

            String content = responseJson.path("choices").get(0)
                    .path("message").path("content").asText();

            log.debug("Groq chat completion successful");
            return content;

        } catch (WebClientResponseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in Groq chat completion: {}", e.getMessage());
            throw new RuntimeException("Failed to get chat completion from Groq: " + e.getMessage(), e);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
