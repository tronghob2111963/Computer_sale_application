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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
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


    //  Tạo phiếu nhập hàng

    @Transactional
    @Override
    public ImportReceiptResponse createImportReceipt(ImportReceiptRequest request) {
        log.info("Creating new Import Receipt for employee {}", request.getEmployeeId());

        // 1️ Kiểm tra nhân viên
        var employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // 2 Tạo phiếu nhập mới
        ImportReceiptEntity receipt = new ImportReceiptEntity();
        receipt.setEmployee(employee);
        receipt.setNote(request.getNote());
        receipt.setReceiptDate(LocalDateTime.now());
        receipt.setTotalAmount(BigDecimal.ZERO);
        receipt.setStatus(ImportReceiptStatus.PENDING);

        //  Sinh mã phiếu PN-YYYY-XXXX
        String code = "PN-" + LocalDate.now().getYear() + "-" + String.format("%04d", new Random().nextInt(9999));
        receipt.setReceiptCode(code);

        receiptRepository.save(receipt);

        BigDecimal total = BigDecimal.ZERO;

        // 3️ Lưu chi tiết sản phẩm nhập
        for (ImportReceiptDetailRequest item : request.getItems()) {
            if (item.getQuantity() <= 0)
                throw new RuntimeException("Invalid quantity for product " + item.getProductId());
            if (item.getImportPrice().compareTo(BigDecimal.ZERO) <= 0)
                throw new RuntimeException("Invalid import price for product " + item.getProductId());

            ProductEntity product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

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

            //  Cập nhật tồn kho sản phẩm
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        // 4️ Cập nhật tổng tiền & trạng thái phiếu
        receipt.setTotalAmount(total);
        receipt.setStatus(ImportReceiptStatus.COMPLETED);
        receiptRepository.saveAndFlush(receipt);

        log.info("Import Receipt {} created successfully with total {}", receipt.getReceiptCode(), total);
        return ImportReceiptResponse.fromEntity(receipt);
    }


    // Lấy tất cả phiếu nhập

    @Override
    public List<ImportReceiptResponse> getAllImportReceipts() {
        return receiptRepository.findAll().stream()
                .map(ImportReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    //  Lấy chi tiết phiếu nhập theo ID

    @Override
    public ImportReceiptResponse getImportReceiptById(UUID receiptId) {
        ImportReceiptEntity receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException("Import Receipt not found"));
        return ImportReceiptResponse.fromEntity(receipt);
    }
}
