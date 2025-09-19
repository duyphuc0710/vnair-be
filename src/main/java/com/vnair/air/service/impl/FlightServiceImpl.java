package com.vnair.air.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vnair.air.dto.request.create.FlightCreateRequest;
import com.vnair.air.dto.request.update.FlightUpdateRequest;
import com.vnair.air.dto.response.FlightResponse;
import com.vnair.air.enums.FlightStatus;
import com.vnair.air.enums.TicketStatus;
import com.vnair.air.exception.AirplaneNotFoundException;
import com.vnair.air.exception.AirportNotFoundException;
import com.vnair.air.exception.FlightNotFoundException;
import com.vnair.air.model.AirplaneModel;
import com.vnair.air.model.AirportModel;
import com.vnair.air.model.FlightModel;
import com.vnair.air.repository.AirplaneRepository;
import com.vnair.air.repository.AirportRepository;
import com.vnair.air.repository.FlightRepository;
import com.vnair.air.service.FlightService;
import com.vnair.air.service.TicketService;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final AirplaneRepository airplaneRepository;
    private final AirportRepository airportRepository;
    private final TicketService ticketService;

    @Override
    public FlightResponse createFlight(FlightCreateRequest request) {
        log.info("Creating flight from airport {} to {}", request.getDepartureAirportId(), request.getArrivalAirportId());
        
        // Validate and fetch entities
        AirplaneModel airplane = validateAndFetchAirplane(request.getAirplaneId());
        AirportModel departureAirport = validateAndFetchDepartureAirport(request.getDepartureAirportId());
        AirportModel arrivalAirport = validateAndFetchArrivalAirport(request.getArrivalAirportId());
        
        // Validate business rules
        validateFlightTimes(request.getDepartureTime(), request.getArrivalTime());
        
        // Create and save flight entity
        FlightModel flight = createFlightEntity(airplane, departureAirport, arrivalAirport, request);
        FlightModel savedFlight = flightRepository.save(flight);
        log.info("Successfully created flight with ID: {}", savedFlight.getId());
        
        // Handle side effects
        handleTicketGenerationForNewFlight(savedFlight.getId());
        
        return mapToResponse(savedFlight);
    }

    @Override
    public FlightResponse updateFlight(Long id, FlightUpdateRequest request) {
        log.info("Updating flight with ID: {}", id);
        
        // Validate and fetch flight
        FlightModel flight = validateAndFetchFlight(id);
        
        // Update airplane if provided
        updateFlightAirplane(flight, request.getAirplaneId());
        
        // Update airports if provided
        updateFlightDepartureAirport(flight, request.getDepartureAirportId());
        updateFlightArrivalAirport(flight, request.getArrivalAirportId());
        
        // Update times if provided
        updateFlightTimes(flight, request.getDepartureTime(), request.getArrivalTime());
        
        // Update other fields if provided
        updateFlightOtherFields(flight, request);
        
        // Validate updated flight times
        validateUpdatedFlightTimes(flight);
        
        FlightModel savedFlight = flightRepository.save(flight);
        log.info("Successfully updated flight with ID: {}", savedFlight.getId());
        
        return mapToResponse(savedFlight);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightById(Long id) {
        log.info("Getting flight by ID: {}", id);
        
        FlightModel flight = validateAndFetchFlight(id);
        
        return mapToResponse(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponse> getAllFlights(Pageable pageable) {
        log.info("Getting all flights with pagination");
        
        Page<FlightModel> flights = flightRepository.findAll(pageable);
        return flights.map(this::mapToResponse);
    }


    @Override
    public void deleteFlight(Long id) {
        log.info("Deleting flight with ID: {}", id);
        
        validateFlightExists(id);
        
        flightRepository.deleteById(id);
        log.info("Successfully deleted flight with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> searchFlights(Integer departureAirportId, Integer arrivalAirportId, LocalDate departureDate) {
        log.info("Searching flights from {} to {} on {}", departureAirportId, arrivalAirportId, departureDate);
        
        List<FlightModel> flights = flightRepository.searchFlights(
                departureAirportId, arrivalAirportId, departureDate, FlightStatus.SCHEDULED);
        
        return flights.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponse> getFlightsByAirplane(Integer airplaneId, Pageable pageable) {
        log.info("Getting flights by airplane ID: {}", airplaneId);
        
        Page<FlightModel> flights = flightRepository.findByAirplaneId(airplaneId, pageable);
        return flights.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponse> getFlightsByDepartureAirport(Integer departureAirportId, Pageable pageable) {
        log.info("Getting flights by departure airport ID: {}", departureAirportId);
        
        Page<FlightModel> flights = flightRepository.findByDepartureAirportId(departureAirportId, pageable);
        return flights.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponse> getFlightsByArrivalAirport(Integer arrivalAirportId, Pageable pageable) {
        log.info("Getting flights by arrival airport ID: {}", arrivalAirportId);
        
        Page<FlightModel> flights = flightRepository.findByArrivalAirportId(arrivalAirportId, pageable);
        return flights.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponse> getFlightsByStatus(FlightStatus status, Pageable pageable) {
        log.info("Getting flights by status: {}", status);
        
        Page<FlightModel> flights = flightRepository.findByStatus(status, pageable);
        return flights.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlightResponse> getFlightsByDepartureTimeBetween(Date startDate, Date endDate, Pageable pageable) {
        log.info("Getting flights departing between {} and {}", startDate, endDate);
        
        Page<FlightModel> flights = flightRepository.findByDepartureTimeBetween(startDate, endDate, pageable);
        return flights.map(this::mapToResponse);
    }



    @Override
    public FlightResponse updateFlightStatus(Long id, FlightStatus status) {
        log.info("Updating flight status for ID: {} to {}", id, status);
        
        FlightModel flight = validateAndFetchFlight(id);
        FlightStatus oldStatus = flight.getStatus();
        
        // Update status
        flight.setStatus(status);
        FlightModel savedFlight = flightRepository.save(flight);
        
        // Handle status change side effects
        handleFlightStatusChangeEffects(savedFlight.getId(), status, oldStatus);
        
        return mapToResponse(savedFlight);
    }

    @Override
    public FlightResponse cancelFlight(Long id) {
        log.info("Canceling flight with ID: {}", id);
        return updateFlightStatus(id, FlightStatus.CANCELED);
    }



    private FlightResponse mapToResponse(FlightModel flight) {
        return FlightResponse.builder()
                .airplane(flight.getAirplane() != null ? flight.getAirplane().getModel() : null)
                .departureAirport(flight.getDepartureAirport() != null ? flight.getDepartureAirport().getCode() : null)
                .arrivalAirport(flight.getArrivalAirport() != null ? flight.getArrivalAirport().getCode() : null)
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .basePrice(flight.getBasePrice())
                .status(flight.getStatus())
                .createdAt(flight.getCreatedAt())
                .updatedAt(flight.getUpdatedAt())
                .build();
    }

    // =========================== HELPER METHODS ===========================
    // Entity fetching, validation, creation, updates, side effects handling

    private AirplaneModel validateAndFetchAirplane(Integer airplaneId) {
        return airplaneRepository.findById(airplaneId)
                .orElseThrow(() -> new AirplaneNotFoundException("Airplane not found with ID: " + airplaneId));
    }

    private AirportModel validateAndFetchDepartureAirport(Integer departureAirportId) {
        return airportRepository.findById(departureAirportId)
                .orElseThrow(() -> new AirportNotFoundException("Departure airport not found with ID: " + departureAirportId));
    }

    private AirportModel validateAndFetchArrivalAirport(Integer arrivalAirportId) {
        return airportRepository.findById(arrivalAirportId)
                .orElseThrow(() -> new AirportNotFoundException("Arrival airport not found with ID: " + arrivalAirportId));
    }

    private FlightModel validateAndFetchFlight(Long flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));
    }

    private void validateFlightTimes(Date departureTime, Date arrivalTime) {
        if (departureTime.after(arrivalTime)) {
            throw new IllegalArgumentException("Departure time must be before arrival time");
        }
    }

    private FlightModel createFlightEntity(AirplaneModel airplane, AirportModel departureAirport, 
                                         AirportModel arrivalAirport, FlightCreateRequest request) {
        FlightModel flight = new FlightModel();
        flight.setAirplane(airplane);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setBasePrice(request.getBasePrice());
        flight.setStatus(FlightStatus.SCHEDULED);
        return flight;
    }

    private void handleTicketGenerationForNewFlight(Long flightId) {
        try {
            ticketService.generateTicketsForFlight(flightId);
            log.info("Successfully generated tickets for flight ID: {}", flightId);
        } catch (Exception e) {
            log.error("Failed to generate tickets for flight ID: {}", flightId, e);
        }
    }

    private void updateFlightAirplane(FlightModel flight, Integer airplaneId) {
        if (airplaneId != null) {
            AirplaneModel airplane = validateAndFetchAirplane(airplaneId);
            flight.setAirplane(airplane);
        }
    }

    private void updateFlightDepartureAirport(FlightModel flight, Integer departureAirportId) {
        if (departureAirportId != null) {
            AirportModel departureAirport = validateAndFetchDepartureAirport(departureAirportId);
            flight.setDepartureAirport(departureAirport);
        }
    }

    private void updateFlightArrivalAirport(FlightModel flight, Integer arrivalAirportId) {
        if (arrivalAirportId != null) {
            AirportModel arrivalAirport = validateAndFetchArrivalAirport(arrivalAirportId);
            flight.setArrivalAirport(arrivalAirport);
        }
    }

    private void updateFlightTimes(FlightModel flight, Date departureTime, Date arrivalTime) {
        if (departureTime != null) {
            flight.setDepartureTime(departureTime);
        }
        
        if (arrivalTime != null) {
            flight.setArrivalTime(arrivalTime);
        }
    }

    private void updateFlightOtherFields(FlightModel flight, FlightUpdateRequest request) {
        if (request.getBasePrice() != null) {
            flight.setBasePrice(request.getBasePrice());
        }
        
        if (request.getStatus() != null) {
            flight.setStatus(request.getStatus());
        }
    }

    private void validateUpdatedFlightTimes(FlightModel flight) {
        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
            if (flight.getDepartureTime().after(flight.getArrivalTime())) {
                throw new IllegalArgumentException("Departure time must be before arrival time");
            }
        }
    }

    private void validateFlightExists(Long flightId) {
        if (!flightRepository.existsById(flightId)) {
            throw new FlightNotFoundException("Flight not found with ID: " + flightId);
        }
    }

    private void handleFlightStatusChangeEffects(Long flightId, FlightStatus newStatus, FlightStatus oldStatus) {
        if (newStatus == FlightStatus.CANCELED && oldStatus != FlightStatus.CANCELED) {
            handleFlightCancellationEffects(flightId);
        }
    }

    private void handleFlightCancellationEffects(Long flightId) {
        log.info("Flight {} has been canceled, handling related bookings and tickets", flightId);
        
        try {
            ticketService.updateTicketStatusByFlight(flightId, TicketStatus.AVAILABLE);
            log.info("Updated all tickets for flight {} to AVAILABLE status", flightId);
            log.info("Flight cancellation processing completed for flight {}", flightId);
        } catch (Exception e) {
            log.error("Error processing flight cancellation for flight {}", flightId, e);
        }
    }
}