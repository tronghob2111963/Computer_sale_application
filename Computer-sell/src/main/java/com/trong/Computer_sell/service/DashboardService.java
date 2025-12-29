package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.dashboard.DashboardStatsResponse;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats();
    DashboardStatsResponse getDashboardStatsByYear(int year);
}
