package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.product.BrandRequestDTO;
import com.trong.Computer_sell.DTO.response.brand.BrandResponseDTO;
import com.trong.Computer_sell.DTO.response.common.PageResponse;

import java.util.UUID;

public interface BrandService {
        UUID saveBrand(BrandRequestDTO brand);
        BrandResponseDTO getBrandById(UUID id);
        PageResponse getAllBrands(String keyword, int pageNo, int pageSize, String sortBy);
}
