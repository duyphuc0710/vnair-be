package com.vnair.air.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vnair.air.dto.request.create.TicketCreateRequest;
import com.vnair.air.dto.request.update.TicketUpdateRequest;
import com.vnair.air.dto.response.TicketResponse;
import com.vnair.air.enums.TicketStatus;

public interface TicketService {
    
    /**
     * Create new ticket
     */
    TicketResponse createTicket(TicketCreateRequest request);
    
    /**
     * Update existing ticket
     */
    TicketResponse updateTicket(Long id, TicketUpdateRequest request);
    
    /**
     * Get ticket by ID
     */
    TicketResponse getTicketById(Long id);
    
    /**
     * Get all tickets with pagination
     */
    Page<TicketResponse> getAllTickets(Pageable pageable);

    
    /**
     * Delete ticket by ID
     */
    void deleteTicket(Long id);
    
    /**
     * Check seat availability
     */
    Boolean isSeatAvailable(String seatNumber, Long flightId);
    
    /**
     * Update ticket status
     */
    TicketResponse updateTicketStatus(Long id, TicketStatus status);
    
    /**
     * Cancel ticket
     */
    TicketResponse cancelTicket(Long id);
    
    /**
     * Check-in ticket
     */
    TicketResponse checkInTicket(Long id);
    
    /**
     * Validate ticket for check-in
     */
    Boolean validateTicketForCheckIn(Long id);
    
    /**
     * Generate tickets automatically for a flight
     * Based on airplane seat capacity and available ticket types
     */
    void generateTicketsForFlight(Long flightId);
    
    /**
     * Update ticket status for all tickets of a specific flight
     */
    void updateTicketStatusByFlight(Long flightId, TicketStatus status);
}