package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.dashboard.DashboardStatsResponse;
import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.repository.OrderRepository;
import com.trong.Computer_sell.repository.PaymentRepository;
import com.trong.Computer_sell.repository.UserRepository;
import com.trong.Computer_sell.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        return getDashboardStatsByYear(LocalDate.now().getYear());
    }

    @Override
    public DashboardStatsResponse getDashboardStatsByYear(int year) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        // Revenue stats
        BigDecimal totalRevenue = paymentRepository.getTotalRevenue();
        BigDecimal todayRevenue = orderRepository.getTotalRevenue(startOfToday, endOfToday);
        BigDecimal weekRevenue = orderRepository.getTotalRevenue(startOfWeek, now);
        BigDecimal monthRevenue = orderRepository.getTotalRevenue(startOfMonth, now);

        // Order stats
        long totalOrders = orderRepository.count();
        long todayOrders = orderRepository.findByDateRange(startOfToday, endOfToday).size();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long shippingOrders = orderRepository.countByStatus(OrderStatus.SHIPPING);
        long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELED);

        // Customer stats
        long totalCustomers = userRepository.count();

        // Revenue by payment method
        Map<String, BigDecimal> revenueByMethod = new HashMap<>();
        for (Object[] row : paymentRepository.getRevenueByPaymentMethod()) {
            revenueByMethod.put((String) row[0], (BigDecimal) row[1]);
        }

        // Monthly revenue
        List<Object[]> monthlyRows = paymentRepository.getMonthlyRevenueByYear(year);
        List<BigDecimal> monthlyRevenue = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));
        for (Object[] row : monthlyRows) {
            int month = ((Number) row[0]).intValue();
            BigDecimal amount = (BigDecimal) row[1];
            monthlyRevenue.set(month - 1, amount);
        }

        // Daily revenue for last 7 days
        List<DashboardStatsResponse.DailyRevenue> dailyRevenue = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
            
            BigDecimal revenue = orderRepository.getTotalRevenue(dayStart, dayEnd);
            long orders = orderRepository.findByDateRange(dayStart, dayEnd).size();
            
            dailyRevenue.add(DashboardStatsResponse.DailyRevenue.builder()
                    .date(date.format(formatter))
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .orders(orders)
                    .build());
        }

        return DashboardStatsResponse.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .weekRevenue(weekRevenue != null ? weekRevenue : BigDecimal.ZERO)
                .monthRevenue(monthRevenue != null ? monthRevenue : BigDecimal.ZERO)
                .totalOrders(totalOrders)
                .todayOrders(todayOrders)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .shippingOrders(shippingOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalCustomers(totalCustomers)
                .newCustomersToday(0L) // TODO: implement when createdAt field available
                .newCustomersWeek(0L)
                .newCustomersMonth(0L)
                .revenueByMethod(revenueByMethod)
                .monthlyRevenue(monthlyRevenue)
                .dailyRevenue(dailyRevenue)
                .build();
    }
}
