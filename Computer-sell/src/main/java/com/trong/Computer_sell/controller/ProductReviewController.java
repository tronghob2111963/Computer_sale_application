package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.review.CreateReviewRequest;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.review.ProductReviewListResponse;
import com.trong.Computer_sell.DTO.response.review.ReviewResponse;
import com.trong.Computer_sell.common.ReviewStatus;
import com.trong.Computer_sell.service.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @Operation(summary = "Khách hàng gửi đánh giá sản phẩm")
    @PostMapping("/api/reviews")
    @PreAuthorize("hasAnyAuthority('User','Admin','SysAdmin')")
    public ResponseData<?> submitReview(@RequestBody CreateReviewRequest request, Authentication authentication) {
        try {
            String username = authentication.getName();
            ReviewResponse response = productReviewService.submitReview(request, username);
            return new ResponseData<>(HttpStatus.OK.value(), "Gửi đánh giá thành công", response);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Danh sách đánh giá đã duyệt theo sản phẩm")
    @GetMapping("/product/{productId}/reviews")
    public ResponseData<?> getProductReviews(@PathVariable UUID productId) {
        try {
            ProductReviewListResponse data = productReviewService.getApprovedReviews(productId);
            return new ResponseData<>(HttpStatus.OK.value(), "Success", data);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Admin: tìm kiếm/lọc đánh giá sản phẩm")
    @GetMapping("/api/admin/reviews/search")
    @PreAuthorize("hasAnyAuthority('Admin','SysAdmin')")
    public ResponseData<PageResponse<List<ReviewResponse>>> searchReviews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        try {
            PageResponse<List<ReviewResponse>> result = productReviewService.searchReviews(
                    keyword, status, productId, rating, pageNo, pageSize, sortBy);
            return new ResponseData<>(HttpStatus.OK.value(), "Success", result);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Admin: cập nhật trạng thái đánh giá")
    @PutMapping("/api/admin/reviews/{id}/status")
    @PreAuthorize("hasAnyAuthority('Admin','SysAdmin')")
    public ResponseData<?> updateStatus(@PathVariable UUID id, @RequestParam ReviewStatus status) {
        try {
            ReviewResponse response = productReviewService.updateStatus(id, status);
            return new ResponseData<>(HttpStatus.OK.value(), "Cập nhật trạng thái thành công", response);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
