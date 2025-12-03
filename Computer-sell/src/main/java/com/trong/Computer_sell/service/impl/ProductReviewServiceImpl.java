package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.request.review.CreateReviewRequest;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.review.ProductReviewListResponse;
import com.trong.Computer_sell.DTO.response.review.ReviewResponse;
import com.trong.Computer_sell.DTO.response.review.ReviewSummaryResponse;
import com.trong.Computer_sell.common.ReviewStatus;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.model.ProductReviewEntity;
import com.trong.Computer_sell.model.UserEntity;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.repository.ProductReviewRepository;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse submitReview(CreateReviewRequest request, String username) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        ProductReviewEntity entity = new ProductReviewEntity();
        entity.setProduct(product);
        entity.setUser(user);
        entity.setRating(request.getRating());
        entity.setComment(request.getComment());
        entity.setStatus(ReviewStatus.APPROVED);

        ProductReviewEntity saved = reviewRepository.save(entity);
        return ReviewResponse.fromEntity(saved);
    }

    @Override
    public ProductReviewListResponse getApprovedReviews(UUID productId) {
        List<ProductReviewEntity> reviews = reviewRepository
                .findByProductIdAndStatusOrderByCreatedAtDesc(productId, ReviewStatus.APPROVED);

        List<ReviewResponse> responses = reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());

        Double avg = reviewRepository.getAverageRating(productId);
        Map<Integer, Long> bucket = buildRatingBucket(productId);

        ReviewSummaryResponse summary = ReviewSummaryResponse.builder()
                .averageRating(avg != null ? avg : 0)
                .totalReviews((long) responses.size())
                .fiveStar(bucket.getOrDefault(5, 0L))
                .fourStar(bucket.getOrDefault(4, 0L))
                .threeStar(bucket.getOrDefault(3, 0L))
                .twoStar(bucket.getOrDefault(2, 0L))
                .oneStar(bucket.getOrDefault(1, 0L))
                .build();

        return new ProductReviewListResponse(responses, summary);
    }

    @Override
    public PageResponse<List<ReviewResponse>> searchReviews(
            String keyword,
            ReviewStatus status,
            UUID productId,
            Integer rating,
            int pageNo,
            int pageSize,
            String sortBy
    ) {
        Pageable pageable = PageRequest.of(
                pageNo > 0 ? pageNo - 1 : 0,
                pageSize,
                Sort.by(Sort.Direction.DESC, StringUtils.hasText(sortBy) ? sortBy : "createdAt")
        );

        String keywordPattern = (!StringUtils.hasText(keyword)) ? null : "%" + keyword.trim().toLowerCase() + "%";
        Page<ProductReviewEntity> page = reviewRepository.search(productId, status, rating, keywordPattern, pageable);

        List<ReviewResponse> items = page.getContent().stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageResponse<>(
                items,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional
    public ReviewResponse updateStatus(UUID reviewId, ReviewStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        ProductReviewEntity entity = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        entity.setStatus(status);
        return ReviewResponse.fromEntity(reviewRepository.save(entity));
    }

    private Map<Integer, Long> buildRatingBucket(UUID productId) {
        List<Object[]> rows = reviewRepository.countByRating(productId);
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            map.put(rating, count);
        }
        return map;
    }
}
