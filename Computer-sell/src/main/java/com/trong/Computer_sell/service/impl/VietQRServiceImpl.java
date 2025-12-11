package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.payment.PaymentResponse;
import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.config.VietQRConfig;
import com.trong.Computer_sell.model.OrderEntity;
import com.trong.Computer_sell.model.PaymentEntity;
import com.trong.Computer_sell.repository.OrderRepository;
import com.trong.Computer_sell.repository.PaymentRepository;
import com.trong.Computer_sell.service.LocalImageService;
import com.trong.Computer_sell.service.NotificationService;
import com.trong.Computer_sell.service.VietQRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VietQRServiceImpl implements VietQRService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final VietQRConfig vietQRConfig;
    private final LocalImageService localImageService;
    private final NotificationService notificationService;

    @Override
    public PaymentResponse createVietQRPayment(UUID orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Kiểm tra xem đã có payment VietQR chưa
        PaymentEntity payment = order.getPayments().stream()
                .filter(p -> "VIETQR".equalsIgnoreCase(p.getPaymentMethod()))
                .findFirst()
                .orElseGet(() -> PaymentEntity.builder()
                        .order(order)
                        .paymentMethod("VIETQR")
                        .provider("VietQR")
                        .paymentDate(LocalDateTime.now())
                        .build());

        // Cập nhật thông tin payment
        payment.setPaymentMethod("VIETQR");
        payment.setProvider("VietQR");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        payment.setNote("Chờ xác nhận chuyển khoản VietQR");
        payment.setPaymentDate(LocalDateTime.now());
        
        // Tạo nội dung chuyển khoản
        String description = "DH" + order.getId().toString().substring(0, 8).toUpperCase();
        payment.setPaymentContent(description);

        payment = paymentRepository.save(payment);

        // Cập nhật trạng thái đơn hàng
        order.setPaymentStatus(PaymentStatus.PENDING);
        orderRepository.save(order);

        // Tạo response với QR code URL
        PaymentResponse response = PaymentResponse.fromEntity(payment);
        String qrUrl = generateQRCodeUrl(
                payment.getAmount().longValue(),
                description
        );
        response.setQrCodeUrl(qrUrl);
        response.setTransactionId(description); // Dùng làm mã giao dịch tham chiếu

        return response;
    }

    @Override
    public PaymentResponse uploadProofImage(UUID paymentId, MultipartFile proofImage) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));

        if (!"VIETQR".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new RuntimeException("Chỉ hỗ trợ upload ảnh cho thanh toán VietQR");
        }

        try {
            // Upload ảnh và lấy URL
            String imageUrl = localImageService.uploadImage(proofImage, "vietqr-proofs");
            payment.setProofImageUrl(imageUrl);
            payment.setNote("Đã gửi ảnh xác nhận, chờ admin duyệt");
            payment = paymentRepository.save(payment);

            log.info("Uploaded proof image for payment {}: {}", paymentId, imageUrl);
            return PaymentResponse.fromEntity(payment);
        } catch (Exception e) {
            log.error("Failed to upload proof image for payment {}", paymentId, e);
            throw new RuntimeException("Không thể upload ảnh xác nhận: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponse confirmVietQRPayment(UUID paymentId) {
        // Fetch payment cùng với order và user để tránh lazy loading issues
        PaymentEntity payment = paymentRepository.findByIdWithOrderAndUser(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));

        if (!"VIETQR".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new RuntimeException("Chỉ hỗ trợ xác nhận cho thanh toán VietQR");
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setNote("Đã xác nhận thanh toán VietQR");
        paymentRepository.save(payment);

        // Cập nhật trạng thái đơn hàng sang CONFIRMED (đã xác nhận thanh toán, chờ xử lý giao hàng)
        OrderEntity order = payment.getOrder();
        OrderStatus oldStatus = order.getStatus();
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // Gửi thông báo cho user về việc thanh toán được xác nhận
        try {
            if (order.getUser() != null) {
                UUID userId = order.getUser().getId();
                String amount = payment.getAmount() != null ? 
                        String.format("%,.0f VNĐ", payment.getAmount()) : "";
                
                // Gửi thông báo thanh toán thành công
                notificationService.notifyPaymentConfirmed(userId, order.getId(), amount);
                
                // Gửi thông báo về việc đơn hàng chuyển trạng thái
                notificationService.notifyOrderStatusChanged(userId, order.getId(), 
                        oldStatus != null ? oldStatus.name() : "PENDING", OrderStatus.CONFIRMED.name());
                
                log.info("Sent notifications to user {} for order {}", userId, order.getId());
            } else {
                log.warn("Order {} has no user, skipping notification", order.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send notification for payment {}: {}", paymentId, e.getMessage());
            // Không throw exception để không ảnh hưởng đến việc xác nhận thanh toán
        }

        log.info("Confirmed VietQR payment {} for order {}", paymentId, order.getId());
        return PaymentResponse.fromEntity(payment);
    }

    @Override
    public PaymentResponse rejectVietQRPayment(UUID paymentId, String reason) {
        // Fetch payment cùng với order và user để tránh lazy loading issues
        PaymentEntity payment = paymentRepository.findByIdWithOrderAndUser(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán"));

        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setNote("Từ chối: " + (reason != null ? reason : "Không hợp lệ"));
        payment.setProofImageUrl(null); // Xóa ảnh cũ để user có thể gửi lại
        paymentRepository.save(payment);

        // Gửi thông báo cho user về việc thanh toán bị từ chối
        try {
            OrderEntity order = payment.getOrder();
            if (order.getUser() != null) {
                UUID userId = order.getUser().getId();
                String rejectReason = reason != null ? reason : "Không hợp lệ";
                notificationService.createNotification(
                        userId,
                        com.trong.Computer_sell.common.NotificationType.PAYMENT_CONFIRMED,
                        "Thanh toán bị từ chối",
                        "Thanh toán VietQR cho đơn hàng của bạn đã bị từ chối. Lý do: " + rejectReason + ". Vui lòng gửi lại ảnh xác nhận.",
                        order.getId(),
                        "ORDER"
                );
                log.info("Sent rejection notification to user {} for payment {}", userId, paymentId);
            }
        } catch (Exception e) {
            log.error("Failed to send rejection notification for payment {}: {}", paymentId, e.getMessage());
        }

        log.info("Rejected VietQR payment {} with reason: {}", paymentId, reason);
        return PaymentResponse.fromEntity(payment);
    }

    @Override
    public String generateQRCodeUrl(long amount, String description) {
        // Sử dụng VietQR API để tạo mã QR
        // Format: https://img.vietqr.io/image/{bankId}-{accountNo}-{template}.png?amount={amount}&addInfo={description}&accountName={accountName}
        
        try {
            String encodedDesc = URLEncoder.encode(description, StandardCharsets.UTF_8.toString());
            String encodedName = URLEncoder.encode(vietQRConfig.getAccountName(), StandardCharsets.UTF_8.toString());
            
            return String.format(
                "https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                vietQRConfig.getBankId(),
                vietQRConfig.getAccountNo(),
                vietQRConfig.getTemplate(),
                amount,
                encodedDesc,
                encodedName
            );
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode QR URL parameters", e);
            // Fallback without encoding
            return String.format(
                "https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s",
                vietQRConfig.getBankId(),
                vietQRConfig.getAccountNo(),
                vietQRConfig.getTemplate(),
                amount,
                description
            );
        }
    }
}
