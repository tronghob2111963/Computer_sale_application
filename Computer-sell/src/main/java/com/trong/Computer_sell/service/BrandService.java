package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.BrandRequestDTO;
import com.trong.Computer_sell.DTO.response.BrandResponseDTO;
import com.trong.Computer_sell.DTO.response.PageResponse;

import java.util.UUID;

public interface BrandService {
        UUID saveBrand(BrandRequestDTO brand);
        BrandResponseDTO getBrandById(UUID id);
        PageResponse getAllBrands(String keyword, int pageNo, int pageSize, String sortBy);
}
