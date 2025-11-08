package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.payment.PaymentResponse;
import com.trong.Computer_sell.common.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    PaymentResponse createCashPayment(UUID orderId);
    PaymentResponse confirmPayment(UUID paymentId);
    List<PaymentResponse> getAllPayments();
    List<PaymentResponse> getPaymentsByStatus(PaymentStatus status);
    PaymentResponse getPaymentDetail(UUID paymentId);
}
