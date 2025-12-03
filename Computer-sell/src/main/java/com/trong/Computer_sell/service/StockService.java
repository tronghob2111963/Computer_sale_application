package com.trong.Computer_sell.service;

import com.trong.Computer_sell.DTO.response.product.StockHistoryResponse;
import com.trong.Computer_sell.common.StockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service quản lý kho hàng
 */
public interface StockService {

    /**
     * Nhập kho - tăng tồn kho sản phẩm
     */
    void importStock(UUID productId, Integer quantity, BigDecimal unitPrice,
                     String referenceId, String referenceType, String note, String createdBy);

    /**
     * Xuất kho - giảm tồn kho sản phẩm (khi xác nhận đơn hàng)
     */
    void exportStock(UUID productId, Integer quantity, BigDecimal unitPrice,
                     String referenceId, String referenceType, String note, String createdBy);

    /**
     * Hoàn kho - tăng lại tồn kho (khi hủy đơn hàng)
     */
    void returnStock(UUID productId, Integer quantity, BigDecimal unitPrice,
                     String referenceId, String referenceType, String note, String createdBy);

    /**
     * Điều chỉnh kho - kiểm kê, sửa lỗi
     */
    void adjustStock(UUID productId, Integer newStock, String note, String createdBy);

    /**
     * Lấy lịch sử kho theo sản phẩm
     */
    List<StockHistoryResponse> getStockHistoryByProduct(UUID productId);

    /**
     * Lấy lịch sử kho theo loại biến động
     */
    List<StockHistoryResponse> getStockHistoryByType(StockMovementType type);

    /**
     * Lấy lịch sử kho theo khoảng thời gian
     */
    List<StockHistoryResponse> getStockHistoryByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Kiểm tra tồn kho có đủ để xuất không
     */
    boolean checkStockAvailable(UUID productId, Integer quantity);

    /**
     * Lấy tồn kho hiện tại của sản phẩm
     */
    Integer getCurrentStock(UUID productId);
}
