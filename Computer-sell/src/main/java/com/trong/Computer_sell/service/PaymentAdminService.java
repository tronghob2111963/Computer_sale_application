package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.payment.PaymentSearchResponse;
import com.trong.Computer_sell.common.PaymentStatus;

import java.time.LocalDateTime;

public interface PaymentAdminService {
    PageResponse<PaymentSearchResponse> searchPayments(
            String keyword,
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int pageNo,
            int pageSize,
            String sortBy
    );
}