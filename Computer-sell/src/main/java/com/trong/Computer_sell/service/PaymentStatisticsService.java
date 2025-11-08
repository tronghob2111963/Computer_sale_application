package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.payment.MonthlyRevenueResponse;
import com.trong.Computer_sell.DTO.response.payment.PaymentStatisticsResponse;

public interface PaymentStatisticsService {
    PaymentStatisticsResponse getOverallStatistics();
    MonthlyRevenueResponse getMonthlyRevenue(int year);
}
