package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.request.product.ImportReceiptRequest;
import com.trong.Computer_sell.DTO.response.product.ImportReceiptResponse;
import com.trong.Computer_sell.common.ImportReceiptStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ImportReceiptService {
    // Tạo phiếu nhập kho
    ImportReceiptResponse createImportReceipt(ImportReceiptRequest request);

    // Lấy tất cả phiếu nhập
    List<ImportReceiptResponse> getAllImportReceipts();

    // Lấy chi tiết phiếu nhập theo ID
    ImportReceiptResponse getImportReceiptById(UUID receiptId);

    // Hủy phiếu nhập (hoàn lại kho)
    void cancelImportReceipt(UUID receiptId, String cancelledBy);

    // Lọc phiếu nhập theo trạng thái
    List<ImportReceiptResponse> getImportReceiptsByStatus(ImportReceiptStatus status);

    // Lọc phiếu nhập theo khoảng thời gian
    List<ImportReceiptResponse> getImportReceiptsByDateRange(LocalDateTime start, LocalDateTime end);

    // Lọc phiếu nhập theo nhân viên
    List<ImportReceiptResponse> getImportReceiptsByEmployee(UUID employeeId);
}
