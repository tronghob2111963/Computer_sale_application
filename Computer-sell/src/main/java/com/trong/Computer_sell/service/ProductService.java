package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.product.ProductRequestDTO;
import com.trong.Computer_sell.DTO.request.product.ProductUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.product.ProductDetailResponseDTO;

import java.util.UUID;

public interface ProductService {
    UUID createProduct(ProductRequestDTO productRequestDTO);
    UUID updateProduct(ProductUpdateRequestDTO dto);
    UUID deleteProduct(UUID id);
    ProductDetailResponseDTO getProductById(UUID id);
    PageResponse<?> getAllProducts(String keyword, int pageNo, int pageSize, String sortBy);
    PageResponse<?> getAllProductsByBrandId(UUID brandId, String keyword, int pageNo, int pageSize, String sortBy);
    PageResponse<?> getAllProductsByCategoryId(UUID categoryId, String keyword, int pageNo, int pageSize, String sortBy);
    PageResponse<?> getAllProductByProductTypeId(UUID productTypeId, String keyword, int pageNo, int pageSize, String sortBy);

}
