package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.RAGChatRequest;
import com.trong.Computer_sell.DTO.response.RAGChatResponse;
import com.trong.Computer_sell.service.RAGService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RAG Chat", description = "AI Chatbot tư vấn mua máy tính với RAG")
public class RAGChatController {

    private final RAGService ragService;

    @PostMapping("/ask")
    @Operation(summary = "Gửi câu hỏi cho AI Chatbot", 
               description = "Sử dụng RAG để tư vấn sản phẩm dựa trên nhu cầu khách hàng")
    public ResponseEntity<RAGChatResponse> askQuestion(
            @RequestBody RAGChatRequest request,
            @RequestParam(required = false) String userId) {
        
        log.info("Received chat request: {}", request.getMessage());
        UUID userUuid = null;
        if (userId != null && !userId.isEmpty()) {
            try {
                userUuid = UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid userId format: {}", userId);
            }
        }
        RAGChatResponse response = ragService.chat(request, userUuid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/session")
    @Operation(summary = "Tạo phiên chat mới")
    public ResponseEntity<String> createSession(
            @RequestParam(required = false) String userId) {
        
        UUID userUuid = null;
        if (userId != null && !userId.isEmpty()) {
            try {
                userUuid = UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid userId format: {}", userId);
            }
        }
        String sessionId = ragService.getOrCreateSession(null, userUuid);
        return ResponseEntity.ok(sessionId);
    }
}
