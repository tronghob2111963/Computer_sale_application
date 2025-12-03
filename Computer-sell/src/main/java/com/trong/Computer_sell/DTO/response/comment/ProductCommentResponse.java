package com.trong.Computer_sell.DTO.response.comment;

import com.trong.Computer_sell.common.CommentStatus;
import com.trong.Computer_sell.model.ProductCommentEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ProductCommentResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private UUID userId;
    private String userName;
    private String content;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private UUID parentId;
    @Builder.Default
    private List<ProductCommentResponse> replies = new ArrayList<>();

    public static ProductCommentResponse fromEntity(ProductCommentEntity entity) {
        return fromEntity(entity, new ArrayList<>());
    }

    public static ProductCommentResponse fromEntity(ProductCommentEntity entity, List<ProductCommentResponse> replies) {
        String fullName = null;
        if (entity.getUser() != null) {
            String fn = entity.getUser().getFirstName();
            String ln = entity.getUser().getLastName();
            fullName = String.join(" ", fn != null ? fn : "", ln != null ? ln : "").trim();
            if (fullName.isBlank()) {
                fullName = entity.getUser().getUsername();
            }
        }

        return ProductCommentResponse.builder()
                .id(entity.getId())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(fullName)
                .content(entity.getContent())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .replies(replies != null ? replies : new ArrayList<>())
                .build();
    }
}
