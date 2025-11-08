package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.payment.MonthlyRevenueResponse;
import com.trong.Computer_sell.DTO.response.payment.PaymentStatisticsResponse;
import com.trong.Computer_sell.service.PaymentStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/payments/statistics")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentStatisticsService paymentStatisticsService;

    @Operation(summary = "Tổng quan thống kê thanh toán")
    @GetMapping("/overview")
    public ResponseData<PaymentStatisticsResponse> getOverview() {
        try {
            return new ResponseData<>(HttpStatus.OK.value(), "Lấy thống kê thành công",
                    paymentStatisticsService.getOverallStatistics());
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Thống kê doanh thu theo tháng của năm")
    @GetMapping("/monthly/{year}")
    public ResponseData<MonthlyRevenueResponse> getMonthlyRevenue(@PathVariable int year) {
        try {
            return new ResponseData<>(HttpStatus.OK.value(), "Lấy thống kê doanh thu theo tháng thành công",
                    paymentStatisticsService.getMonthlyRevenue(year));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}