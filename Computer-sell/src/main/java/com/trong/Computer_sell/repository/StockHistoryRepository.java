package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.common.StockMovementType;
import com.trong.Computer_sell.model.ProductEntity;
import com.trong.Computer_sell.model.StockHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistoryEntity, UUID> {

    // Lấy lịch sử kho theo sản phẩm
    List<StockHistoryEntity> findByProductIdOrderByMovementDateDesc(UUID productId);

    // Lấy lịch sử kho theo loại biến động
    List<StockHistoryEntity> findByMovementTypeOrderByMovementDateDesc(StockMovementType type);

    // Lấy lịch sử kho theo khoảng thời gian
    @Query("SELECT s FROM StockHistoryEntity s WHERE s.movementDate BETWEEN :start AND :end ORDER BY s.movementDate DESC")
    List<StockHistoryEntity> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Lấy lịch sử kho theo reference (phiếu nhập hoặc đơn hàng)
    List<StockHistoryEntity> findByReferenceIdAndReferenceType(String referenceId, String referenceType);

    // Tổng số lượng nhập theo sản phẩm
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockHistoryEntity s WHERE s.product.id = :productId AND s.movementType = 'IMPORT'")
    Integer getTotalImportByProduct(@Param("productId") UUID productId);

    // Tổng số lượng xuất theo sản phẩm
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockHistoryEntity s WHERE s.product.id = :productId AND s.movementType = 'EXPORT'")
    Integer getTotalExportByProduct(@Param("productId") UUID productId);
}
