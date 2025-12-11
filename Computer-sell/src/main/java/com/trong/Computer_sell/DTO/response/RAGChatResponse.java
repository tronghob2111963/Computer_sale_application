package com.trong.Computer_sell.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAGChatResponse {
    private String answer;
    private List<ProductSuggestion> products;
    private String sessionId;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSuggestion {
        private String id;
        private String name;
        private Double price;
        private String category;
        private String brand;
        private String description;
        private Integer stock;
        private Integer warrantyPeriod;
        private String imageUrl;
        private Double similarityScore;
    }
}
