package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.common.ImportReceiptStatus;
import com.trong.Computer_sell.model.ImportReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ImportReceiptRepository extends JpaRepository<ImportReceiptEntity, UUID> {

    // Đếm số phiếu nhập theo prefix (để sinh mã tuần tự)
    long countByReceiptCodeStartingWith(String prefix);

    // Tìm phiếu nhập theo trạng thái
    List<ImportReceiptEntity> findByStatus(ImportReceiptStatus status);

    // Tìm phiếu nhập theo khoảng thời gian
    @Query("SELECT r FROM ImportReceiptEntity r WHERE r.receiptDate BETWEEN :start AND :end ORDER BY r.receiptDate DESC")
    List<ImportReceiptEntity> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Tìm phiếu nhập theo nhân viên
    List<ImportReceiptEntity> findByEmployeeIdOrderByReceiptDateDesc(UUID employeeId);
}
