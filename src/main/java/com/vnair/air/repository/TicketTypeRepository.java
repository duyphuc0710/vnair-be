package com.vnair.air.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vnair.air.model.TicketTypeModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketTypeModel, Integer> {
    
    /**
     * Find ticket types by flight ID through tickets
     */
    @Query("SELECT DISTINCT t.ticketType FROM TicketModel t WHERE t.flight.id = :flightId")
    List<TicketTypeModel> findByFlightId(@Param("flightId") Long flightId);
    
    /**
     * Find ticket types by flight ID with pagination through tickets
     */
    @Query("SELECT DISTINCT t.ticketType FROM TicketModel t WHERE t.flight.id = :flightId")
    Page<TicketTypeModel> findByFlightId(@Param("flightId") Long flightId, Pageable pageable);
    
    /**
     * Find ticket type by flight and class through tickets
     */
    @Query("SELECT DISTINCT t.ticketType FROM TicketModel t WHERE t.flight.id = :flightId AND t.ticketType.name = :ticketClass")
    Optional<TicketTypeModel> findByFlightIdAndTicketClass(@Param("flightId") Long flightId,
                                                           @Param("ticketClass") String ticketClass);
    
    /**
     * Find ticket types by price multiplier range
     */
    @Query("SELECT tt FROM TicketTypeModel tt WHERE tt.priceMultiplier BETWEEN :minMultiplier AND :maxMultiplier")
    Page<TicketTypeModel> findByPriceBetween(@Param("minMultiplier") BigDecimal minMultiplier,
                                             @Param("maxMultiplier") BigDecimal maxMultiplier,
                                             Pageable pageable);
    
    /**
     * Find all ticket types (no availableTickets field in new design)
     */
    @Query("SELECT tt FROM TicketTypeModel tt")
    Page<TicketTypeModel> findAvailableTicketTypes(Pageable pageable);
    
    /**
     * Find available ticket types by flight
     */
    /**
     * Find available ticket types by flight through tickets
     */
    @Query("SELECT DISTINCT t.ticketType FROM TicketModel t WHERE t.flight.id = :flightId AND t.status = 'AVAILABLE'")
    List<TicketTypeModel> findAvailableByFlightId(@Param("flightId") Long flightId);
    
    /**
     * Count available tickets for a flight through tickets
     */
    @Query("SELECT COUNT(t) FROM TicketModel t WHERE t.flight.id = :flightId AND t.status = 'AVAILABLE'")
    Integer countAvailableTicketsByFlightId(@Param("flightId") Long flightId);
}