package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.common.PageResponse;
import com.trong.Computer_sell.DTO.response.payment.PaymentSearchResponse;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.model.PaymentEntity;
import com.trong.Computer_sell.repository.PaymentRepository;
import com.trong.Computer_sell.service.PaymentAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentAdminServiceImpl implements PaymentAdminService {

    private final PaymentRepository paymentRepository;

    @Override
    public PageResponse<PaymentSearchResponse> searchPayments(
            String keyword,
            PaymentStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            int pageNo,
            int pageSize,
            String sortBy
    ) {
        Pageable pageable = PageRequest.of(pageNo > 0 ? pageNo - 1 : 0, pageSize,
                Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "paymentDate"));

        Page<PaymentEntity> page = paymentRepository.searchPayments(keyword, status, startDate, endDate, pageable);

        return new PageResponse<>(
                page.getContent().stream().map(PaymentSearchResponse::fromEntity).collect(Collectors.toList()),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}