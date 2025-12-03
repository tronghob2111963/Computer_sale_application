package com.trong.Computer_sell.DTO.response.review;

import com.trong.Computer_sell.common.ReviewStatus;
import com.trong.Computer_sell.model.ProductReviewEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private UUID userId;
    private String userName;
    private int rating;
    private String comment;
    private ReviewStatus status;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(ProductReviewEntity entity) {
        String fullName = null;
        if (entity.getUser() != null) {
            String fn = entity.getUser().getFirstName();
            String ln = entity.getUser().getLastName();
            fullName = String.join(" ", fn != null ? fn : "", ln != null ? ln : "").trim();
            if (fullName.isBlank()) {
                fullName = entity.getUser().getUsername();
            }
        }

        return ReviewResponse.builder()
                .id(entity.getId())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(fullName)
                .rating(entity.getRating())
                .comment(entity.getComment())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
