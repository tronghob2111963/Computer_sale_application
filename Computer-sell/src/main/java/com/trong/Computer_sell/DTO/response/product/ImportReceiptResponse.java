package com.trong.Computer_sell.DTO.response.product;

import com.trong.Computer_sell.model.ImportReceiptEntity;
import com.trong.Computer_sell.model.ProductEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportReceiptResponse {
    private UUID receiptId;
    private UUID employeeId;
    private String employeeName;
    private BigDecimal totalAmount;
    private List<ImportReceiptDetailResponse> details;
    private String note;
    private LocalDateTime createdAt;

    public static ImportReceiptResponse fromEntity(ImportReceiptEntity receipt) {
        List<ImportReceiptDetailResponse> detailResponses = receipt.getDetails()
                .stream()
                .map(d -> ImportReceiptDetailResponse.builder()
                        .productName(d.getProduct().getName())
                        .quantity(d.getQuantity())
                        .importPrice(d.getImportPrice())
                        .build())
                .toList();

        return ImportReceiptResponse.builder()
                .receiptId(receipt.getId())
                .employeeId(receipt.getEmployee().getId())
                .employeeName(receipt.getEmployee().getUser().getUsername())
                .totalAmount(receipt.getTotalAmount())
                .details(detailResponses)
                .note(receipt.getNote())
                .createdAt(receipt.getCreatedAt())
                .build();
    }
}
