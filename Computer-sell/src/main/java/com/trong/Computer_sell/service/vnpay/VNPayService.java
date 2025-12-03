package com.trong.Computer_sell.service.vnpay;

import com.trong.Computer_sell.common.vnpay.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class VNPayService {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.ipn-url}")
    private String ipnUrl;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    public String createPayment(long amount, String paymentId) {

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", "VND");

        params.put("vnp_TxnRef", paymentId);
        params.put("vnp_OrderInfo", "Thanh toan PaymentId=" + paymentId);
        params.put("vnp_OrderType", "billpayment");

        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpnUrl", ipnUrl);
        params.put("vnp_Locale", "vn");

        params.put("vnp_CreateDate",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        params.put("vnp_ExpireDate",
                LocalDateTime.now().plusMinutes(15)
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // 1. Build hashData (đúng chuẩn: key=value & ...)
        String hashData = VNPayUtil.buildHashData(params);

        // 2. Hash SHA512
        String secureHash = VNPayUtil.hmacSHA512(hashSecret, hashData);

        // 3. Build query URL encode
        String queryUrl = VNPayUtil.buildQuery(params);

        // 4. Trả về URL chính xác
        return payUrl + "?" + queryUrl
                + "&vnp_SecureHashType=HMACSHA512"
                + "&vnp_SecureHash=" + secureHash;
    }
}
