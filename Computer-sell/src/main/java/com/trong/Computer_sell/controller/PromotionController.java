package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.promotion.PromotionRequest;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.promotion.PromotionResponse;
import com.trong.Computer_sell.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion API", description = "Quản lý khuyến mãi (Promotion Management)")
public class PromotionController {

    private final PromotionService promotionService;

    @Operation(summary = "Tạo khuyến mãi mới")
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('Admin','SysAdmin')")
    public ResponseEntity<?> createPromotion(@RequestBody PromotionRequest request) {
        try {
            PromotionResponse response = promotionService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseData<>(201, "Promotion created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(400, e.getMessage()));
        }
    }

    @Operation(summary = "Cập nhật khuyến mãi")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('Admin','SysAdmin')")
    public ResponseEntity<?> updatePromotion(@PathVariable UUID id, @RequestBody PromotionRequest request) {
        try {
            PromotionResponse response = promotionService.update(id, request);
            return ResponseEntity.ok(new ResponseData<>(200, "Promotion updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(400, e.getMessage()));
        }
    }

    @Operation(summary = "Xóa khuyến mãi")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('Admin','SysAdmin')")
    public ResponseEntity<?> deletePromotion(@PathVariable UUID id) {
        try {
            promotionService.delete(id);
            return ResponseEntity.ok(new ResponseData<>(200, "Promotion deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(400, e.getMessage()));
        }
    }

    @Operation(summary = "Lấy danh sách tất cả khuyến mãi")
    @GetMapping("/get-all")
    @PreAuthorize("hasAnyAuthority('Admin','SysAdmin','Staff')")
    public ResponseEntity<?> getAllPromotions() {
        return ResponseEntity.ok(new ResponseData<>(200, "Success", promotionService.getAll()));
    }

    @Operation(summary = "Lấy khuyến mãi theo mã code")
    @GetMapping("/get-by-code/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        try {
            PromotionResponse response = promotionService.getByCode(code);
            return ResponseEntity.ok(new ResponseData<>(200, "Promotion found", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(400, e.getMessage()));
        }
    }
}