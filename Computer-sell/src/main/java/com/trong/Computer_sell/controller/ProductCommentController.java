package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.comment.CreateCommentRequest;
import com.trong.Computer_sell.DTO.response.comment.ProductCommentResponse;
import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.common.CommentStatus;
import com.trong.Computer_sell.service.ProductCommentService;
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
public class ProductCommentController {

    private final ProductCommentService commentService;

    @Operation(summary = "Them binh luan cho san pham hoac tra loi mot binh luan")
    @PostMapping("/api/comments")
    @PreAuthorize("hasAnyAuthority('User','Admin','SysAdmin','Staff')")
    public ResponseData<?> addComment(@RequestBody CreateCommentRequest request, Authentication authentication) {
        try {
            String username = authentication.getName();
            ProductCommentResponse response = commentService.addComment(request, username);
            return new ResponseData<>(HttpStatus.OK.value(), "Success", response);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Lay danh sach binh luan da duyet cua san pham")
    @GetMapping("/product/{productId}/comments")
    public ResponseData<List<ProductCommentResponse>> getComments(@PathVariable UUID productId) {
        try {
            List<ProductCommentResponse> comments = commentService.getApprovedComments(productId);
            return new ResponseData<>(HttpStatus.OK.value(), "Success", comments);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Admin: tim kiem/lua chon binh luan")
    @GetMapping("/api/admin/comments/search")
    @PreAuthorize("hasAnyAuthority('Admin','SysAdmin')")
    public ResponseData<PageResponse<List<ProductCommentResponse>>> searchComments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CommentStatus status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        try {
            PageResponse<List<ProductCommentResponse>> result = commentService.searchComments(
                    keyword, status, productId, pageNo, pageSize, sortBy);
            return new ResponseData<>(HttpStatus.OK.value(), "Success", result);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Admin: cap nhat trang thai binh luan")
    @PutMapping("/api/admin/comments/{id}/status")
    @PreAuthorize("hasAnyAuthority('Admin','SysAdmin')")
    public ResponseData<?> updateStatus(@PathVariable UUID id, @RequestParam CommentStatus status) {
        try {
            ProductCommentResponse response = commentService.updateStatus(id, status);
            return new ResponseData<>(HttpStatus.OK.value(), "Cap nhat trang thai thanh cong", response);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
