package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.ChatMessageDTO;
import com.trong.Computer_sell.DTO.ChatResponseDTO;
import com.trong.Computer_sell.model.ChatLog;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.repository.ChatLogRepository;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatBotServiceImpl implements ChatBotService {

    private static final int MAX_CONTEXT_PRODUCTS = 5;
    private static final int MAX_API_RETRIES = 2;
    private static final long INITIAL_BACKOFF_MS = 2000L;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final ChatLogRepository chatLogRepository;
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    @Override
    public ChatResponseDTO chat(ChatMessageDTO message, String userId) {
        String userMessage = message.getMessage();
        List<ProductEntity> relatedProducts = findRelevantProducts(userMessage);
        String prompt = buildPrompt(userMessage, relatedProducts);
        String response;

        try {
            response = callGeminiAPI(prompt);
        } catch (HttpClientErrorException e) {
            if (HttpStatus.TOO_MANY_REQUESTS.equals(e.getStatusCode())) {
                log.warn("Gemini quota/rate limit hit, falling back to DB-only answer");
                response = buildFallbackResponse(userMessage, relatedProducts, true);
            } else {
                log.error("Gemini API call failed: ", e);
                response = buildFallbackResponse(userMessage, relatedProducts, false);
            }
        } catch (Exception e) {
            log.error("Error in chatbot: ", e);
            response = buildFallbackResponse(userMessage, relatedProducts, false);
        }

        ChatLog chatLog = ChatLog.builder()
                .userId(UUID.fromString(userId))
                .message(userMessage)
                .response(response)
                .timestamp(LocalDateTime.now())
                .build();
        chatLogRepository.save(chatLog);

        return ChatResponseDTO.builder()
                .message(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String callGeminiAPI(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=" + geminiApiKey;

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();

        part.put("text", prompt);
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Exception lastError = null;

        for (int attempt = 0; attempt <= MAX_API_RETRIES; attempt++) {
            try {
                Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
                return extractText(response);
            } catch (HttpClientErrorException e) {
                lastError = e;
                if (HttpStatus.TOO_MANY_REQUESTS.equals(e.getStatusCode()) && attempt < MAX_API_RETRIES) {
                    sleepWithBackoff(attempt);
                    continue;
                }
                throw e;
            } catch (Exception e) {
                lastError = e;
                if (attempt < MAX_API_RETRIES) {
                    sleepWithBackoff(attempt);
                    continue;
                }
                throw e;
            }
        }

        throw lastError != null ? lastError : new Exception("Gemini API call failed for unknown reasons");
    }

    @Override
    public String getProductAvailability(String productName) {
        try {
            var products = productRepository.searchUserByKeyword(productName, PageRequest.of(0, 10));

            if (products.isEmpty()) {
                return "Sản phẩm '" + productName + "' không tìm thấy trong cửa hàng.";
            }

            var product = products.getContent().get(0);
            int stock = product.getStock();

            if (stock > 0) {
                return "Sản phẩm '" + productName + "' hiện có sẵn với " + stock + " sản phẩm trong kho.";
            } else {
                return "Sản phẩm '" + productName + "' hiện hết hàng.";
            }
        } catch (Exception e) {
            log.error("Error checking product availability: ", e);
            return "Không thể kiểm tra tính khả dụng sản phẩm lúc này.";
        }
    }

    @Override
    public String getProductPrice(String productName) {
        try {
            var products = productRepository.searchUserByKeyword(productName, PageRequest.of(0, 10));

            if (products.isEmpty()) {
                return "Sản phẩm '" + productName + "' không tìm thấy.";
            }

            var product = products.getContent().get(0);
            if (product.getPrice() == null) {
                return "Không tìm thấy giá cho sản phẩm '" + productName + "'.";
            }

            return "Giá của " + productName + ": " + formatPrice(product.getPrice());
        } catch (Exception e) {
            log.error("Error getting product price: ", e);
            return "Không thể lấy giá sản phẩm lúc này.";
        }
    }

    private String buildContext() {
        long totalProducts = productRepository.count();

        return String.format(
                "Bạn là một trợ lý bán hàng cho cửa hàng bán máy tính. " +
                        "Cửa hàng hiện có %d sản phẩm. " +
                        "Bạn có thể giúp khách hàng kiểm tra tình khả dụng sản phẩm, " +
                        "trả lời câu hỏi về giá cả, và cung cấp thông tin sản phẩm. " +
                        "Luôn trả lời bằng tiếng Việt.",
                totalProducts
        );
    }

    private String buildPrompt(String userMessage, List<ProductEntity> relatedProducts) {
        String context = buildContext();
        StringBuilder productContext = new StringBuilder();

        if (!relatedProducts.isEmpty()) {
            productContext.append("\n\nThông tin sản phẩm liên quan (tối đa ")
                    .append(MAX_CONTEXT_PRODUCTS)
                    .append(" mục):\n");
            relatedProducts.forEach(product -> productContext.append(formatProductLine(product)).append("\n"));
        } else {
            productContext.append("\n\nKhông tìm thấy sản phẩm trùng khớp rõ ràng trong kho. ")
                    .append("Nếu câu trả lời cần thông tin sản phẩm, hãy hỏi lại người dùng để biết tên/mã sản phẩm cụ thể.");
        }

        return context +
                productContext +
                "\n\nCâu hỏi của khách hàng: " + userMessage +
                "\n\nHãy trả lời thân thiện, ngắn gọn, chỉ dựa trên thông tin đã cung cấp. " +
                "Nếu không chắc chắn, hãy đề nghị khách hàng cung cấp tên sản phẩm cụ thể.";
    }

    private List<ProductEntity> findRelevantProducts(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return List.of();
        }
        try {
            var page = productRepository.searchUserByKeyword(userMessage, PageRequest.of(0, MAX_CONTEXT_PRODUCTS));
            return page.getContent();
        } catch (Exception e) {
            log.error("Could not load related products for context: ", e);
            return List.of();
        }
    }

    private String buildFallbackResponse(String userMessage, List<ProductEntity> relatedProducts, boolean quotaExceeded) {
        StringBuilder message = new StringBuilder();
        if (quotaExceeded) {
            message.append("Xin lỗi, hệ thống AI đang tạm hết hạn mức. ");
        } else {
            message.append("Xin lỗi, hiện tôi không thể kết nối tới dịch vụ AI. ");
        }

        if (!relatedProducts.isEmpty()) {
            message.append("Dưới đây là thông tin sản phẩm tìm được liên quan đến '")
                    .append(userMessage)
                    .append("':\n");
            relatedProducts.forEach(product -> message.append(formatProductLine(product)).append("\n"));
            message.append("Bạn có thể hỏi lại chi tiết hơn hoặc chỉ định tên/mã sản phẩm cụ thể.");
        } else {
            message.append("Vui lòng thử lại sau hoặc cung cấp tên/mã sản phẩm để tôi tra cứu trong kho.");
        }

        return message.toString().trim();
    }

    private String formatProductLine(ProductEntity product) {
        String brand = product.getBrandId() != null ? product.getBrandId().getName() : "Không rõ thương hiệu";
        String category = product.getCategory() != null ? product.getCategory().getName() : "Không rõ danh mục";
        String price = formatPrice(product.getPrice());
        String stock = product.getStock() != null && product.getStock() > 0
                ? product.getStock() + " chiếc"
                : "Hết hàng";

        return "- " + product.getName() +
                " | Thương hiệu: " + brand +
                " | Danh mục: " + category +
                " | Giá: " + price +
                " | Tồn kho: " + stock;
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Chưa cập nhật";
        }
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(price) + " VND";
    }

    private String extractText(Map<String, Object> response) throws Exception {
        if (response == null) {
            throw new Exception("Empty response from Gemini API");
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new Exception("No candidates in response");
        }

        Map<String, Object> candidate = candidates.get(0);
        Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
        if (contentMap == null) {
            throw new Exception("Missing content in response");
        }

        List<Map<String, String>> parts = (List<Map<String, String>>) contentMap.get("parts");
        if (parts == null || parts.isEmpty()) {
            throw new Exception("No parts in response");
        }

        return parts.get(0).get("text");
    }

    private void sleepWithBackoff(int attempt) {
        long backoff = INITIAL_BACKOFF_MS * (attempt + 1);
        try {
            Thread.sleep(backoff);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
