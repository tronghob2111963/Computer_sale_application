package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.payment.PaymentResponse;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.model.OrderEntity;
import com.trong.Computer_sell.model.PaymentEntity;
import com.trong.Computer_sell.repository.OrderRepository;
import com.trong.Computer_sell.repository.PaymentRepository;
import com.trong.Computer_sell.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    public PaymentResponse createCashPayment(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        PaymentEntity payment = PaymentEntity.builder()
                .order(order)
                .paymentMethod("CASH")
                .amount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.SUCCESS)
                .note("Thanh toán tiền mặt khi nhận hàng")
                .build();

        paymentRepository.save(payment);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        orderRepository.save(order);

        return PaymentResponse.fromEntity(payment);
    }

    @Override
    public PaymentResponse confirmPayment(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // cập nhật trạng thái order nếu cần
        OrderEntity order = payment.getOrder();
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        orderRepository.save(order);

        return PaymentResponse.fromEntity(payment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status).stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentDetail(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return PaymentResponse.fromEntity(payment);
    }
}