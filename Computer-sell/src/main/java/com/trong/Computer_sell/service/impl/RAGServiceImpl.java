package com.trong.Computer_sell.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trong.Computer_sell.DTO.request.RAGChatRequest;
import com.trong.Computer_sell.DTO.response.RAGChatResponse;
import com.trong.Computer_sell.model.ChatMessageEntity;
import com.trong.Computer_sell.model.ChatSessionEntity;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.repository.ChatMessageRepository;
import com.trong.Computer_sell.repository.ChatSessionRepository;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.service.OpenAIService;
import com.trong.Computer_sell.service.RAGService;
import com.trong.Computer_sell.service.VectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGServiceImpl implements RAGService {

    private final OpenAIService openAIService;
    private final VectorService vectorService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_SIMILAR_PRODUCTS = 5;
    private static final int MAX_CONVERSATION_HISTORY = 10;

    @Override
    @Transactional
    public RAGChatResponse chat(RAGChatRequest request, UUID userId) {
        try {
            // 1. Get or create session
            String sessionId = getOrCreateSession(request.getSessionId(), userId);
            ChatSessionEntity session = chatSessionRepository.findBySessionToken(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            // 2. Extract budget from user message
            PriceRange priceRange = extractPriceRange(request.getMessage());

            // 3. Create embedding for user query
            float[] queryEmbedding = openAIService.createEmbedding(request.getMessage());

            // 4. Search similar products
            List<RAGChatResponse.ProductSuggestion> similarProducts;
            if (priceRange != null) {
                similarProducts = vectorService.searchSimilarProductsWithPriceRange(
                        queryEmbedding, priceRange.min, priceRange.max, MAX_SIMILAR_PRODUCTS);
            } else {
                similarProducts = vectorService.searchSimilarProducts(queryEmbedding, MAX_SIMILAR_PRODUCTS);
            }

            // 5. Get conversation history
            List<OpenAIService.ChatMessage> conversationHistory = getConversationHistory(session.getId());

            // 6. Build RAG prompt
            String systemPrompt = buildRAGSystemPrompt(similarProducts);

            // 7. Get AI response
            String aiResponse = openAIService.chatCompletionWithHistory(
                    systemPrompt, conversationHistory, request.getMessage());

            // 8. Save messages
            saveMessage(session, "user", request.getMessage(), null);
            saveMessage(session, "assistant", aiResponse, similarProducts);

            // 9. Enrich products with image URLs
            enrichProductsWithImages(similarProducts);

            return RAGChatResponse.builder()
                    .answer(aiResponse)
                    .products(similarProducts)
                    .sessionId(sessionId)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error in RAG chat: {}", e.getMessage(), e);
            return RAGChatResponse.builder()
                    .answer("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.")
                    .products(Collections.emptyList())
                    .sessionId(request.getSessionId())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    @Transactional
    public String getOrCreateSession(String sessionId, UUID userId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            Optional<ChatSessionEntity> existing = chatSessionRepository.findBySessionToken(sessionId);
            if (existing.isPresent()) {
                return sessionId;
            }
        }

        // Create new session
        String newSessionId = UUID.randomUUID().toString();
        ChatSessionEntity session = ChatSessionEntity.builder()
                .sessionToken(newSessionId)
                .userId(userId)
                .build();
        chatSessionRepository.save(session);
        
        return newSessionId;
    }

    private String buildRAGSystemPrompt(List<RAGChatResponse.ProductSuggestion> products) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
            Bạn là trợ lý tư vấn mua máy tính chuyên nghiệp của cửa hàng THComputer.
            
            NHIỆM VỤ:
            - Tư vấn khách hàng chọn sản phẩm phù hợp với nhu cầu và ngân sách
            - Phân tích nhu cầu: gaming, học tập, văn phòng, đồ họa, lập trình...
            - Giải thích thông số kỹ thuật một cách dễ hiểu
            - So sánh các sản phẩm khi được yêu cầu
            - Trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp
            
            QUY TẮC:
            1. Chỉ gợi ý sản phẩm từ danh sách được cung cấp bên dưới
            2. Nếu không có sản phẩm phù hợp, hãy thông báo và đề xuất liên hệ cửa hàng
            3. Luôn đề cập giá và tình trạng còn hàng
            4. Với câu hỏi không liên quan đến máy tính, lịch sự từ chối và hướng dẫn về sản phẩm
            
            DANH SÁCH SẢN PHẨM LIÊN QUAN:
            """);

        if (products.isEmpty()) {
            prompt.append("Không tìm thấy sản phẩm phù hợp trong kho.\n");
        } else {
            for (int i = 0; i < products.size(); i++) {
                RAGChatResponse.ProductSuggestion p = products.get(i);
                prompt.append(String.format("""
                    
                    %d. %s
                       - Thương hiệu: %s
                       - Danh mục: %s
                       - Giá: %,.0f VNĐ
                       - Tồn kho: %d sản phẩm
                       - Bảo hành: %d tháng
                       - Mô tả: %s
                    """,
                        i + 1,
                        p.getName(),
                        p.getBrand() != null ? p.getBrand() : "N/A",
                        p.getCategory() != null ? p.getCategory() : "N/A",
                        p.getPrice() != null ? p.getPrice() : 0,
                        p.getStock() != null ? p.getStock() : 0,
                        p.getWarrantyPeriod() != null ? p.getWarrantyPeriod() : 0,
                        p.getDescription() != null ? p.getDescription() : "Không có mô tả"
                ));
            }
        }

        prompt.append("""
            
            ĐỊNH DẠNG TRẢ LỜI:
            - Sử dụng markdown để format
            - Đánh số sản phẩm gợi ý
            - Giải thích lý do gợi ý
            - Kết thúc bằng câu hỏi để hiểu thêm nhu cầu khách hàng
            """);

        return prompt.toString();
    }

    private List<OpenAIService.ChatMessage> getConversationHistory(UUID sessionId) {
        List<ChatMessageEntity> messages = chatMessageRepository.findRecentMessages(
                sessionId, MAX_CONVERSATION_HISTORY);
        
        // Reverse to get chronological order
        Collections.reverse(messages);
        
        return messages.stream()
                .map(m -> new OpenAIService.ChatMessage(m.getRole(), m.getContent()))
                .collect(Collectors.toList());
    }

    private void saveMessage(ChatSessionEntity session, String role, String content, 
                            List<RAGChatResponse.ProductSuggestion> products) {
        try {
            String productsJson = null;
            if (products != null && !products.isEmpty()) {
                List<String> productIds = products.stream()
                        .map(RAGChatResponse.ProductSuggestion::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                productsJson = objectMapper.writeValueAsString(productIds);
            }

            ChatMessageEntity message = ChatMessageEntity.builder()
                    .session(session)
                    .role(role)
                    .content(content)
                    .productsSuggested(productsJson)
                    .build();
            chatMessageRepository.save(message);
        } catch (Exception e) {
            log.warn("Failed to save chat message: {}", e.getMessage());
        }
    }

    private PriceRange extractPriceRange(String message) {
        // Pattern for Vietnamese currency mentions
        Pattern[] patterns = {
                // "từ X đến Y triệu"
                Pattern.compile("từ\\s*(\\d+(?:[.,]\\d+)?)\\s*(?:đến|tới|-)?\\s*(\\d+(?:[.,]\\d+)?)\\s*triệu", Pattern.CASE_INSENSITIVE),
                // "dưới X triệu"
                Pattern.compile("dưới\\s*(\\d+(?:[.,]\\d+)?)\\s*triệu", Pattern.CASE_INSENSITIVE),
                // "trên X triệu"
                Pattern.compile("trên\\s*(\\d+(?:[.,]\\d+)?)\\s*triệu", Pattern.CASE_INSENSITIVE),
                // "khoảng X triệu"
                Pattern.compile("khoảng\\s*(\\d+(?:[.,]\\d+)?)\\s*triệu", Pattern.CASE_INSENSITIVE),
                // "X triệu"
                Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*triệu", Pattern.CASE_INSENSITIVE),
                // "ngân sách X"
                Pattern.compile("ngân\\s*sách\\s*(\\d+(?:[.,]\\d+)?)\\s*(?:triệu)?", Pattern.CASE_INSENSITIVE)
        };

        for (int i = 0; i < patterns.length; i++) {
            Matcher matcher = patterns[i].matcher(message);
            if (matcher.find()) {
                double value1 = parseVietnameseNumber(matcher.group(1));
                
                switch (i) {
                    case 0: // từ X đến Y
                        double value2 = parseVietnameseNumber(matcher.group(2));
                        return new PriceRange(value1 * 1_000_000, value2 * 1_000_000);
                    case 1: // dưới X
                        return new PriceRange(0.0, value1 * 1_000_000);
                    case 2: // trên X
                        return new PriceRange(value1 * 1_000_000, Double.MAX_VALUE);
                    case 3: // khoảng X (±20%)
                    case 4: // X triệu
                    case 5: // ngân sách X
                        double base = value1 * 1_000_000;
                        return new PriceRange(base * 0.8, base * 1.2);
                }
            }
        }
        
        return null;
    }

    private double parseVietnameseNumber(String number) {
        if (number == null) return 0;
        return Double.parseDouble(number.replace(",", "."));
    }

    private void enrichProductsWithImages(List<RAGChatResponse.ProductSuggestion> products) {
        for (RAGChatResponse.ProductSuggestion product : products) {
            if (product.getId() != null) {
                try {
                    UUID productId = UUID.fromString(product.getId());
                    productRepository.findById(productId).ifPresent(entity -> {
                        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
                            product.setImageUrl(entity.getImages().get(0).getImageUrl());
                        }
                    });
                } catch (Exception e) {
                    log.debug("Could not enrich product image: {}", e.getMessage());
                }
            }
        }
    }

    private record PriceRange(Double min, Double max) {}
}
