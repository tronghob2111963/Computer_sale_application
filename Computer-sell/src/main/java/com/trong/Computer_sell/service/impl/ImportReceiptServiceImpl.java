package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.request.product.ImportReceiptDetailRequest;
import com.trong.Computer_sell.DTO.request.product.ImportReceiptRequest;
import com.trong.Computer_sell.DTO.response.product.ImportReceiptResponse;
import com.trong.Computer_sell.model.ImportReceiptDetailEntity;
import com.trong.Computer_sell.model.ImportReceiptEntity;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.common.ImportReceiptStatus;
import com.trong.Computer_sell.repository.EmployeeRepository;
import com.trong.Computer_sell.repository.ImportReceiptDetailRepository;
import com.trong.Computer_sell.repository.ImportReceiptRepository;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.service.ImportReceiptService;
import com.trong.Computer_sell.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "IMPORT-RECEIPT-SERVICE")
public class ImportReceiptServiceImpl implements ImportReceiptService {

    private final ImportReceiptRepository receiptRepository;
    private final ImportReceiptDetailRepository detailRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final StockService stockService;

    // Biến đếm để sinh mã phiếu tuần tự
    private static int receiptCounter = 0;

    @Transactional
    @Override
    public ImportReceiptResponse createImportReceipt(ImportReceiptRequest request) {
        log.info("Creating new Import Receipt for employee {}", request.getEmployeeId());

        // 1. Kiểm tra nhân viên
        var employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // 2. Tạo phiếu nhập mới với trạng thái PENDING
        ImportReceiptEntity receipt = new ImportReceiptEntity();
        receipt.setEmployee(employee);
        receipt.setNote(request.getNote());
        receipt.setReceiptDate(LocalDateTime.now());
        receipt.setTotalAmount(BigDecimal.ZERO);
        receipt.setStatus(ImportReceiptStatus.PENDING);

        // Sinh mã phiếu PN-YYYY-XXXX (tuần tự)
        String code = generateReceiptCode();
        receipt.setReceiptCode(code);

        receiptRepository.save(receipt);

        BigDecimal total = BigDecimal.ZERO;

        // 3. Lưu chi tiết sản phẩm nhập
        for (ImportReceiptDetailRequest item : request.getItems()) {
            validateImportItem(item);

            ProductEntity product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            // Tạo chi tiết phiếu nhập
            ImportReceiptDetailEntity detail = new ImportReceiptDetailEntity();
            detail.setReceipt(receipt);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setImportPrice(item.getImportPrice());

            BigDecimal subtotal = item.getImportPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            detail.setSubtotal(subtotal);
            total = total.add(subtotal);

            detailRepository.save(detail);

            // Sử dụng StockService để nhập kho và ghi lịch sử
            stockService.importStock(
                    product.getId(),
                    item.getQuantity(),
                    item.getImportPrice(),
                    receipt.getReceiptCode(),
                    "IMPORT_RECEIPT",
                    "Nhập kho từ phiếu " + receipt.getReceiptCode(),
                    employee.getUser().getUsername()
            );
        }

        // 4. Cập nhật tổng tiền & trạng thái phiếu
        receipt.setTotalAmount(total);
        receipt.setStatus(ImportReceiptStatus.COMPLETED);
        receiptRepository.saveAndFlush(receipt);

        log.info("Import Receipt {} created successfully with total {}", receipt.getReceiptCode(), total);
        return ImportReceiptResponse.fromEntity(receipt);
    }

    @Override
    public List<ImportReceiptResponse> getAllImportReceipts() {
        return receiptRepository.findAll().stream()
                .map(ImportReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ImportReceiptResponse getImportReceiptById(UUID receiptId) {
        ImportReceiptEntity receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Import Receipt not found"));
        return ImportReceiptResponse.fromEntity(receipt);
    }

    @Override
    @Transactional
    public void cancelImportReceipt(UUID receiptId, String cancelledBy) {
        ImportReceiptEntity receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Import Receipt not found"));

        if (receipt.getStatus() == ImportReceiptStatus.CANCELLED) {
            throw new RuntimeException("Phiếu nhập đã bị hủy trước đó");
        }

        if (receipt.getStatus() != ImportReceiptStatus.COMPLETED) {
            throw new RuntimeException("Chỉ có thể hủy phiếu nhập đã hoàn thành");
        }

        // Hoàn lại kho cho từng sản phẩm
        for (ImportReceiptDetailEntity detail : receipt.getDetails()) {
            stockService.returnStock(
                    detail.getProduct().getId(),
                    detail.getQuantity(),
                    detail.getImportPrice(),
                    receipt.getReceiptCode(),
                    "IMPORT_RECEIPT_CANCEL",
                    "Hoàn kho do hủy phiếu nhập " + receipt.getReceiptCode(),
                    cancelledBy
            );
        }

        receipt.setStatus(ImportReceiptStatus.CANCELLED);
        receiptRepository.save(receipt);
        log.info("Import Receipt {} cancelled by {}", receipt.getReceiptCode(), cancelledBy);
    }

    @Override
    public List<ImportReceiptResponse> getImportReceiptsByStatus(ImportReceiptStatus status) {
        return receiptRepository.findByStatus(status).stream()
                .map(ImportReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImportReceiptResponse> getImportReceiptsByDateRange(LocalDateTime start, LocalDateTime end) {
        return receiptRepository.findByDateRange(start, end).stream()
                .map(ImportReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImportReceiptResponse> getImportReceiptsByEmployee(UUID employeeId) {
        return receiptRepository.findByEmployeeIdOrderByReceiptDateDesc(employeeId).stream()
                .map(ImportReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private void validateImportItem(ImportReceiptDetailRequest item) {
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new RuntimeException("Số lượng nhập phải lớn hơn 0 cho sản phẩm " + item.getProductId());
        }
        if (item.getImportPrice() == null || item.getImportPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá nhập phải lớn hơn 0 cho sản phẩm " + item.getProductId());
        }
    }

    private synchronized String generateReceiptCode() {
        // Đếm số phiếu trong năm hiện tại
        int year = LocalDate.now().getYear();
        long count = receiptRepository.countByReceiptCodeStartingWith("PN-" + year);
        return "PN-" + year + "-" + String.format("%04d", count + 1);
    }
}
