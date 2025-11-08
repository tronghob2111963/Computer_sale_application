package com.trong.Computer_sell.repository;


import com.trong.Computer_sell.common.PaymentStatus;
import com.trong.Computer_sell.model.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    List<PaymentEntity> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT p FROM PaymentEntity p WHERE p.paymentDate BETWEEN :start AND :end")
    List<PaymentEntity> findPaymentsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.paymentStatus = 'SUCCESS'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COUNT(p) FROM PaymentEntity p WHERE p.paymentStatus = 'SUCCESS'")
    Long getTotalTransactions();

    @Query("SELECT FUNCTION('MONTH', p.paymentDate), COALESCE(SUM(p.amount), 0) FROM PaymentEntity p " +
            "WHERE FUNCTION('YEAR', p.paymentDate) = :year AND p.paymentStatus = 'SUCCESS' " +
            "GROUP BY FUNCTION('MONTH', p.paymentDate) ORDER BY FUNCTION('MONTH', p.paymentDate)")
    List<Object[]> getMonthlyRevenueByYear(@Param("year") int year);

    @Query("SELECT p.paymentMethod, COALESCE(SUM(p.amount), 0) FROM PaymentEntity p WHERE p.paymentStatus = 'SUCCESS' GROUP BY p.paymentMethod")
    List<Object[]> getRevenueByPaymentMethod();

    @Query("""
        SELECT p FROM PaymentEntity p
        LEFT JOIN p.order o
        LEFT JOIN o.user u
        WHERE (:keyword IS NULL OR 
               LOWER(p.paymentMethod) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(p.transactionId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR p.paymentStatus = :status)
          AND (:start IS NULL OR p.paymentDate >= :start)
          AND (:end IS NULL OR p.paymentDate <= :end)
        """)
    Page<PaymentEntity> searchPayments(
            @Param("keyword") String keyword,
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}