package com.trong.Computer_sell.service.impl;



import com.trong.Computer_sell.DTO.request.product.ProductRequestDTO;
import com.trong.Computer_sell.DTO.request.product.ProductUpdateRequestDTO;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.product.ProductDetailResponseDTO;
import com.trong.Computer_sell.DTO.response.product.ProductResponseDTO;
import com.trong.Computer_sell.common.ProductStatus;
import com.trong.Computer_sell.model.*;
import com.trong.Computer_sell.repository.*;
import com.trong.Computer_sell.service.LocalImageService;
import com.trong.Computer_sell.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ProductTypeRepository productTypeRepository;
    @Override
    @Transactional
    public UUID createProduct(ProductRequestDTO productRequestDTO) {
        log.info("Creating product: {}", productRequestDTO);
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(productRequestDTO.getName());
        productEntity.setPrice(productRequestDTO.getPrice());
        // Sản phẩm mới tạo có stock = 0, phải nhập kho mới có tồn kho
        productEntity.setStock(0);
        productEntity.setStatus(ProductStatus.ACTIVE);
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

        //gan product_type
        if (productRequestDTO.getProductTypeId() != null) {
            ProductTypeEntity productType = productTypeRepository.findById(productRequestDTO.getProductTypeId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ProductType với ID: " + productRequestDTO.getProductTypeId()));
            productEntity.setProductTypeId(productType);
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

        // Gán category
        if (dto.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Category với ID: " + dto.getCategoryId()));
            productEntity.setCategory(category);
        }

        // Gán brand
        if (dto.getBrandId() != null) {
            BrandEntity brand = brandRepository.findBrandById(dto.getBrandId());
            productEntity.setBrandId(brand);
        }

        // Gán product type
        if (dto.getProductTypeId() != null) {
            ProductTypeEntity productType = productTypeRepository.findById(dto.getProductTypeId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ProductType với ID: " + dto.getProductTypeId()));
            productEntity.setProductTypeId(productType);
        }

        // KHÔNG cho phép cập nhật stock trực tiếp - stock chỉ được thay đổi qua:
        // 1. Phiếu nhập kho (ImportReceipt)
        // 2. Xác nhận đơn hàng (Order CONFIRMED)
        // 3. Hủy đơn hàng (Order CANCELED)
        // 4. Điều chỉnh kho (Stock Adjustment)

        // Cập nhật các thuộc tính khác
        productEntity.setName(dto.getName());
        productEntity.setPrice(dto.getPrice());
        productEntity.setWarrantyPeriod(dto.getWarrantyPeriod());
        productEntity.setDescription(dto.getDescription());

        ProductEntity saved = productRepository.save(productEntity);

        // Lưu ảnh nếu có
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
    public ProductDetailResponseDTO getProductById(UUID id) {
        log.info("Product detail");
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Product với ID: " + id));

        String brandName = (product.getBrandId() != null)
                ? product.getBrandId().getName()
                : null;

        String categoryName = (product.getCategory() != null)
                ? product.getCategory().getName()
                : null;

        Object productType = (product.getProductTypeId() != null)
                ? productTypeRepository.findProductTypeById(product.getProductTypeId().getId())
                : null;

        return ProductDetailResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .brandName(brandName)
                .categoryName(categoryName)
                .stock(product.getStock())
                .productType((String)productType)
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
            log.info("product", productPage);
        }



        List<ProductResponseDTO> products = productPage.stream().map(product -> {
            String brandName = (product.getBrandId() != null)
                    ? product.getBrandId().getName()
                    : null;

            String categoryName = (product.getCategory() != null)
                    ? product.getCategory().getName()
                    : null;

            String productType = (product.getProductTypeId() != null)
                    ? product.getProductTypeId().getName()
                    : null;

            return ProductResponseDTO.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .brandName(brandName)
                    .categoryName(categoryName)
                    .productType(productType)
                    .image(product.getImages().stream().map(image -> image.getImageUrl()).collect(Collectors.toList()))
                    .warrantyPeriod(product.getWarrantyPeriod())
                    .stock(product.getStock())
                    .build();
        }).toList();

        return PageResponse.builder()
                .pageNo(productPage.getNumber() + 1)
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .items(products)
                .build();
    }

    @Override
    public PageResponse<?> getAllProductsByBrandId(UUID brandId, String keyword, int pageNo, int pageSize, String sortBy) {
        log.info("Get all products by brand id");
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
            productPage = productRepository.searchProductByBrandId(brandId, pageable);
        }
        List<ProductResponseDTO> products = productPage.stream().map(product -> {
            String brandName = (product.getBrandId() != null)
                    ? brandRepository.findBrandById(product.getBrandId().getId()).getName()
                    : null;

            String categoryName = (product.getCategory() != null)
                    ? categoryRepository.findCategoryNameById(product.getCategory().getId())
                    : null;

            String productType = (product.getProductTypeId() != null)
                    ? product.getProductTypeId().getName()
                    : null;

            return ProductResponseDTO.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .brandName(brandName)
                    .categoryName(categoryName)
                    .productType(productType)
                    .image(productImageRepository.findProductImageByProductId(product.getId()))
                    .warrantyPeriod(product.getWarrantyPeriod())
                    .stock(product.getStock())
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

    @Override
    public PageResponse<?> getAllProductsByCategoryId(UUID categoryId, String keyword, int pageNo, int pageSize, String sortBy) {
        log.info("Get all products by brand id");
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
            productPage = productRepository.searchProductByCategoryId(categoryId, pageable);
        }
        List<ProductResponseDTO> products = productPage.stream().map(product -> {
            String brandName = (product.getBrandId() != null)
                    ? brandRepository.findBrandById(product.getBrandId().getId()).getName()
                    : null;

            String categoryName = (product.getCategory() != null)
                    ? categoryRepository.findCategoryNameById(product.getCategory().getId())
                    : null;

            String productType = (product.getProductTypeId() != null)
                    ? product.getProductTypeId().getName()
                    : null;

            return ProductResponseDTO.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .brandName(brandName)
                    .categoryName(categoryName)
                    .productType(productType)
                    .image(productImageRepository.findProductImageByProductId(product.getId()))
                    .warrantyPeriod(product.getWarrantyPeriod())
                    .stock(product.getStock())
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

    @Override
    public PageResponse<?> getAllProductByProductTypeId(UUID productTypeId, String keyword, int pageNo, int pageSize, String sortBy) {
        log.info("Get all products by product type");

        int p = pageNo > 0 ? pageNo - 1 : 0;
        List<Sort.Order> sorts = new ArrayList<>();

        // Xử lý sort
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

        Pageable pageable = PageRequest.of(p, pageSize, Sort.by(sorts));
        Page<ProductEntity> productPage;

        // Tìm kiếm theo keyword và productTypeId
        if (StringUtils.hasLength(keyword)) {
            productPage = productRepository.searchProductByTypeAndKeyword(productTypeId, keyword.trim().toLowerCase(), pageable);
        } else {
            productPage = productRepository.searchProductByProductTypeId(productTypeId, pageable);
        }

        // Map sang DTO, có kiểm tra null
        List<ProductResponseDTO> products = productPage.stream().map(product -> {
            String brandName = (product.getBrandId() != null)
                    ? brandRepository.findBrandById(product.getBrandId().getId()).getName()
                    : null;

            String categoryName = (product.getCategory() != null)
                    ? categoryRepository.findCategoryNameById(product.getCategory().getId())
                    : null;

            List<String> images = productImageRepository.findProductImageByProductId(product.getId());

            return ProductResponseDTO.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .brandName(brandName)
                    .categoryName(categoryName)
                    .productType(product.getProductTypeId().getName())
                    .image(images)
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

    // ==================== SOFT DELETE & STATUS MANAGEMENT ====================

    @Override
    @Transactional
    public void softDeleteProduct(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Chuyển trạng thái sang DELETED thay vì xóa cứng
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
        log.info("Product {} soft deleted (status changed to DELETED)", id);
    }

    @Override
    @Transactional
    public void restoreProduct(UUID id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        if (product.getStatus() != ProductStatus.DELETED) {
            throw new RuntimeException("Sản phẩm không ở trạng thái DELETED");
        }

        product.setStatus(ProductStatus.ACTIVE);
        productRepository.save(product);
        log.info("Product {} restored (status changed to ACTIVE)", id);
    }

    @Override
    @Transactional
    public void updateProductStatus(UUID id, ProductStatus status) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        product.setStatus(status);
        productRepository.save(product);
        log.info("Product {} status changed to {}", id, status);
    }
}
