package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.common.NotificationType;
import com.trong.Computer_sell.model.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    // Lấy thông báo của user theo thứ tự mới nhất
    Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Lấy thông báo chưa đọc của user
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    // Đếm số thông báo chưa đọc
    long countByUserIdAndIsReadFalse(UUID userId);

    // Đánh dấu tất cả thông báo của user là đã đọc
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") UUID userId);

    // Lấy thông báo theo loại
    Page<NotificationEntity> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationType type, Pageable pageable);

    // Xóa thông báo cũ hơn ngày chỉ định
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Lấy thông báo cho admin (users có role ADMIN)
    @Query("SELECT n FROM NotificationEntity n WHERE n.user.id IN " +
           "(SELECT uhr.user.id FROM UserHasRole uhr WHERE uhr.role.name = 'ADMIN') " +
           "ORDER BY n.createdAt DESC")
    Page<NotificationEntity> findAdminNotifications(Pageable pageable);
}
