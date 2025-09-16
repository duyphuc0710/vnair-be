package com.vnair.air.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vnair.air.dto.request.create.BookingCreateRequest;
import com.vnair.air.dto.request.update.BookingUpdateRequest;
import com.vnair.air.dto.response.BookingResponse;
import com.vnair.air.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    
    /**
     * Create new booking
     */
    BookingResponse createBooking(BookingCreateRequest request);
    
    /**
     * Update existing booking
     */
    BookingResponse updateBooking(Long id, BookingUpdateRequest request);
    
    /**
     * Get booking by ID
     */
    BookingResponse getBookingById(Long id);
    
    /**
     * Get all bookings with pagination
     */
    Page<BookingResponse> getAllBookings(Pageable pageable);

    /**
     * Delete booking by ID
     */
    void deleteBooking(Long id);
    
    /**
     * Get bookings by customer email
     */
    Page<BookingResponse> getBookingsByCustomerEmail(String customerEmail, Pageable pageable);
    
    /**
     * Get bookings by customer phone
     */
    Page<BookingResponse> getBookingsByCustomerPhone(String customerPhone, Pageable pageable);
    
    /**
     * Get bookings by username (for current logged-in user)
     */
    List<BookingResponse> getBookingsByUsername(String username);
    
    /**
     * Get bookings by user ID (for current logged-in user)
     */
    List<BookingResponse> getBookingsByUserId(String username);
    
    /**
     * Search bookings by customer name
     */
    Page<BookingResponse> searchBookingsByCustomerName(String customerName, Pageable pageable);
    
    /**
     * Get bookings by status
     */
    Page<BookingResponse> getBookingsByStatus(BookingStatus status, Pageable pageable);
    
    /**
     * Get bookings created between dates
     */
    Page<BookingResponse> getBookingsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);
 
    
    /**
     * Update booking status
     */
    BookingResponse updateBookingStatus(Long id, BookingStatus status);
    
    /**
     * Cancel booking
     */
    BookingResponse cancelBooking(Long id);
    
    /**
     * Generate booking reference
     */
    String generateBookingReference();
    
    /**
     * Calculate booking total amount
     */
    BigDecimal calculateBookingTotal(Long bookingId);
    
}