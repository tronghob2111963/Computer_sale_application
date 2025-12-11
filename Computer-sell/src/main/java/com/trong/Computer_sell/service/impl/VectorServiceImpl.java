package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.RAGChatResponse;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.repository.ProductVectorRepository;
import com.trong.Computer_sell.service.OpenAIService;
import com.trong.Computer_sell.service.VectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorServiceImpl implements VectorService {

    private final ProductRepository productRepository;
    private final ProductVectorRepository productVectorRepository;
    private final OpenAIService openAIService;

    @Override
    @Transactional
    public boolean buildProductEmbedding(ProductEntity product) {
        try {
            String textContent = buildProductText(product);
            float[] embedding = openAIService.createEmbedding(textContent);
            String embeddingStr = floatArrayToString(embedding);

            String categoryName = product.getCategory() != null ? product.getCategory().getName() : "";
            String brandName = product.getBrandId() != null ? product.getBrandId().getName() : "";
            String productTypeName = product.getProductTypeId() != null ? product.getProductTypeId().getName() : "";

            productVectorRepository.upsertProductVector(
                    UUID.randomUUID(),
                    product.getId(),
                    embeddingStr,
                    product.getName(),
                    product.getPrice() != null ? product.getPrice().doubleValue() : 0.0,
                    categoryName,
                    brandName,
                    productTypeName,
                    product.getDescription(),
                    buildSpecsText(product),
                    product.getStock(),
                    product.getWarrantyPeriod()
            );

            log.info("Built embedding for product: {}", product.getName());
            return true;

        } catch (Exception e) {
            log.error("Failed to build embedding for product {}: {}", product.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public int rebuildAllEmbeddings() {
        List<ProductEntity> products = productRepository.findAll();
        int successCount = 0;

        log.info("Starting to rebuild embeddings for {} products", products.size());

        for (ProductEntity product : products) {
            if (buildProductEmbedding(product)) {
                successCount++;
            }
            // Add small delay to avoid rate limiting
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("Completed rebuilding embeddings. Success: {}/{}", successCount, products.size());
        return successCount;
    }

    @Override
    public List<RAGChatResponse.ProductSuggestion> searchSimilarProducts(float[] queryEmbedding, int limit) {
        String embeddingStr = floatArrayToString(queryEmbedding);
        List<Object[]> results = productVectorRepository.findSimilarProducts(embeddingStr, limit);
        return mapToProductSuggestions(results);
    }

    @Override
    public List<RAGChatResponse.ProductSuggestion> searchSimilarProductsWithPriceRange(
            float[] queryEmbedding, Double minPrice, Double maxPrice, int limit) {
        String embeddingStr = floatArrayToString(queryEmbedding);
        List<Object[]> results = productVectorRepository.findSimilarProductsWithPriceRange(
                embeddingStr, minPrice, maxPrice, limit);
        return mapToProductSuggestions(results);
    }

    @Override
    @Transactional
    public void deleteProductVector(UUID productId) {
        productVectorRepository.deleteByProductId(productId);
    }

    @Override
    public long getEmbeddingCount() {
        return productVectorRepository.countWithEmbeddings();
    }

    private String buildProductText(ProductEntity product) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Tên sản phẩm: ").append(product.getName()).append(". ");
        
        if (product.getBrandId() != null) {
            sb.append("Thương hiệu: ").append(product.getBrandId().getName()).append(". ");
        }
        
        if (product.getCategory() != null) {
            sb.append("Danh mục: ").append(product.getCategory().getName()).append(". ");
        }
        
        if (product.getProductTypeId() != null) {
            sb.append("Loại sản phẩm: ").append(product.getProductTypeId().getName()).append(". ");
        }
        
        if (product.getPrice() != null) {
            sb.append("Giá: ").append(formatPrice(product.getPrice())).append(" VNĐ. ");
        }
        
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            sb.append("Mô tả: ").append(product.getDescription()).append(". ");
        }
        
        if (product.getWarrantyPeriod() != null) {
            sb.append("Bảo hành: ").append(product.getWarrantyPeriod()).append(" tháng. ");
        }
        
        if (product.getStock() != null) {
            sb.append("Tồn kho: ").append(product.getStock()).append(" sản phẩm. ");
        }

        return sb.toString();
    }

    private String buildSpecsText(ProductEntity product) {
        // This can be extended to include product specifications
        return "";
    }

    private String formatPrice(BigDecimal price) {
        return String.format("%,.0f", price);
    }

    private String floatArrayToString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private List<RAGChatResponse.ProductSuggestion> mapToProductSuggestions(List<Object[]> results) {
        List<RAGChatResponse.ProductSuggestion> suggestions = new ArrayList<>();
        
        for (Object[] row : results) {
            try {
                RAGChatResponse.ProductSuggestion suggestion = RAGChatResponse.ProductSuggestion.builder()
                        .id(row[1] != null ? row[1].toString() : null) // product_id
                        .name(row[2] != null ? row[2].toString() : null) // name
                        .price(row[3] != null ? ((Number) row[3]).doubleValue() : null) // price
                        .category(row[4] != null ? row[4].toString() : null) // category
                        .brand(row[5] != null ? row[5].toString() : null) // brand
                        .description(row[7] != null ? row[7].toString() : null) // description
                        .stock(row[9] != null ? ((Number) row[9]).intValue() : null) // stock
                        .warrantyPeriod(row[10] != null ? ((Number) row[10]).intValue() : null) // warranty
                        .similarityScore(row[13] != null ? ((Number) row[13]).doubleValue() : null) // similarity
                        .build();
                suggestions.add(suggestion);
            } catch (Exception e) {
                log.warn("Error mapping product suggestion: {}", e.getMessage());
            }
        }
        
        return suggestions;
    }
}
