package com.trong.Computer_sell.DTO.response.User;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Builder
public class UserBuildResponse {
    private UUID id;
    private String name;
    private BigDecimal totalPrice;
    private Boolean isPublic;
    private List<UserBuildDetailResponse> details;
}
