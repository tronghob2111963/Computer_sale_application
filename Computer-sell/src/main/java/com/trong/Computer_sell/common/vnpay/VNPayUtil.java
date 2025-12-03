package com.trong.Computer_sell.common.vnpay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class VNPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) hash.append(String.format("%02x", b));
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing data", e);
        }
    }

    public static String buildHashData(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    public static String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII)
                .replace("+", "%20"); // VNPay yêu cầu, KHÔNG được có dấu "+"
    }
}
