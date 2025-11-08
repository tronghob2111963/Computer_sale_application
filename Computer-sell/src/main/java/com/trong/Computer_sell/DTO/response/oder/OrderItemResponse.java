package com.trong.Computer_sell.DTO.response.oder;

import com.trong.Computer_sell.model.OrderDetailEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private UUID productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public static OrderItemResponse fromEntity(OrderDetailEntity detail) {
        return OrderItemResponse.builder()
                .productId(detail.getProduct().getId())
                .productName(detail.getProduct().getName())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .subtotal(detail.getSubtotal())
                .build();
    }
}