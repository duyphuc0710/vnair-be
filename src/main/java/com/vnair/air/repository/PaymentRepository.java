package com.vnair.air.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vnair.air.enums.PaymentMethod;
import com.vnair.air.enums.PaymentStatus;
import com.vnair.air.model.PaymentModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentModel, Long> {
    
    /**
     * Find payments by booking ID
     */
    List<PaymentModel> findByBookingId(Long bookingId);
    
    /**
     * Find payments by multiple booking IDs
     */
    List<PaymentModel> findByBookingIdIn(List<Long> bookingIds);
    
    /**
     * Find payments by booking ID with pagination
     */
    Page<PaymentModel> findByBookingId(Long bookingId, Pageable pageable);
    
    /**
     * Find payments by status
     */
    Page<PaymentModel> findByStatus(PaymentStatus status, Pageable pageable);
    
    /**
     * Find payments by payment method
     */
    Page<PaymentModel> findByMethod(PaymentMethod method, Pageable pageable);
    
    /**
     * Find payments by transaction ID
     */
    List<PaymentModel> findByTransactionId(String transactionId);
    
    /**
     * Find payments created between dates
     */
    @Query("SELECT p FROM PaymentModel p WHERE DATE(p.createdAt) BETWEEN :startDate AND :endDate")
    Page<PaymentModel> findByCreatedAtBetween(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              Pageable pageable);
    
    /**
     * Find payments by amount range
     */
    @Query("SELECT p FROM PaymentModel p WHERE p.amount BETWEEN :minAmount AND :maxAmount")
    Page<PaymentModel> findByAmountBetween(@Param("minAmount") BigDecimal minAmount,
                                           @Param("maxAmount") BigDecimal maxAmount,
                                           Pageable pageable);
    
    /**
     * Find successful payments for a booking
     */
    @Query("SELECT p FROM PaymentModel p WHERE p.booking.id = :bookingId AND p.status = 'SUCCESS'")
    List<PaymentModel> findSuccessfulPaymentsByBookingId(@Param("bookingId") Long bookingId);
    
    /**
     * Calculate total paid amount for a booking
     */
    @Query("SELECT SUM(p.amount) FROM PaymentModel p WHERE p.booking.id = :bookingId AND p.status = 'SUCCESS'")
    BigDecimal getTotalPaidAmountByBookingId(@Param("bookingId") Long bookingId);
    
    /**
     * Find pending payments (created more than 30 minutes ago)
     */
    @Query("SELECT p FROM PaymentModel p WHERE p.status = 'PENDING' AND p.createdAt < :thirtyMinutesAgo")
    List<PaymentModel> findExpiredPendingPayments(@Param("thirtyMinutesAgo") Date thirtyMinutesAgo);
    
    /**
     * Count payments by status
     */
    Long countByStatus(PaymentStatus status);
    
    /**
     * Find daily payment summary
     */
    @Query("SELECT DATE(p.createdAt) as paymentDate, COUNT(p) as totalPayments, SUM(p.amount) as totalAmount " +
           "FROM PaymentModel p WHERE p.status = 'SUCCESS' AND DATE(p.createdAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt)")
    List<Object[]> findDailyPaymentSummary(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}