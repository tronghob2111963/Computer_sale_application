package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.product.ImportReceiptDetailRequest;
import com.trong.Computer_sell.DTO.request.product.ImportReceiptRequest;
import com.trong.Computer_sell.DTO.response.product.ImportReceiptResponse;

import java.util.List;
import java.util.UUID;

public interface ImportReceiptService {
    ImportReceiptResponse createImportReceipt(ImportReceiptRequest request);
    List<ImportReceiptResponse> getAllImportReceipts();
    ImportReceiptResponse getImportReceiptById(UUID receiptId);
}
