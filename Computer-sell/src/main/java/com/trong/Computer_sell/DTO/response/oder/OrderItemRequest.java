package com.trong.Computer_sell.DTO.response.oder;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItemRequest {
    private UUID productId;
    private Integer quantity;
}