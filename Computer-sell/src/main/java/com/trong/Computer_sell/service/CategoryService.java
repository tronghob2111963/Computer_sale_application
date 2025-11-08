package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.product.BrandRequestDTO;
import com.trong.Computer_sell.DTO.response.category.CategoryResponseDTO;
import com.trong.Computer_sell.DTO.response.common.PageResponse;

import java.util.UUID;

public interface CategoryService {
    UUID saveCategory(BrandRequestDTO brand);
    CategoryResponseDTO getCategoryById(UUID id);
    PageResponse getAllCategories(String keyword, int pageNo, int pageSize, String sortBy);
}
