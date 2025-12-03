package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.payment.PaymentResponse;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Tạo thanh toán tiền mặt cho đơn hàng")
    @PostMapping("/cash/{orderId}")
    public ResponseData<PaymentResponse> createCashPayment(@PathVariable UUID orderId) {
        try {
            return new ResponseData<>(200, "Thanh toán tiền mặt thành công",
                    paymentService.createCashPayment(orderId));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Xác nhận thanh toán")
    @PutMapping("/confirm/{paymentId}")
    public ResponseData<PaymentResponse> confirmPayment(@PathVariable UUID paymentId) {
        try {
            return new ResponseData<>(200, "Xác nhận thanh toán thành công",
                    paymentService.confirmPayment(paymentId));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Lấy tất cả thanh toán")
    @GetMapping
    public ResponseData<List<PaymentResponse>> getAllPayments() {
        try {
            return new ResponseData<>(200, "Lấy danh sách thanh toán thành công",
                    paymentService.getAllPayments());
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Lọc thanh toán theo trạng thái")
    @GetMapping("/status/{status}")
    public ResponseData<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        try {
            return new ResponseData<>(200, "Lọc thanh toán thành công",
                    paymentService.getPaymentsByStatus(status));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Xem chi tiết thanh toán")
    @GetMapping("/{paymentId}")
    public ResponseData<PaymentResponse> getPaymentDetail(@PathVariable UUID paymentId) {
        try {
            return new ResponseData<>(200, "Lấy chi tiết thanh toán thành công",
                    paymentService.getPaymentDetail(paymentId));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Tạo thanh toán VNPay")
    @PostMapping("/vnpay/{orderId}")
    public ResponseData<PaymentResponse> createVNPayPayment(@PathVariable UUID orderId) {
        try {
            return new ResponseData<>(200, "Tạo thanh toán VNPay thành công",
                    paymentService.createVNPayPayment(orderId));
        } catch (Exception e) {
            return new ResponseError(400, e.getMessage());
        }
    }

    @GetMapping("/vnpay/ipn")
    public String vnpayIPN(@RequestParam Map<String, String> params) {
        return paymentService.handleVNPayIPN(params);
    }

}
