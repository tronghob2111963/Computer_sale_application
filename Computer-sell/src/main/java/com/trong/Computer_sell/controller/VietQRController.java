package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.payment.PaymentResponse;
import com.trong.Computer_sell.service.VietQRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments/vietqr")
@RequiredArgsConstructor
@Tag(name = "VietQR Payment", description = "API thanh toán qua VietQR")
public class VietQRController {

    private final VietQRService vietQRService;

    @Operation(summary = "Tạo thanh toán VietQR cho đơn hàng")
    @PostMapping("/{orderId}")
    public ResponseData<PaymentResponse> createVietQRPayment(@PathVariable UUID orderId) {
        try {
            PaymentResponse response = vietQRService.createVietQRPayment(orderId);
            return new ResponseData<>(200, "Tạo thanh toán VietQR thành công", response);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Upload ảnh xác nhận chuyển khoản")
    @PostMapping(value = "/{paymentId}/proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<PaymentResponse> uploadProofImage(
            @PathVariable UUID paymentId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Vui lòng chọn ảnh xác nhận");
            }
            PaymentResponse response = vietQRService.uploadProofImage(paymentId, file);
            return new ResponseData<>(200, "Upload ảnh xác nhận thành công", response);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Admin xác nhận thanh toán VietQR")
    @PutMapping("/{paymentId}/confirm")
    public ResponseData<PaymentResponse> confirmPayment(@PathVariable UUID paymentId) {
        try {
            PaymentResponse response = vietQRService.confirmVietQRPayment(paymentId);
            return new ResponseData<>(200, "Xác nhận thanh toán thành công", response);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Admin từ chối thanh toán VietQR")
    @PutMapping("/{paymentId}/reject")
    public ResponseData<PaymentResponse> rejectPayment(
            @PathVariable UUID paymentId,
            @RequestParam(required = false) String reason) {
        try {
            PaymentResponse response = vietQRService.rejectVietQRPayment(paymentId, reason);
            return new ResponseData<>(200, "Đã từ chối thanh toán", response);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
