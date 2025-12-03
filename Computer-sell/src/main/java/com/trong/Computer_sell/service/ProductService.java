package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.product.ProductRequestDTO;
import com.trong.Computer_sell.DTO.request.product.ProductUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.product.ProductDetailResponseDTO;
import com.trong.Computer_sell.common.ProductStatus;

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

    // Soft delete - chuyển trạng thái sản phẩm thay vì xóa cứng
    void softDeleteProduct(UUID id);

    // Khôi phục sản phẩm đã xóa
    void restoreProduct(UUID id);

    // Cập nhật trạng thái sản phẩm
    void updateProductStatus(UUID id, ProductStatus status);
}
