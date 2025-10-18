package com.trong.Computer_sell.DTO.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private UUID cartId;
    private UUID userId;
    private BigDecimal totalPrice;
    private String status;
    private List<CartItemResponse> items;

}
