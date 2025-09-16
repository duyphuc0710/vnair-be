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
        
        AirplaneModel airplane = airplaneRepository.findById(request.getAirplaneId())
                .orElseThrow(() -> new AirplaneNotFoundException("Airplane not found with ID: " + request.getAirplaneId()));
        
        AirportModel departureAirport = airportRepository.findById(request.getDepartureAirportId())
                .orElseThrow(() -> new AirportNotFoundException("Departure airport not found with ID: " + request.getDepartureAirportId()));
        
        AirportModel arrivalAirport = airportRepository.findById(request.getArrivalAirportId())
                .orElseThrow(() -> new AirportNotFoundException("Arrival airport not found with ID: " + request.getArrivalAirportId()));
        
        if (request.getDepartureTime().after(request.getArrivalTime())) {
            throw new AirportNotFoundException("Departure time must be before arrival time");
        }
        
        FlightModel flight = new FlightModel();
        flight.setAirplane(airplane);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setBasePrice(request.getBasePrice());
        flight.setStatus(FlightStatus.SCHEDULED);
        
        FlightModel savedFlight = flightRepository.save(flight);
        log.info("Successfully created flight with ID: {}", savedFlight.getId());
        
        // Generate tickets automatically based on airplane seat capacity and ticket types
        try {
            ticketService.generateTicketsForFlight(savedFlight.getId());
            log.info("Successfully generated tickets for flight ID: {}", savedFlight.getId());
        } catch (Exception e) {
            log.error("Failed to generate tickets for flight ID: {}", savedFlight.getId(), e);
            // Continue without failing the flight creation
        }
        
        return mapToResponse(savedFlight);
    }

    @Override
    public FlightResponse updateFlight(Long id, FlightUpdateRequest request) {
        log.info("Updating flight with ID: {}", id);
        
        FlightModel flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + id));
        
        if (request.getAirplaneId() != null) {
            AirplaneModel airplane = airplaneRepository.findById(request.getAirplaneId())
                    .orElseThrow(() -> new IllegalArgumentException("Airplane not found with ID: " + request.getAirplaneId()));
            flight.setAirplane(airplane);
        }
        
        if (request.getDepartureAirportId() != null) {
            AirportModel departureAirport = airportRepository.findById(request.getDepartureAirportId())
                    .orElseThrow(() -> new IllegalArgumentException("Departure airport not found with ID: " + request.getDepartureAirportId()));
            flight.setDepartureAirport(departureAirport);
        }
        
        if (request.getArrivalAirportId() != null) {
            AirportModel arrivalAirport = airportRepository.findById(request.getArrivalAirportId())
                    .orElseThrow(() -> new IllegalArgumentException("Arrival airport not found with ID: " + request.getArrivalAirportId()));
            flight.setArrivalAirport(arrivalAirport);
        }
        
        if (request.getDepartureTime() != null) {
            flight.setDepartureTime(request.getDepartureTime());
        }
        
        if (request.getArrivalTime() != null) {
            flight.setArrivalTime(request.getArrivalTime());
        }
        
        if (request.getBasePrice() != null) {
            flight.setBasePrice(request.getBasePrice());
        }
        
        if (request.getStatus() != null) {
            flight.setStatus(request.getStatus());
        }
        
        // Validate time logic if both times are set
        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
            if (flight.getDepartureTime().after(flight.getArrivalTime())) {
                throw new FlightNotFoundException("Departure time must be before arrival time");
            }
        }
        
        FlightModel savedFlight = flightRepository.save(flight);
        log.info("Successfully updated flight with ID: {}", savedFlight.getId());
        
        return mapToResponse(savedFlight);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightById(Long id) {
        log.info("Getting flight by ID: {}", id);
        
        FlightModel flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + id));
        
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
        
        if (!flightRepository.existsById(id)) {
            throw new FlightNotFoundException("Flight not found with ID: " + id);
        }
        
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
        
        FlightModel flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + id));
        
        FlightStatus oldStatus = flight.getStatus();
        flight.setStatus(status);
        
        FlightModel savedFlight = flightRepository.save(flight);
        
        // Handle status change business logic
        if (status == FlightStatus.CANCELED && oldStatus != FlightStatus.CANCELED) {
            log.info("Flight {} has been canceled, handling related bookings and tickets", id);
            
            try {
                ticketService.updateTicketStatusByFlight(id, TicketStatus.AVAILABLE);
                log.info("Updated all tickets for flight {} to AVAILABLE status", id);
                
               
                log.info("Flight cancellation processing completed for flight {}", id);
                
            } catch (Exception e) {
                log.error("Error processing flight cancellation for flight {}", id, e);
            }
        }
        
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
}