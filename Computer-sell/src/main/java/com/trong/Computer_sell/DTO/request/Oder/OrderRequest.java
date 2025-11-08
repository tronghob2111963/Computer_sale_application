package com.trong.Computer_sell.DTO.request.Oder;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderRequest {
    private UUID userId;
    private String paymentMethod;
    private String promoCode;
    private List<OrderDetailRequest> items;
}