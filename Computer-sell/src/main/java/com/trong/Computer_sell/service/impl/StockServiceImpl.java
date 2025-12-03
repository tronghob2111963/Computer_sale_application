package com.trong.Computer_sell.service.impl;

import com.trong.Computer_sell.DTO.response.product.StockHistoryResponse;
import com.trong.Computer_sell.common.StockMovementType;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.model.StockHistoryEntity;
import com.trong.Computer_sell.repository.ProductRepository;
import com.trong.Computer_sell.repository.StockHistoryRepository;
import com.trong.Computer_sell.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "STOCK-SERVICE")
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;

    @Override
    @Transactional
    public void importStock(UUID productId, Integer quantity, BigDecimal unitPrice,
                            String referenceId, String referenceType, String note, String createdBy) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng nhập phải lớn hơn 0");
        }

        ProductEntity product = getProduct(productId);
        int stockBefore = product.getStock() != null ? product.getStock() : 0;
        int stockAfter = stockBefore + quantity;

        // Cập nhật tồn kho
        product.setStock(stockAfter);
        productRepository.save(product);

        // Ghi lịch sử
        saveStockHistory(product, StockMovementType.IMPORT, quantity, stockBefore, stockAfter,
                unitPrice, referenceId, referenceType, note, createdBy);

        log.info("IMPORT: Product {} | Qty: {} | Before: {} | After: {} | Ref: {}",
                product.getName(), quantity, stockBefore, stockAfter, referenceId);
    }

    @Override
    @Transactional
    public void exportStock(UUID productId, Integer quantity, BigDecimal unitPrice,
                            String referenceId, String referenceType, String note, String createdBy) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng xuất phải lớn hơn 0");
        }

        ProductEntity product = getProduct(productId);
        int stockBefore = product.getStock() != null ? product.getStock() : 0;

        if (stockBefore < quantity) {
            throw new RuntimeException("Không đủ tồn kho. Hiện có: " + stockBefore + ", cần: " + quantity);
        }

        int stockAfter = stockBefore - quantity;

        // Cập nhật tồn kho
        product.setStock(stockAfter);
        productRepository.save(product);

        // Ghi lịch sử
        saveStockHistory(product, StockMovementType.EXPORT, quantity, stockBefore, stockAfter,
                unitPrice, referenceId, referenceType, note, createdBy);

        log.info("EXPORT: Product {} | Qty: {} | Before: {} | After: {} | Ref: {}",
                product.getName(), quantity, stockBefore, stockAfter, referenceId);
    }

    @Override
    @Transactional
    public void returnStock(UUID productId, Integer quantity, BigDecimal unitPrice,
                            String referenceId, String referenceType, String note, String createdBy) {
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng hoàn phải lớn hơn 0");
        }

        ProductEntity product = getProduct(productId);
        int stockBefore = product.getStock() != null ? product.getStock() : 0;
        int stockAfter = stockBefore + quantity;

        // Cập nhật tồn kho
        product.setStock(stockAfter);
        productRepository.save(product);

        // Ghi lịch sử
        saveStockHistory(product, StockMovementType.RETURN, quantity, stockBefore, stockAfter,
                unitPrice, referenceId, referenceType, note, createdBy);

        log.info("RETURN: Product {} | Qty: {} | Before: {} | After: {} | Ref: {}",
                product.getName(), quantity, stockBefore, stockAfter, referenceId);
    }

    @Override
    @Transactional
    public void adjustStock(UUID productId, Integer newStock, String note, String createdBy) {
        if (newStock < 0) {
            throw new RuntimeException("Tồn kho không thể âm");
        }

        ProductEntity product = getProduct(productId);
        int stockBefore = product.getStock() != null ? product.getStock() : 0;
        int difference = newStock - stockBefore;

        // Cập nhật tồn kho
        product.setStock(newStock);
        productRepository.save(product);

        // Ghi lịch sử
        saveStockHistory(product, StockMovementType.ADJUSTMENT, Math.abs(difference), stockBefore, newStock,
                null, null, "ADJUSTMENT", note, createdBy);

        log.info("ADJUSTMENT: Product {} | Before: {} | After: {} | Diff: {}",
                product.getName(), stockBefore, newStock, difference);
    }

    @Override
    public List<StockHistoryResponse> getStockHistoryByProduct(UUID productId) {
        return stockHistoryRepository.findByProductIdOrderByMovementDateDesc(productId)
                .stream()
                .map(StockHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockHistoryResponse> getStockHistoryByType(StockMovementType type) {
        return stockHistoryRepository.findByMovementTypeOrderByMovementDateDesc(type)
                .stream()
                .map(StockHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockHistoryResponse> getStockHistoryByDateRange(LocalDateTime start, LocalDateTime end) {
        return stockHistoryRepository.findByDateRange(start, end)
                .stream()
                .map(StockHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkStockAvailable(UUID productId, Integer quantity) {
        ProductEntity product = getProduct(productId);
        int currentStock = product.getStock() != null ? product.getStock() : 0;
        return currentStock >= quantity;
    }

    @Override
    public Integer getCurrentStock(UUID productId) {
        ProductEntity product = getProduct(productId);
        return product.getStock() != null ? product.getStock() : 0;
    }

    // ==================== HELPER METHODS ====================

    private ProductEntity getProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
    }

    private void saveStockHistory(ProductEntity product, StockMovementType type, Integer quantity,
                                  Integer stockBefore, Integer stockAfter, BigDecimal unitPrice,
                                  String referenceId, String referenceType, String note, String createdBy) {
        StockHistoryEntity history = StockHistoryEntity.builder()
                .product(product)
                .movementType(type)
                .quantity(quantity)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .unitPrice(unitPrice)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .note(note)
                .createdBy(createdBy)
                .movementDate(LocalDateTime.now())
                .build();

        stockHistoryRepository.save(history);
    }
}
