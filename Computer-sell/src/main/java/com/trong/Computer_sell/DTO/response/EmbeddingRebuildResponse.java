package com.trong.Computer_sell.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRebuildResponse {
    private String status;
    private int totalProducts;
    private int successCount;
    private int failedCount;
    private long durationMs;
    private LocalDateTime timestamp;
}
