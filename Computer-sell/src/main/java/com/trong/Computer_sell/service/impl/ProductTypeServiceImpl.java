package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.product.ProductTypeResponseDTO;
import com.trong.Computer_sell.model.ProductTypeEntity;
import com.trong.Computer_sell.repository.ProductTypeRepository;
import com.trong.Computer_sell.service.ProductTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository productTypeRepository;
    @Override
    public List<ProductTypeResponseDTO> getAllProductType() {
        log.info("Getting all product types");
        List<ProductTypeEntity> productTypes = productTypeRepository.findAll();
        List<ProductTypeResponseDTO> productTypeResponses = productTypes.stream()
                .map(productType -> ProductTypeResponseDTO.builder()
                        .id(productType.getId())
                        .name(productType.getName())
                        .build())
                .toList();
        return productTypeResponses;
    }
}
