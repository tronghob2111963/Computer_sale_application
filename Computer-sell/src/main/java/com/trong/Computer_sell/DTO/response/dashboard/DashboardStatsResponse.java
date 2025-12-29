package com.trong.Computer_sell.DTO.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    // Revenue stats
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal weekRevenue;
    private BigDecimal monthRevenue;
    
    // Order stats
    private Long totalOrders;
    private Long todayOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long shippingOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    
    // Customer stats
    private Long totalCustomers;
    private Long newCustomersToday;
    private Long newCustomersWeek;
    private Long newCustomersMonth;
    
    // Revenue by payment method
    private Map<String, BigDecimal> revenueByMethod;
    
    // Monthly revenue for chart (12 months)
    private List<BigDecimal> monthlyRevenue;
    
    // Daily revenue for last 7 days
    private List<DailyRevenue> dailyRevenue;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRevenue {
        private String date;
        private BigDecimal revenue;
        private Long orders;
    }
}
