package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.ProductRequestDTO;
import com.trong.Computer_sell.DTO.request.ProductUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.PageResponse;
import com.trong.Computer_sell.DTO.response.ProductDetailResponse;
import com.trong.Computer_sell.DTO.response.ProductResponseDTO;

import java.util.UUID;

public interface ProductService {
    UUID createProduct(ProductRequestDTO productRequestDTO);
    UUID updateProduct(ProductUpdateRequestDTO dto);
    UUID deleteProduct(UUID id);
    ProductDetailResponse getProductById(UUID id);
    PageResponse<?> getAllProducts(String keyword, int pageNo, int pageSize, String sortBy);
    PageResponse<?> getAllProductsByBrandId(UUID brandId, String keyword, int pageNo, int pageSize, String sortBy);
    PageResponse<?> getAllProductsByCategoryId(UUID categoryId, String keyword, int pageNo, int pageSize, String sortBy);

}
