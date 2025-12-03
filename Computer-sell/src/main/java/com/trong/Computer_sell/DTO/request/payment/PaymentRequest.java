package com.trong.Computer_sell.DTO.request.payment;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentRequest {
    private UUID orderId;
    private Long amount;
    private String method; // VNPAY, MOMO, VIETQR, COD
}