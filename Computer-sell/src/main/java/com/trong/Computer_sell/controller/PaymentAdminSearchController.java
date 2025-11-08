package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.payment.PaymentSearchResponse;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.service.PaymentAdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminSearchController {

    private final PaymentAdminService paymentAdminService;

    @Operation(summary = "Tìm kiếm / lọc danh sách thanh toán (cho admin)")
    @GetMapping("/search")
    public ResponseData<PageResponse<PaymentSearchResponse>> searchPayments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "paymentDate") String sortBy
    ) {
        try {
            return new ResponseData<>(HttpStatus.OK.value(), "Tìm kiếm thanh toán thành công",
                    paymentAdminService.searchPayments(keyword, status, startDate, endDate, pageNo, pageSize, sortBy));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}