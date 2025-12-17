package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.common.ResponseData;
import com.trong.Computer_sell.DTO.response.common.ResponseError;
import com.trong.Computer_sell.DTO.response.dashboard.DashboardStatsResponse;
import com.trong.Computer_sell.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "API thống kê dashboard admin")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Lấy thống kê tổng quan dashboard")
    @GetMapping("/stats")
    public ResponseData<DashboardStatsResponse> getDashboardStats() {
        try {
            return new ResponseData<>(HttpStatus.OK.value(), "Lấy thống kê thành công",
                    dashboardService.getDashboardStats());
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Lấy thống kê dashboard theo năm")
    @GetMapping("/stats/{year}")
    public ResponseData<DashboardStatsResponse> getDashboardStatsByYear(@PathVariable int year) {
        try {
            return new ResponseData<>(HttpStatus.OK.value(), "Lấy thống kê thành công",
                    dashboardService.getDashboardStatsByYear(year));
        } catch (Exception e) {
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }
}
