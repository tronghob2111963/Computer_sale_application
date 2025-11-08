package com.trong.Computer_sell.DTO.request.Oder;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderDetailRequest {
    private UUID productId;
    private int quantity;
}