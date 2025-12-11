package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.RAGChatRequest;
import com.trong.Computer_sell.DTO.response.RAGChatResponse;

import java.util.UUID;

public interface RAGService {

    /**
     * Process RAG chat request
     * @param request Chat request with message and optional session ID
     * @param userId Optional user ID (UUID) for authenticated users
     * @return Chat response with answer and product suggestions
     */
    RAGChatResponse chat(RAGChatRequest request, UUID userId);

    /**
     * Get or create chat session
     * @param sessionId Optional existing session ID
     * @param userId Optional user ID (UUID)
     * @return Session ID
     */
    String getOrCreateSession(String sessionId, UUID userId);
}
