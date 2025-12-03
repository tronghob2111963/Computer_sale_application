package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.comment.CreateCommentRequest;
import com.trong.Computer_sell.DTO.response.comment.ProductCommentResponse;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.common.CommentStatus;

import java.util.List;
import java.util.UUID;

public interface ProductCommentService {
    ProductCommentResponse addComment(CreateCommentRequest request, String username);

    List<ProductCommentResponse> getApprovedComments(UUID productId);

    PageResponse<List<ProductCommentResponse>> searchComments(
            String keyword,
            CommentStatus status,
            UUID productId,
            int pageNo,
            int pageSize,
            String sortBy
    );

    ProductCommentResponse updateStatus(UUID commentId, CommentStatus status);
}
