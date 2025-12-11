package com.trong.Computer_sell.controller;

import com.trong.Computer_sell.DTO.response.EmbeddingRebuildResponse;
import com.trong.Computer_sell.service.VectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/embeddings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Embeddings", description = "Quản lý vector embeddings cho sản phẩm")
public class EmbeddingController {

    private final VectorService vectorService;

    @PostMapping("/rebuild")
    @Operation(summary = "Rebuild tất cả embeddings", 
               description = "Tạo lại vector embeddings cho tất cả sản phẩm trong database")
    public ResponseEntity<EmbeddingRebuildResponse> rebuildEmbeddings() {
        log.info("Starting embedding rebuild process...");
        
        long startTime = System.currentTimeMillis();
        int successCount = vectorService.rebuildAllEmbeddings();
        long duration = System.currentTimeMillis() - startTime;
        
        long totalCount = vectorService.getEmbeddingCount();
        
        EmbeddingRebuildResponse response = EmbeddingRebuildResponse.builder()
                .status("COMPLETED")
                .totalProducts((int) totalCount)
                .successCount(successCount)
                .failedCount((int) (totalCount - successCount))
                .durationMs(duration)
                .timestamp(LocalDateTime.now())
                .build();
        
        log.info("Embedding rebuild completed. Success: {}, Duration: {}ms", successCount, duration);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Kiểm tra trạng thái embeddings")
    public ResponseEntity<EmbeddingStatusResponse> getStatus() {
        long count = vectorService.getEmbeddingCount();
        return ResponseEntity.ok(new EmbeddingStatusResponse(count, LocalDateTime.now()));
    }

    record EmbeddingStatusResponse(long embeddingCount, LocalDateTime timestamp) {}
}
