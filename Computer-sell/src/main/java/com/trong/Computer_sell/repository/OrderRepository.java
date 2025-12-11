package com.trong.Computer_sell.repository;

import com.trong.Computer_sell.common.OrderStatus;
import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    /**
     * Lấy danh sách đơn hàng của người dùng (từ mới nhất -> cũ nhất)
     */
    List<OrderEntity> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    /**
     * Lọc đơn hàng theo trạng thái (dành cho admin)
     */
    List<OrderEntity> findByStatus(OrderStatus status);

    /**
     * Lọc đơn hàng theo trạng thái thanh toán
     */
    List<OrderEntity> findByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Lấy đơn hàng có chi tiết sản phẩm (fetch join) — dùng khi hiển thị trang chi tiết đơn hàng
     */
    @Query("""
        SELECT DISTINCT o FROM OrderEntity o
        LEFT JOIN FETCH o.orderDetails d
        LEFT JOIN FETCH d.product p
        WHERE o.id = :id
    """)
    Optional<OrderEntity> findWithDetailsById(@Param("id") UUID id);

    /**
     * Lấy đơn hàng kèm khuyến mãi và chi tiết (đã dùng @Fetch(Subselect) trong Entity để tránh lỗi MultipleBagFetchException)
     */
    @Query("""
        SELECT o FROM OrderEntity o
        LEFT JOIN FETCH o.orderPromotions op
        WHERE o.id = :id
    """)
    Optional<OrderEntity> findWithPromotionsAndDetailsById(@Param("id") UUID id);

    /**
     * Lọc đơn hàng theo khoảng thời gian (ví dụ: báo cáo doanh thu, thống kê)
     */
    @Query("""
        SELECT o FROM OrderEntity o
        WHERE o.orderDate BETWEEN :start AND :end
        ORDER BY o.orderDate DESC
    """)
    List<OrderEntity> findByDateRange(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    /**
     * Đếm số lượng đơn hàng theo trạng thái (dùng trong dashboard admin)
     */
    @Query("""
        SELECT COUNT(o) FROM OrderEntity o
        WHERE o.status = :status
    """)
    long countByStatus(@Param("status") OrderStatus status);

    /**
     * Tính tổng doanh thu theo thời gian (chỉ lấy các đơn đã thanh toán)
     */
    @Query("""
        SELECT SUM(o.totalAmount)
        FROM OrderEntity o
        WHERE o.paymentStatus = 'PAID'
        AND o.orderDate BETWEEN :start AND :end
    """)
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    /**
     * Lấy đơn hàng kèm user (để gửi thông báo)
     */
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.user WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithUser(@Param("id") UUID id);
}
