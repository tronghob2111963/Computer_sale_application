package com.trong.Computer_sell.service.impl;


import com.trong.Computer_sell.DTO.request.ProductRequestDTO;
import com.trong.Computer_sell.DTO.request.ProductUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.PageResponse;
import com.trong.Computer_sell.DTO.response.ProductDetailResponse;
import com.trong.Computer_sell.DTO.response.ProductResponseDTO;
import com.trong.Computer_sell.model.*;
import com.trong.Computer_sell.repository.BrandRepository;
import com.trong.Computer_sell.repository.CategoryRepository;
import com.trong.Computer_sell.repository.ProductImageRepository;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.service.LocalImageService;
import com.trong.Computer_sell.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Override
    public UUID updateProduct(ProductUpdateRequestDTO dto) {

        UUID id = dto.getId();
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Product với ID: " + id));
        if (dto.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Category với ID: " + dto.getCategoryId()));
            productEntity.setCategory(category);
        }

        //gan brand
        if (dto.getBrandId() != null) {
            BrandEntity brand = brandRepository.findBrandById(dto.getBrandId());
            productEntity.setBrandId(brand);
        }
        productEntity.setName(dto.getName());
        productEntity.setPrice(dto.getPrice());
        productEntity.setStock(dto.getStock());
        productEntity.setWarrantyPeriod(dto.getWarrantyPeriod());
        productEntity.setDescription(dto.getDescription());
        ProductEntity saved = productRepository.save(productEntity);

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {

            List<ProductImageEntity> imageEntities = new ArrayList<>();
            dto.getImage().forEach(file -> {
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
        return saved.getId();
    }

    @Override
    public UUID deleteProduct(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để xóa"));

        List<ProductImageEntity> images = productImageRepository.findByProductId(id);
        images.forEach(img -> {
            File file = new File("src/main/resources/static" + img.getImageUrl());
            if (file.exists()) file.delete();
        });

        productImageRepository.deleteAll(images);
        productRepository.delete(product);
        return null;
    }

    @Override
    public ProductDetailResponse getProductById(UUID id) {
        log.info("Product detail");
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Product với ID: " + id));



        return ProductDetailResponse.builder()
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .brandName(product.getBrandId().getName())
                .categoryName(product.getCategory().getName())
                .warrantyPeriod(product.getWarrantyPeriod())
                .image(product.getImages().stream().map(image -> image.getImageUrl()).collect(Collectors.toList()))
                .build();
    }

    @Override
    public PageResponse<?> getAllProducts(String keyword, int pageNo, int pageSize, String sortBy) {
        log.info("Find all users with keyword: {}", keyword);
        int p = pageNo > 0 ? pageNo - 1 : 0;
        List<Sort.Order> sorts = new ArrayList<>();
        // Sort by ID
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        //pagging
        Pageable pageable = PageRequest.of(p, pageSize, Sort.by(sorts));
        Page<ProductEntity>  productPage;
        if(StringUtils.hasLength(keyword)){
            keyword = "%" + keyword.toLowerCase() + "%";
            productPage = productRepository.searchUserByKeyword(keyword ,pageable);

        }else{
            productPage = productRepository.findAll(pageable);
        }



        List<ProductResponseDTO> products = productPage.stream().map(product -> {
            return ProductResponseDTO.builder()
                    .name(product.getName())
                    .price(product.getPrice())
                    .brandName(brandRepository.findBrandById(product.getBrandId().getId()).getName())
                    .categoryName(categoryRepository.findCategoryNameById(product.getCategory().getId()))
                    .image(productImageRepository.findProductImageByProductId(product.getId()))
                    .warrantyPeriod(product.getWarrantyPeriod())
                    .build();
        }).collect(Collectors.toList());

        return PageResponse.builder()
                .pageNo(productPage.getNumber() + 1)
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .items(products)
                .build();
    }
}
