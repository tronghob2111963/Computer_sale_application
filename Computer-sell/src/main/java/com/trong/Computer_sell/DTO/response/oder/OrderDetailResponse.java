package com.trong.Computer_sell.DTO.response.oder;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}

