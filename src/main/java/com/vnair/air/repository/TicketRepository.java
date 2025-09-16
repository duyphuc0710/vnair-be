package com.vnair.air.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vnair.air.enums.TicketStatus;
import com.vnair.air.model.TicketModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketModel, Long> {
    
    /**
     * Find tickets by booking ID through BookingTicketModel
     */
    @Query("SELECT bt.ticket FROM BookingTicketModel bt WHERE bt.booking.id = :bookingId")
    List<TicketModel> findByBookingId(@Param("bookingId") Long bookingId);
    
    /**
     * Find tickets by booking ID with pagination through BookingTicketModel
     */
    @Query("SELECT bt.ticket FROM BookingTicketModel bt WHERE bt.booking.id = :bookingId")
    Page<TicketModel> findByBookingId(@Param("bookingId") Long bookingId, Pageable pageable);
    
    /**
     * Find tickets by ticket type
     */
    Page<TicketModel> findByTicketTypeId(Integer ticketTypeId, Pageable pageable);
    
    /**
     * Find tickets by flight ID
     */
    List<TicketModel> findByFlightId(Long flightId);
    
    /**
     * Find tickets by status
     */
    Page<TicketModel> findByStatus(TicketStatus status, Pageable pageable);
    
    /**
     * Find ticket by seat number and flight
     */
    @Query("SELECT t FROM TicketModel t WHERE t.seatNumber = :seatNumber " +
           "AND t.flight.id = :flightId")
    Optional<TicketModel> findBySeatNumberAndFlightId(@Param("seatNumber") String seatNumber,
                                                      @Param("flightId") Long flightId);
    
    /**
     * Find tickets by passenger name through booking
     */
    @Query("SELECT bt.ticket FROM BookingTicketModel bt JOIN bt.booking b JOIN b.user u " +
           "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :passengerName, '%'))")
    Page<TicketModel> findByPassengerNameContaining(@Param("passengerName") String passengerName, Pageable pageable);
    
    /**
     * Find tickets by passenger phone through booking
     */
    @Query("SELECT bt.ticket FROM BookingTicketModel bt JOIN bt.booking b JOIN b.user u " +
           "WHERE u.phone = :passengerPhone")
    Page<TicketModel> findByPassengerPhone(@Param("passengerPhone") String passengerPhone, Pageable pageable);
    
    /**
     * Find tickets by passenger email through booking
     */
    @Query("SELECT bt.ticket FROM BookingTicketModel bt JOIN bt.booking b JOIN b.user u " +
           "WHERE LOWER(u.email) = LOWER(:passengerEmail)")
    Page<TicketModel> findByPassengerEmail(@Param("passengerEmail") String passengerEmail, Pageable pageable);
    
    /**
     * Count tickets by booking ID through BookingTicketModel
     */
    @Query("SELECT COUNT(bt.ticket) FROM BookingTicketModel bt WHERE bt.booking.id = :bookingId")
    Long countByBookingId(@Param("bookingId") Long bookingId);
    
    /**
     * Find taken seats for a flight
     */
    @Query("SELECT t.seatNumber FROM TicketModel t WHERE t.flight.id = :flightId " +
           "AND t.status IN ('BOOKED', 'CHECKED_IN')")
    List<String> findTakenSeatsByFlightId(@Param("flightId") Long flightId);
}