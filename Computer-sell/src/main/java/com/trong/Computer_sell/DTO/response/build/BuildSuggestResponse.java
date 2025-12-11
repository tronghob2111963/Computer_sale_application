package com.trong.Computer_sell.DTO.response.build;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BuildSuggestResponse {
    private String profile;
    private BigDecimal budgetInput;
    private BigDecimal estimatedTotal;
    private String note;
    private List<SuggestedPartDTO> parts;
}
