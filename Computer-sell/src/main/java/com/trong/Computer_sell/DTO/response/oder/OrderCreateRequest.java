package com.trong.Computer_sell.DTO.response.oder;

import com.trong.Computer_sell.common.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderCreateRequest {
    private UUID userId;
    private List<OrderItemRequest> items;
    private String promoCode;
    private PaymentMethod paymentMethod;
}