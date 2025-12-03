package com.trong.Computer_sell.DTO.response.oder;

import com.trong.Computer_sell.model.OrderDetailEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {

    private UUID productId;
    private String productName;
    private String productImageUrl;   //  ảnh sản phẩm

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    private BigDecimal vatAmount;        // tiền VAT
    private BigDecimal subtotalWithVat;  // tổng sau VAT

    public static OrderDetailResponse fromEntity(OrderDetailEntity entity) {

        BigDecimal subtotal = entity.getSubtotal();

        //  VAT mặc định 10% (nếu bạn muốn dynamic thì nói mình sửa)
        BigDecimal vatPercent = BigDecimal.valueOf(0.10);
        BigDecimal vatAmount = subtotal.multiply(vatPercent);
        BigDecimal subtotalWithVat = subtotal.add(vatAmount);

        return OrderDetailResponse.builder()
                .productId(entity.getProduct().getId())
                .productName(entity.getProduct().getName())
                .productImageUrl(String.valueOf(entity.getProduct().getImages().get(0)))  //  nhớ là ProductEntity phải có trường này
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .subtotal(subtotal)
                .vatAmount(vatAmount)
                .subtotalWithVat(subtotalWithVat)
                .build();
    }
}
