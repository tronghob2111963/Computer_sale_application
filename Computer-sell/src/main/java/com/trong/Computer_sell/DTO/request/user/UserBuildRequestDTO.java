package com.trong.Computer_sell.DTO.request.user;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserBuildRequestDTO {
    private UUID userId;
    private String name;

}
