package com.vnair.air.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vnair.air.model.BookingModel;

import java.util.Date;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingModel, Long> {
    
    /**
     * Find bookings by user email
     */
    @Query("SELECT b FROM BookingModel b WHERE b.user.email = :email")
    Page<BookingModel> findByUserEmail(@Param("email") String email, Pageable pageable);
    
    /**
     * Find bookings by user phone
     */
    @Query("SELECT b FROM BookingModel b WHERE b.user.phone = :phone")
    Page<BookingModel> findByUserPhone(@Param("phone") String phone, Pageable pageable);
    
    /**
     * Find bookings by user full name
     */
    @Query("SELECT b FROM BookingModel b WHERE LOWER(b.user.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    Page<BookingModel> findByUserFullNameContainingIgnoreCase(@Param("fullName") String fullName, Pageable pageable);
    
    /**
     * Find bookings created between dates
     */
    @Query("SELECT b FROM BookingModel b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    Page<BookingModel> findByBookingDateBetween(@Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                Pageable pageable);
    
    /**
     * Find bookings by user ID
     */
    Page<BookingModel> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find all bookings by user ID (without pagination)
     */
    List<BookingModel> findByUserId(Long userId);
    
    /**
     * Find recent bookings (last 30 days)
     */
    @Query("SELECT b FROM BookingModel b WHERE b.bookingDate >= :thirtyDaysAgo ORDER BY b.bookingDate DESC")
    Page<BookingModel> findRecentBookings(@Param("thirtyDaysAgo") Date thirtyDaysAgo, Pageable pageable);
}