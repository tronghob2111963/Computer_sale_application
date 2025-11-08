package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.User.UserBuildDetailResponse;
import com.trong.Computer_sell.DTO.response.User.UserBuildResponse;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.model.UserBuildDetailEntity;
import com.trong.Computer_sell.model.UserBuildEntity;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.repository.UserBuildDetailRepository;
import com.trong.Computer_sell.repository.UserBuildRepository;
import com.trong.Computer_sell.service.UserBuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserBuildServiceImpl implements UserBuildService {

    private final UserBuildRepository buildRepository;
    private final UserBuildDetailRepository detailRepository;
    private final ProductRepository productRepository;
    @Override
    public UserBuildResponse createBuild(UUID userId, String name) {
        UserBuildEntity build = new UserBuildEntity();
        build.setName(name);
        build.setUser(new UserEntity(userId));
        return toResponse(buildRepository.save(build));
    }

    @Override
    public UUID addProductToBuild(UUID buildId, UUID productId, int quantity) {
        log.info("Add product to build with build id", buildId);
        UserBuildEntity build = buildRepository.findById(buildId)
                .orElseThrow(() -> new RuntimeException("Build not found"));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        UserBuildDetailEntity detail = new UserBuildDetailEntity();
        detail.setBuild(build);
        detail.setProduct(product);
        detail.setQuantity(quantity);

        detailRepository.save(detail);

        // Cập nhật tổng giá
        BigDecimal total = detailRepository.findByBuildId(buildId).stream()
                .map(d -> d.getProduct().getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        build.setTotalPrice(total);
        buildRepository.save(build);
        log.info("Product added to build with build id", buildId);
        return buildId;

    }

    @Override
    public UserBuildResponse removeProductFromBuild(UUID buildId, UUID productId) {
        UserBuildEntity build = buildRepository.findById(buildId)
                .orElseThrow(() -> new RuntimeException("Build not found"));
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        UserBuildDetailEntity detail = detailRepository.findByBuildIdAndProductId(buildId, productId);

        //xóa detail
        detailRepository.delete(detail);

        // Cập nhật tổng giá
        BigDecimal total = detailRepository.findByBuildId(buildId).stream()
                .map(d -> d.getProduct().getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        build.setTotalPrice(total);
        return toResponse(buildRepository.save(build));
    }

    @Override
    public UserBuildResponse updateBuild(UUID buildId, String name) {
        UserBuildEntity build = buildRepository.findById(buildId)
                .orElseThrow(() -> new RuntimeException("Build not found"));
        build.setName(name);
        return toResponse(buildRepository.save(build));
    }

    @Override
    public void deleteBuild(UUID buildId) {
        try {
            buildRepository.deleteById(buildId);
        } catch (Exception e) {
            throw new RuntimeException("Build not found");
        }

    }

    @Override
    public List<UserBuildResponse> getUserBuilds(UUID userId) {
       try{
           return buildRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
       }catch (Exception e){
           throw new RuntimeException("User not found");
       }
    }

    @Override
    public UserBuildResponse getBuild(UUID buildId) {
        return toResponse(buildRepository.findById(buildId)
                .orElseThrow(() -> new RuntimeException("Build not found")));
    }

    private UserBuildResponse toResponse(UserBuildEntity build) {
        List<UserBuildDetailResponse> detailResponses = build.getDetails().stream()
                .map(detail -> UserBuildDetailResponse.builder()
                        .productId(detail.getProduct().getId())
                        .productName(detail.getProduct().getName())
                        .price(detail.getProduct().getPrice())
                        .quantity(detail.getQuantity())
                        .imageUrl(
                                detail.getProduct().getImages() != null && !detail.getProduct().getImages().isEmpty()
                                        ? detail.getProduct().getImages().get(0).getImageUrl()
                                        : null
                        )
                        .build()
                ).toList();

        return UserBuildResponse.builder()
                .id(build.getId())
                .name(build.getName())
                .totalPrice(build.getTotalPrice())
                .isPublic(build.getIsPublic())
                .details(detailResponses)
                .build();
    }

    private PageResponse<Object> getUserBuildPageResponse(int pageNo, int pageSize, Page<UserBuildEntity> buildsPage){
        List<UserBuildResponse> builds = buildsPage.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.builder()
                .pageNo(pageNo + 1)
                .pageSize(pageSize)
                .totalElements(buildsPage.getTotalElements())
                .totalPages(buildsPage.getTotalPages())
                .items(builds)
                .build();
    }
}
