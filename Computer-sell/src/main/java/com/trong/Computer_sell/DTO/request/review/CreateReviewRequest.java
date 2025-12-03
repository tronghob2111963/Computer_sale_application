package com.trong.Computer_sell.DTO.request.review;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateReviewRequest {
    private UUID productId;
    private int rating;
    private String comment;
}
