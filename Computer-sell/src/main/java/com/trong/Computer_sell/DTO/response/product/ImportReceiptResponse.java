package com.trong.Computer_sell.DTO.response.product;

import com.trong.Computer_sell.common.ImportReceiptStatus;
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
    private UUID id;
    private UUID receiptId;
    private String receiptCode;
    private UUID employeeId;
    private String employeeName;
    private BigDecimal totalAmount;
    private List<ImportReceiptDetailResponse> details;
    private String note;
    private LocalDateTime receiptDate;  // Ngày nhập hàng
    private LocalDateTime createdAt;
    private ImportReceiptStatus status;

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
                .id(receipt.getId())
                .receiptId(receipt.getId())
                .receiptCode(receipt.getReceiptCode())
                .employeeId(receipt.getEmployee().getId())
                .employeeName(receipt.getEmployee().getUser().getLastName() + " " + receipt.getEmployee().getUser().getFirstName())
                .totalAmount(receipt.getTotalAmount())
                .details(detailResponses)
                .note(receipt.getNote())
                .receiptDate(receipt.getReceiptDate())  // Ngày nhập hàng
                .createdAt(receipt.getCreatedAt())
                .status(receipt.getStatus())
                .build();
    }
}
