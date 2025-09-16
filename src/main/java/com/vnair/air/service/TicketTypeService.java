package com.vnair.air.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vnair.air.dto.request.create.TicketTypeCreateRequest;
import com.vnair.air.dto.request.update.TicketTypeUpdateRequest;
import com.vnair.air.dto.response.TicketTypeResponse;

public interface TicketTypeService {
    
    /**
     * Create new ticket type
     */
    TicketTypeResponse createTicketType(TicketTypeCreateRequest request);
    
    /**
     * Update existing ticket type
     */
    TicketTypeResponse updateTicketType(Integer id, TicketTypeUpdateRequest request);
    
    /**
     * Get ticket type by ID
     */
    TicketTypeResponse getTicketTypeById(Integer id);
    
    /**
     * Get all ticket types with pagination
     */
    Page<TicketTypeResponse> getAllTicketTypes(Pageable pageable);
    
    /**
     * Delete ticket type by ID
     */
    void deleteTicketType(Integer id);
    
    
    /**
     * Update available tickets count
     */
    TicketTypeResponse updateAvailableTickets(Integer id, Integer newCount);
      
    /**
     * Release tickets (increase available count)
     */
    TicketTypeResponse releaseTickets(Integer id, Integer quantity);
    
    /**
     * Get total available tickets for a flight
     */
    Integer getTotalAvailableTicketsByFlight(Long flightId);
}