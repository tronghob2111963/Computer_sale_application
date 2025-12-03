package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.review.CreateReviewRequest;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.review.ProductReviewListResponse;
import com.trong.Computer_sell.DTO.response.review.ReviewResponse;
import com.trong.Computer_sell.common.ReviewStatus;

import java.util.List;
import java.util.UUID;

public interface ProductReviewService {
    ReviewResponse submitReview(CreateReviewRequest request, String username);

    ProductReviewListResponse getApprovedReviews(UUID productId);

    PageResponse<List<ReviewResponse>> searchReviews(
            String keyword,
            ReviewStatus status,
            UUID productId,
            Integer rating,
            int pageNo,
            int pageSize,
            String sortBy
    );

    ReviewResponse updateStatus(UUID reviewId, ReviewStatus status);
}
