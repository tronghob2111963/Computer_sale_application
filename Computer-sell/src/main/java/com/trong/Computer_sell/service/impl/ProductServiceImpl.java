package com.trong.Computer_sell.service.impl;


import com.trong.Computer_sell.DTO.request.ProductRequestDTO;
import com.trong.Computer_sell.model.BrandEntity;
import com.trong.Computer_sell.model.CategoryEntity;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.model.ProductImageEntity;
import com.trong.Computer_sell.repository.BrandRepository;
import com.trong.Computer_sell.repository.CategoryRepository;
import com.trong.Computer_sell.repository.ProductImageRepository;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.service.LocalImageService;
import com.trong.Computer_sell.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j(topic = "PRODUCT-SERVICE")
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final LocalImageService localImageService;
    @Override
    public UUID createProduct(ProductRequestDTO productRequestDTO) {
        log.info("Creating product: {}", productRequestDTO);
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(productRequestDTO.getName());
        productEntity.setPrice(productRequestDTO.getPrice());
        productEntity.setStock(productRequestDTO.getStock());
        productEntity.setWarrantyPeriod(productRequestDTO.getWarrantyPeriod());

        //gan category
        if (productRequestDTO.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(productRequestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Category với ID: " + productRequestDTO.getCategoryId()));
            productEntity.setCategory(category);
        }


        //gan brand
        if (productRequestDTO.getBrandId() != null) {
            BrandEntity brand = brandRepository.findBrandById(productRequestDTO.getBrandId());
            productEntity.setBrandId(brand);
        }
        productEntity.setDescription(productRequestDTO.getDescription());
        ProductEntity saved = productRepository.save(productEntity);

        // Nếu có ảnh upload
        if (productRequestDTO.getImage() != null && !productRequestDTO.getImage().isEmpty()) {
            List<ProductImageEntity> imageEntities = new ArrayList<>();

            productRequestDTO.getImage().forEach(file -> {
                try {
                    String imageUrl = localImageService.saveImage(file);
                    ProductImageEntity imageEntity = new ProductImageEntity();
                    imageEntity.setProduct(saved);
                    imageEntity.setImageUrl(imageUrl);
                    imageEntities.add(imageEntity);
                } catch (Exception e) {
                    log.error("Lỗi khi lưu ảnh: {}", e.getMessage());
                }
            });

            productImageRepository.saveAll(imageEntities);
            saved.setImages(imageEntities);
        }

        return productEntity.getId();
    }
}
