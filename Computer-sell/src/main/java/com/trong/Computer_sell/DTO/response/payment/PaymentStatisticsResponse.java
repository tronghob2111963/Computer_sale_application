package com.trong.Computer_sell.DTO.response.payment;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatisticsResponse {
    private BigDecimal totalRevenue;
    private Long totalTransactions;
    private Map<String, BigDecimal> revenueByMethod;
}