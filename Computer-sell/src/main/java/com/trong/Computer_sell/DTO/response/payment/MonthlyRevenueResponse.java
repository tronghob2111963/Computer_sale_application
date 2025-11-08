package com.trong.Computer_sell.DTO.response.payment;



import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueResponse {
    private int year;
    private List<BigDecimal> monthlyRevenue; // 12 phần tử (từ tháng 1-12)
}