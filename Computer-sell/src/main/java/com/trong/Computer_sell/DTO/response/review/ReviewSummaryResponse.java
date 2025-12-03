package com.trong.Computer_sell.DTO.response.review;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReviewSummaryResponse {
    private double averageRating;
    private long totalReviews;
    private long fiveStar;
    private long fourStar;
    private long threeStar;
    private long twoStar;
    private long oneStar;
}
