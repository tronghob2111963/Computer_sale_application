package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.payment.MonthlyRevenueResponse;
import com.trong.Computer_sell.DTO.response.payment.PaymentStatisticsResponse;
import com.trong.Computer_sell.repository.PaymentRepository;
import com.trong.Computer_sell.service.PaymentStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;



@Service
@RequiredArgsConstructor
public class PaymentStatisticsServiceImpl implements PaymentStatisticsService {
    private final PaymentRepository paymentRepository;

    @Override
    public PaymentStatisticsResponse getOverallStatistics() {
        BigDecimal totalRevenue = paymentRepository.getTotalRevenue();
        Long totalTransactions = paymentRepository.getTotalTransactions();

        Map<String, BigDecimal> revenueByMethod = new HashMap<>();
        for (Object[] row : paymentRepository.getRevenueByPaymentMethod()) {
            revenueByMethod.put((String) row[0], (BigDecimal) row[1]);
        }

        return PaymentStatisticsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTransactions(totalTransactions)
                .revenueByMethod(revenueByMethod)
                .build();
    }

    @Override
    public MonthlyRevenueResponse getMonthlyRevenue(int year) {
        List<Object[]> rows = paymentRepository.getMonthlyRevenueByYear(year);

        List<BigDecimal> monthlyRevenue = new ArrayList<>(Collections.nCopies(12, BigDecimal.ZERO));
        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            BigDecimal amount = (BigDecimal) row[1];
            monthlyRevenue.set(month - 1, amount);
        }

        return MonthlyRevenueResponse.builder()
                .year(year)
                .monthlyRevenue(monthlyRevenue)
                .build();
    }
}
