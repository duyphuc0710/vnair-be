package com.vnair.air.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vnair.air.dto.request.create.TicketCreateRequest;
import com.vnair.air.dto.request.update.TicketUpdateRequest;
import com.vnair.air.dto.response.TicketResponse;
import com.vnair.air.enums.TicketStatus;
import com.vnair.air.exception.FlightNotFoundException;
import com.vnair.air.exception.TicketNotFoundException;
import com.vnair.air.model.FlightModel;
import com.vnair.air.model.TicketModel;
import com.vnair.air.model.TicketTypeModel;
import com.vnair.air.repository.FlightRepository;
import com.vnair.air.repository.TicketRepository;
import com.vnair.air.repository.TicketTypeRepository;
import com.vnair.air.service.TicketService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Override
    public TicketResponse createTicket(TicketCreateRequest request) {
        log.info("Creating ticket for flight: {}", request.getFlightId());

        // Validate flight exists
        FlightModel flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + request.getFlightId()));

        // Validate ticket type exists
        TicketTypeModel ticketType = ticketTypeRepository.findById(request.getTicketTypeId())
                .orElseThrow(() -> new FlightNotFoundException(
                        "TicketType not found with ID: " + request.getTicketTypeId()));

        TicketModel ticket = new TicketModel();
        ticket.setFlight(flight);
        ticket.setTicketType(ticketType);
        ticket.setSeatNumber(request.getSeatNumber());

        // Set status from request or default to AVAILABLE
        if (request.getStatus() != null) {
            try {
                TicketStatus status = TicketStatus.valueOf(request.getStatus().toUpperCase());
                ticket.setStatus(status);
            } catch (IllegalArgumentException e) {
                ticket.setStatus(TicketStatus.AVAILABLE);
            }
        } else {
            ticket.setStatus(TicketStatus.AVAILABLE);
        }

        TicketModel savedTicket = ticketRepository.save(ticket);
        log.info("Successfully created ticket with ID: {}", savedTicket.getId());

        return mapToResponse(savedTicket);
    }

    @Override
    public TicketResponse updateTicket(Long id, TicketUpdateRequest request) {
        log.info("Updating ticket with ID: {}", id);

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));

        if (request.getSeatNumber() != null) {
            ticket.setSeatNumber(request.getSeatNumber());
        }

        TicketModel savedTicket = ticketRepository.save(ticket);
        log.info("Successfully updated ticket with ID: {}", savedTicket.getId());

        return mapToResponse(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        log.info("Getting ticket by ID: {}", id);

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));

        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        log.info("Getting all tickets with pagination");

        Page<TicketModel> tickets = ticketRepository.findAll(pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    public void deleteTicket(Long id) {
        log.info("Deleting ticket with ID: {}", id);

        if (!ticketRepository.existsById(id)) {
            throw new TicketNotFoundException("Ticket not found with ID: " + id);
        }

        ticketRepository.deleteById(id);
        log.info("Successfully deleted ticket with ID: {}", id);
    }

    /**
     * Xem vé của chuyến bay theo business logic từ service.md:
     * Customer: chỉ vé AVAILABLE
     * Manager: tất cả vé của flight
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByFlightForCustomer(Long flightId) {
        log.info("Getting AVAILABLE tickets for flight: {} (Customer view)", flightId);

        // Get available tickets for customer
        Page<TicketModel> availableTickets = ticketRepository.findByStatus(TicketStatus.AVAILABLE, Pageable.unpaged());
        return availableTickets.getContent().stream()
                .filter(ticket -> ticket.getFlight() != null && ticket.getFlight().getId().equals(flightId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByFlightForManager(Long flightId) {
        log.info("Getting ALL tickets for flight: {} (Manager view)", flightId);

        // Manager sees all tickets regardless of status
        List<TicketModel> allTickets = ticketRepository.findAll();
        return allTickets.stream()
                .filter(ticket -> ticket.getFlight() != null && ticket.getFlight().getId().equals(flightId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isSeatAvailable(String seatNumber, Long flightId) {
        log.info("Checking seat availability: {} for flight: {}", seatNumber, flightId);

        return ticketRepository.findBySeatNumberAndFlightId(seatNumber, flightId).isEmpty();
    }

    /**
     * - BOOKED khi booking → save()
     * - PAID khi payment success → save()
     * - AVAILABLE khi booking bị hủy → save()
     */
    @Override
    public TicketResponse updateTicketStatus(Long id, TicketStatus status) {
        log.info("Updating ticket status for ID: {} to {}", id, status);

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(status);

        TicketModel savedTicket = ticketRepository.save(ticket);
        log.info("Updated ticket {} status from {} to {}", id, oldStatus, status);

        return mapToResponse(savedTicket);
    }

    @Override
    public TicketResponse cancelTicket(Long id) {
        log.info("Canceling ticket with ID: {}", id);
        return updateTicketStatus(id, TicketStatus.CANCELED);
    }

    @Override
    public TicketResponse checkInTicket(Long id) {
        log.info("Checking in ticket with ID: {}", id);

        return updateTicketStatus(id, TicketStatus.PAID);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean validateTicketForCheckIn(Long id) {
        log.info("Validating ticket for check-in: {}", id);

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));

        return ticket.getStatus() == TicketStatus.PAID &&
                ticket.getFlight() != null &&
                ticket.getFlight().getDepartureTime().after(new java.util.Date());
    }

    /**
     * Batch operations for booking process
     */
    @Transactional
    public void updateTicketsStatusBatch(List<Long> ticketIds, TicketStatus status) {
        log.info("Batch updating {} tickets to status: {}", ticketIds.size(), status);

        List<TicketModel> tickets = ticketRepository.findAllById(ticketIds);
        tickets.forEach(ticket -> ticket.setStatus(status));

        ticketRepository.saveAll(tickets);
        log.info("Successfully updated {} tickets to status: {}", tickets.size(), status);
    }

    @Override
    @Transactional
    public void generateTicketsForFlight(Long flightId) {
        log.info("Generating tickets for flight ID: {}", flightId);

        // Get flight information
        FlightModel flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found with ID: " + flightId));

        // Get all available ticket types
        List<TicketTypeModel> ticketTypes = ticketTypeRepository.findAll();
        if (ticketTypes.isEmpty()) {
            log.warn("No ticket types found, cannot generate tickets");
            return;
        }

        // Get airplane seat capacity
        Integer seatCapacity = flight.getAirplane().getSeatCapacity();
        if (seatCapacity == null || seatCapacity <= 0) {
            log.warn("Invalid seat capacity for airplane, cannot generate tickets");
            return;
        }

        List<TicketModel> ticketsToCreate = new ArrayList<>();

        // Generate tickets for each seat and each ticket type
        for (int seatNumber = 1; seatNumber <= seatCapacity; seatNumber++) {
            for (TicketTypeModel ticketType : ticketTypes) {
                TicketModel ticket = new TicketModel();
                ticket.setFlight(flight);
                ticket.setTicketType(ticketType);
                ticket.setSeatNumber(String.format("%03d", seatNumber)); // Format as "001", "002", etc.
                ticket.setStatus(TicketStatus.AVAILABLE);

                ticketsToCreate.add(ticket);
            }
        }

        // Save all tickets in batch
        ticketRepository.saveAll(ticketsToCreate);
        log.info("Successfully generated {} tickets for flight ID: {}", ticketsToCreate.size(), flightId);
    }

    @Override
    @Transactional
    public void updateTicketStatusByFlight(Long flightId, TicketStatus status) {
        log.info("Updating all tickets for flight ID: {} to status: {}", flightId, status);

        List<TicketModel> tickets = ticketRepository.findByFlightId(flightId);

        for (TicketModel ticket : tickets) {
            ticket.setStatus(status);
        }

        ticketRepository.saveAll(tickets);
        log.info("Successfully updated {} tickets for flight ID: {} to status: {}", tickets.size(), flightId, status);
    }

    private TicketResponse mapToResponse(TicketModel ticket) {
        return TicketResponse.builder()
                .flight(ticket.getFlight() != null ? ticket.getFlight().getDepartureAirport().getCode() + "-"
                        + ticket.getFlight().getArrivalAirport().getCode() : null)
                .ticketType(ticket.getTicketType() != null ? ticket.getTicketType().getName() : null)
                .seatNumber(ticket.getSeatNumber())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}