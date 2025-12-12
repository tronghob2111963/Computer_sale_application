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
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
@ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
public class GeminiServiceImpl implements OpenAIService {

    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;
    private WebClient webClient;

    private static final int MAX_RETRIES = 6;
    private static final long INITIAL_RETRY_DELAY_MS = 8000; // base backoff 8s for free-tier limits
    private static final long MAX_RETRY_DELAY_MS = 60000; // do not exceed 60s wait
    private static final long MIN_DELAY_BETWEEN_CALLS_MS = 5000; // 5 seconds between API calls
    private static final Semaphore API_RATE_LIMITER = new Semaphore(1); // prevent concurrent Gemini hits
    private static volatile long nextAllowedRequestTimeMs = 0;

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
        return executeWithRetry("embedding", () -> doCreateEmbedding(text));
    }

    private void waitForRateLimit() {
        synchronized (GeminiServiceImpl.class) {
            long now = System.currentTimeMillis();
            long waitUntil = Math.max(now, nextAllowedRequestTimeMs);
            long waitTime = waitUntil - now;
            if (waitTime > 0) {
                try {
                    log.debug("Rate limiting: waiting {}ms before next API call", waitTime);
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            nextAllowedRequestTimeMs = System.currentTimeMillis() + MIN_DELAY_BETWEEN_CALLS_MS;
        }
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
        return executeWithRetry("chat completion", () -> doChatCompletion(systemPrompt, conversationHistory, userMessage));
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

    private <T> T executeWithRetry(String operationName, Callable<T> action) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt <= MAX_RETRIES) {
            try {
                return withRateLimit(action);
            } catch (WebClientResponseException.TooManyRequests e) {
                lastException = e;
                attempt++;
                long delay = calculateBackoffDelay(attempt, e);
                log.warn("{} hit Gemini rate limit (attempt {}/{}). Waiting {}ms (Retry-After: {}s)", 
                        operationName, attempt, MAX_RETRIES, delay, getRetryAfterSeconds(e));
                pushNextAllowedTime(delay);
                sleepQuietly(delay);
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().is5xxServerError()) {
                    lastException = e;
                    attempt++;
                    long delay = calculateBackoffDelay(attempt, e);
                    log.warn("{} failed with status {} (attempt {}/{}). Waiting {}ms before retry", 
                            operationName, e.getStatusCode(), attempt, MAX_RETRIES, delay);
                    pushNextAllowedTime(delay);
                    sleepQuietly(delay);
                } else {
                    throw e;
                }
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    lastException = e;
                    attempt++;
                    long delay = calculateBackoffDelay(attempt, null);
                    log.warn("{} hit Gemini rate limit (by message) attempt {}/{} - waiting {}ms", 
                            operationName, attempt, MAX_RETRIES, delay);
                    pushNextAllowedTime(delay);
                    sleepQuietly(delay);
                } else {
                    throw new RuntimeException("Failed to call Gemini " + operationName + ": " + e.getMessage(), e);
                }
            }
        }

        log.error("Failed to complete {} after {} retries", operationName, MAX_RETRIES);
        throw new RuntimeException("Gemini rate limit exceeded. Please wait a moment and try again.", lastException);
    }

    private <T> T withRateLimit(Callable<T> action) throws Exception {
        API_RATE_LIMITER.acquire();
        try {
            waitForRateLimit();
            return action.call();
        } finally {
            API_RATE_LIMITER.release();
        }
    }

    private long calculateBackoffDelay(int attempt, WebClientResponseException exception) {
        long retryAfterMs = getRetryAfterMs(exception);
        double exponential = INITIAL_RETRY_DELAY_MS * Math.pow(1.5, Math.max(0, attempt - 1));
        long baseDelay = retryAfterMs > 0 ? retryAfterMs : (long) exponential;
        long jitter = ThreadLocalRandom.current().nextLong(500, 1500);
        return Math.min(baseDelay + jitter, MAX_RETRY_DELAY_MS);
    }

    private long getRetryAfterMs(WebClientResponseException exception) {
        if (exception == null || exception.getHeaders() == null) {
            return -1;
        }
        List<String> retryAfter = exception.getHeaders().get("Retry-After");
        if (retryAfter != null && !retryAfter.isEmpty()) {
            try {
                return Long.parseLong(retryAfter.get(0)) * 1000;
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return -1;
    }

    private long getRetryAfterSeconds(WebClientResponseException exception) {
        long ms = getRetryAfterMs(exception);
        return ms > 0 ? ms / 1000 : -1;
    }

    private void pushNextAllowedTime(long additionalDelayMs) {
        synchronized (GeminiServiceImpl.class) {
            long candidate = System.currentTimeMillis() + additionalDelayMs;
            nextAllowedRequestTimeMs = Math.max(nextAllowedRequestTimeMs, candidate);
        }
    }

    private void sleepQuietly(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
