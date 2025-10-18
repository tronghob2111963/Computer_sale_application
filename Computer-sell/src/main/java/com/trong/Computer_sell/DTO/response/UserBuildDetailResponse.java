package com.trong.Computer_sell.DTO.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBuildDetailResponse {
    private UUID productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
}
