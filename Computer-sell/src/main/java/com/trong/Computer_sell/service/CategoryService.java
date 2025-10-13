package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.BrandRequestDTO;
import com.trong.Computer_sell.DTO.response.BrandResponseDTO;
import com.trong.Computer_sell.DTO.response.CategoryResponseDTO;
import com.trong.Computer_sell.DTO.response.PageResponse;

import java.util.UUID;

public interface CategoryService {
    UUID saveCategory(BrandRequestDTO brand);
    CategoryResponseDTO getCategoryById(UUID id);
    PageResponse getAllCategories(String keyword, int pageNo, int pageSize, String sortBy);
}
