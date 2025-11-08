package com.trong.Computer_sell.DTO.request.product;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ImportReceiptRequest {
    private UUID employeeId;
    private String note;
    private List<ImportReceiptDetailRequest> items;
}
