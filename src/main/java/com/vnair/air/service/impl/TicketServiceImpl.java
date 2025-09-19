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

        // Validate and fetch entities
        FlightModel flight = validateAndFetchFlight(request.getFlightId());
        TicketTypeModel ticketType = validateAndFetchTicketType(request.getTicketTypeId());

        // Create and save ticket entity
        TicketModel ticket = createTicketEntity(flight, ticketType, request);
        TicketModel savedTicket = ticketRepository.save(ticket);
        
        log.info("Successfully created ticket with ID: {}", savedTicket.getId());
        return mapToResponse(savedTicket);
    }

    @Override
    public TicketResponse updateTicket(Long id, TicketUpdateRequest request) {
        log.info("Updating ticket with ID: {}", id);

        TicketModel ticket = validateAndFetchTicket(id);
        updateTicketFields(ticket, request);
        
        TicketModel savedTicket = ticketRepository.save(ticket);
        log.info("Successfully updated ticket with ID: {}", savedTicket.getId());

        return mapToResponse(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        log.info("Getting ticket by ID: {}", id);

        TicketModel ticket = validateAndFetchTicket(id);
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

        validateTicketExists(id);
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

        List<TicketModel> availableTickets = getTicketsByFlightAndStatus(flightId, TicketStatus.AVAILABLE);
        return convertListToResponses(availableTickets);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByFlightForManager(Long flightId) {
        log.info("Getting ALL tickets for flight: {} (Manager view)", flightId);

        List<TicketModel> allTickets = getTicketsByFlight(flightId);
        return convertListToResponses(allTickets);
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

        TicketModel ticket = validateAndFetchTicket(id);
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

        TicketModel ticket = validateAndFetchTicket(id);
        return isTicketValidForCheckIn(ticket);
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

        // Validate and fetch flight
        FlightModel flight = validateAndFetchFlight(flightId);
        
        // Validate prerequisites for ticket generation
        validateTicketGenerationPrerequisites(flight);
        
        // Generate tickets
        List<TicketModel> ticketsToCreate = createTicketsForFlight(flight);
        
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

    // =========================== HELPER METHODS ===========================
    // Entity fetching, validation, creation, updates, business logic utilities

    private FlightModel validateAndFetchFlight(Long flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));
    }

    private TicketTypeModel validateAndFetchTicketType(Integer ticketTypeId) {
        return ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new FlightNotFoundException("TicketType not found with ID: " + ticketTypeId));
    }

    private TicketModel validateAndFetchTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
    }

    private void validateTicketExists(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new TicketNotFoundException("Ticket not found with ID: " + ticketId);
        }
    }

    private TicketModel createTicketEntity(FlightModel flight, TicketTypeModel ticketType, TicketCreateRequest request) {
        TicketModel ticket = new TicketModel();
        ticket.setFlight(flight);
        ticket.setTicketType(ticketType);
        ticket.setSeatNumber(request.getSeatNumber());
        ticket.setStatus(parseTicketStatus(request.getStatus()));
        return ticket;
    }

    private TicketStatus parseTicketStatus(String statusString) {
        if (statusString != null) {
            try {
                return TicketStatus.valueOf(statusString.toUpperCase());
            } catch (IllegalArgumentException e) {
                return TicketStatus.AVAILABLE;
            }
        }
        return TicketStatus.AVAILABLE;
    }

    private void updateTicketFields(TicketModel ticket, TicketUpdateRequest request) {
        if (request.getSeatNumber() != null) {
            ticket.setSeatNumber(request.getSeatNumber());
        }
    }

    private List<TicketModel> getTicketsByFlightAndStatus(Long flightId, TicketStatus status) {
        Page<TicketModel> ticketsPage = ticketRepository.findByStatus(status, Pageable.unpaged());
        return ticketsPage.getContent().stream()
                .filter(ticket -> ticket.getFlight() != null && ticket.getFlight().getId().equals(flightId))
                .collect(Collectors.toList());
    }

    private List<TicketModel> getTicketsByFlight(Long flightId) {
        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getFlight() != null && ticket.getFlight().getId().equals(flightId))
                .collect(Collectors.toList());
    }

    private Boolean isTicketValidForCheckIn(TicketModel ticket) {
        return ticket.getStatus() == TicketStatus.PAID &&
                ticket.getFlight() != null &&
                ticket.getFlight().getDepartureTime().after(new java.util.Date());
    }

    private void validateTicketGenerationPrerequisites(FlightModel flight) {
        List<TicketTypeModel> ticketTypes = ticketTypeRepository.findAll();
        if (ticketTypes.isEmpty()) {
            log.warn("No ticket types found, cannot generate tickets");
            throw new RuntimeException("No ticket types available");
        }

        Integer seatCapacity = flight.getAirplane().getSeatCapacity();
        if (seatCapacity == null || seatCapacity <= 0) {
            log.warn("Invalid seat capacity for airplane, cannot generate tickets");
            throw new RuntimeException("Invalid seat capacity");
        }
    }

    private List<TicketModel> createTicketsForFlight(FlightModel flight) {
        List<TicketTypeModel> ticketTypes = ticketTypeRepository.findAll();
        Integer seatCapacity = flight.getAirplane().getSeatCapacity();
        List<TicketModel> ticketsToCreate = new ArrayList<>();

        for (int seatNumber = 1; seatNumber <= seatCapacity; seatNumber++) {
            for (TicketTypeModel ticketType : ticketTypes) {
                TicketModel ticket = new TicketModel();
                ticket.setFlight(flight);
                ticket.setTicketType(ticketType);
                ticket.setSeatNumber(String.format("%03d", seatNumber));
                ticket.setStatus(TicketStatus.AVAILABLE);
                ticketsToCreate.add(ticket);
            }
        }

        return ticketsToCreate;
    }

    private List<TicketResponse> convertListToResponses(List<TicketModel> tickets) {
        return tickets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}