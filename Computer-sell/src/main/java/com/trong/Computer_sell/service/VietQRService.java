package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.payment.PaymentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface VietQRService {
    
    /**
     * Tạo thanh toán VietQR cho đơn hàng
     * @param orderId ID đơn hàng
     * @return PaymentResponse với QR code URL
     */
    PaymentResponse createVietQRPayment(UUID orderId);
    
    /**
     * Upload ảnh xác nhận chuyển khoản
     * @param paymentId ID thanh toán
     * @param proofImage File ảnh xác nhận
     * @return PaymentResponse đã cập nhật
     */
    PaymentResponse uploadProofImage(UUID paymentId, MultipartFile proofImage);
    
    /**
     * Admin xác nhận thanh toán VietQR
     * @param paymentId ID thanh toán
     * @return PaymentResponse đã xác nhận
     */
    PaymentResponse confirmVietQRPayment(UUID paymentId);
    
    /**
     * Admin từ chối thanh toán VietQR
     * @param paymentId ID thanh toán
     * @param reason Lý do từ chối
     * @return PaymentResponse đã từ chối
     */
    PaymentResponse rejectVietQRPayment(UUID paymentId, String reason);
    
    /**
     * Tạo URL mã QR VietQR
     * @param amount Số tiền
     * @param description Nội dung chuyển khoản
     * @return URL mã QR
     */
    String generateQRCodeUrl(long amount, String description);
}
