package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.request.product.StockAdjustmentRequest;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.product.StockHistoryResponse;
import com.trong.Computer_sell.common.StockMovementType;
import com.trong.Computer_sell.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Tag(name = "Stock Management", description = "API quản lý kho hàng")
public class StockController {

    private final StockService stockService;

    @Operation(summary = "Lấy lịch sử kho theo sản phẩm")
    @GetMapping("/history/product/{productId}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> getStockHistoryByProduct(@PathVariable UUID productId) {
        try {
            List<StockHistoryResponse> history = stockService.getStockHistoryByProduct(productId);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Stock history retrieved", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @Operation(summary = "Lấy lịch sử kho theo loại biến động")
    @GetMapping("/history/type/{type}")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> getStockHistoryByType(@PathVariable StockMovementType type) {
        try {
            List<StockHistoryResponse> history = stockService.getStockHistoryByType(type);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Stock history retrieved", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @Operation(summary = "Lấy lịch sử kho theo khoảng thời gian")
    @GetMapping("/history/date-range")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin','Staff')")
    public ResponseEntity<?> getStockHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<StockHistoryResponse> history = stockService.getStockHistoryByDateRange(start, end);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Stock history retrieved", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @Operation(summary = "Kiểm tra tồn kho sản phẩm")
    @GetMapping("/check/{productId}")
    public ResponseEntity<?> checkStock(@PathVariable UUID productId) {
        try {
            Integer stock = stockService.getCurrentStock(productId);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Current stock", stock));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @Operation(summary = "Kiểm tra tồn kho có đủ không")
    @GetMapping("/check-available/{productId}")
    public ResponseEntity<?> checkStockAvailable(
            @PathVariable UUID productId,
            @RequestParam Integer quantity) {
        try {
            boolean available = stockService.checkStockAvailable(productId, quantity);
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(),
                    available ? "Stock available" : "Stock not available", available));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @Operation(summary = "Điều chỉnh tồn kho (kiểm kê)")
    @PostMapping("/adjust")
    @PreAuthorize("hasAnyAuthority('SysAdmin','Admin')")
    public ResponseEntity<?> adjustStock(@RequestBody StockAdjustmentRequest request) {
        try {
            stockService.adjustStock(
                    request.getProductId(),
                    request.getNewStock(),
                    request.getNote(),
                    request.getCreatedBy()
            );
            return ResponseEntity.ok(new ResponseData<>(HttpStatus.OK.value(), "Stock adjusted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }
}
