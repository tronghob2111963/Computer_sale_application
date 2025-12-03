package com.trong.Computer_sell.DTO.response.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductReviewListResponse {
    private List<ReviewResponse> reviews;
    private ReviewSummaryResponse summary;
}
