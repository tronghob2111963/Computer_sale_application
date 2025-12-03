package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.payment.PaymentResponse;
import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.model.OrderEntity;
import com.trong.Computer_sell.model.PaymentEntity;
import com.trong.Computer_sell.repository.OrderRepository;
import com.trong.Computer_sell.repository.PaymentRepository;
import com.trong.Computer_sell.service.PaymentService;
import com.trong.Computer_sell.service.vnpay.VNPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VNPayService vnPayService;

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

        OrderEntity order = payment.getOrder();

        // Nếu tất cả payment đều SUCCESS → order hoàn tất
        boolean allPaid = order.getPayments().stream()
                .allMatch(p -> p.getPaymentStatus() == PaymentStatus.SUCCESS);

        if (allPaid) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        }

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

    @Override
    public List<PaymentResponse> getPaymentsByOrder(UUID orderId) {
        var list = paymentRepository.findByOrderId(orderId);

        return list.stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    @Override
    public PaymentResponse createVNPayPayment(UUID orderId) {

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Reuse existing VNPay payment if any, otherwise create a fresh one
        PaymentEntity payment = order.getPayments().stream()
                .filter(p -> "VNPAY".equalsIgnoreCase(p.getPaymentMethod()))
                .findFirst()
                .orElseGet(() -> PaymentEntity.builder()
                        .order(order)
                        .paymentMethod("VNPAY")
                        .provider("VNPay")
                        .paymentDate(LocalDateTime.now())
                        .build());

        // Refresh payment data for the new attempt
        payment.setPaymentMethod("VNPAY");
        payment.setProvider("VNPay");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getTotalAmount());
        payment.setNote("Pending VNPay checkout");
        payment.setPaymentDate(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        // Mark order as waiting for online payment
        order.setPaymentStatus(PaymentStatus.PENDING);
        orderRepository.save(order);

        // tạo link thanh toán
        String paymentUrl = vnPayService.createPayment(
                order.getTotalAmount().longValue(),
                payment.getId().toString()   // Dùng paymentId làm vnp_TxnRef
        );

        PaymentResponse response = PaymentResponse.fromEntity(payment);
        response.setTransactionId(paymentUrl);  // FE sẽ redirect theo URL này

        return response;
    }

    @Override
    public String handleVNPayIPN(Map<String, String> params) {

        String paymentId = params.get("vnp_TxnRef");
        String status = params.get("vnp_TransactionStatus");

        PaymentEntity payment = paymentRepository.findById(UUID.fromString(paymentId))
                .orElse(null);

        if (payment == null) return "Payment not found";

        if ("00".equals(status)) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(params.get("vnp_TransactionNo"));

            OrderEntity order = payment.getOrder();
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.PROCESSING);

            orderRepository.save(order);
            paymentRepository.save(payment);

        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }

        return "OK";
    }

}
