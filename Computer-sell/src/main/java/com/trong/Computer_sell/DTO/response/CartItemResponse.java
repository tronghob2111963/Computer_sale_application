package com.trong.Computer_sell.DTO.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private UUID productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subtotal;
    private String productImg;
}
