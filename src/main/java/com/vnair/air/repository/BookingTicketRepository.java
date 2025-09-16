package com.vnair.air.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vnair.air.model.BookingTicketModel;

import java.util.List;

@Repository
public interface BookingTicketRepository extends JpaRepository<BookingTicketModel, Long> {
    
    /**
     * Find booking tickets by booking ID
     */
    List<BookingTicketModel> findByBookingId(Long bookingId);
    
    /**
     * Find booking tickets by booking ID with pagination
     */
    Page<BookingTicketModel> findByBookingId(Long bookingId, Pageable pageable);
    
    /**
     * Find booking tickets by ticket ID
     */
    List<BookingTicketModel> findByTicketId(Long ticketId);
    
    /**
     * Find booking tickets by ticket ID with pagination
     */
    Page<BookingTicketModel> findByTicketId(Long ticketId, Pageable pageable);
    
    /**
     * Check if booking-ticket relationship exists
     */
    @Query("SELECT COUNT(bt) > 0 FROM BookingTicketModel bt WHERE bt.booking.id = :bookingId AND bt.ticket.id = :ticketId")
    Boolean existsByBookingIdAndTicketId(@Param("bookingId") Long bookingId, @Param("ticketId") Long ticketId);
    
    /**
     * Count tickets in a booking
     */
    Long countByBookingId(Long bookingId);
    
    /**
     * Find all tickets for a specific flight through bookings
     */
    @Query("SELECT bt FROM BookingTicketModel bt WHERE bt.ticket.flight.id = :flightId")
    List<BookingTicketModel> findByFlightId(@Param("flightId") Long flightId);
    
    /**
     * Find booking tickets by flight with pagination
     */
    @Query("SELECT bt FROM BookingTicketModel bt WHERE bt.ticket.flight.id = :flightId")
    Page<BookingTicketModel> findByFlightId(@Param("flightId") Long flightId, Pageable pageable);
    
    /**
     * Delete booking tickets by booking ID
     */
    void deleteByBookingId(Long bookingId);
    
    /**
     * Delete booking tickets by ticket ID
     */
    void deleteByTicketId(Long ticketId);
}