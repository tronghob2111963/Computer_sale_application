package com.trong.Computer_sell.service;

import java.util.List;

public interface cd OpenAIService {
    
    /**
     * Generate embedding vector for given text
     * @param text Input text to embed
     * @return float array of embedding vector (1536 dimensions)
     */
    float[] createEmbedding(String text);
    
    /**
     * Generate chat completion response
     * @param systemPrompt System prompt for context
     * @param userMessage User's message
     * @return AI generated response
     */
    String chatCompletion(String systemPrompt, String userMessage);
    
    /**
     * Generate chat completion with conversation history
     * @param systemPrompt System prompt
     * @param conversationHistory List of previous messages
     * @param userMessage Current user message
     * @return AI generated response
     */
    String chatCompletionWithHistory(String systemPrompt, List<ChatMessage> conversationHistory, String userMessage);
    
    record ChatMessage(String role, String content) {}
}
