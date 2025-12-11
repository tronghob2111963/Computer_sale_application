package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.RAGChatResponse;
import com.trong.Computer_sell.model.ProductEntity;

import java.util.List;
import java.util.UUID;

public interface VectorService {
    
    /**
     * Build and save embedding for a single product
     * @param product Product entity
     * @return true if successful
     */
    boolean buildProductEmbedding(ProductEntity product);
    
    /**
     * Rebuild all product embeddings
     * @return number of successfully processed products
     */
    int rebuildAllEmbeddings();
    
    /**
     * Search similar products using vector similarity
     * @param queryEmbedding Query embedding vector
     * @param limit Maximum number of results
     * @return List of similar products
     */
    List<RAGChatResponse.ProductSuggestion> searchSimilarProducts(float[] queryEmbedding, int limit);
    
    /**
     * Search similar products with price filter
     * @param queryEmbedding Query embedding vector
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @param limit Maximum number of results
     * @return List of similar products
     */
    List<RAGChatResponse.ProductSuggestion> searchSimilarProductsWithPriceRange(
            float[] queryEmbedding, Double minPrice, Double maxPrice, int limit);
    
    /**
     * Delete product vector
     * @param productId Product ID
     */
    void deleteProductVector(UUID productId);
    
    /**
     * Get count of products with embeddings
     * @return count
     */
    long getEmbeddingCount();
}
