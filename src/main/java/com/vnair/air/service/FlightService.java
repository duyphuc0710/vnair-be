package com.vnair.air.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vnair.air.dto.request.create.FlightCreateRequest;
import com.vnair.air.dto.request.update.FlightUpdateRequest;
import com.vnair.air.dto.response.FlightResponse;
import com.vnair.air.enums.FlightStatus;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface FlightService {

    /**
     * Create new flight
     */
    FlightResponse createFlight(FlightCreateRequest request);

    /**
     * Update existing flight
     */
    FlightResponse updateFlight(Long id, FlightUpdateRequest request);

    /**
     * Get flight by ID
     */
    FlightResponse getFlightById(Long id);

    /**
     * Get all flights with pagination
     */
    Page<FlightResponse> getAllFlights(Pageable pageable);

    /**
     * Delete flight by ID
     */
    void deleteFlight(Long id);

    /**
     * Search flights by route and date
     */
    List<FlightResponse> searchFlights(Integer departureAirportId, Integer arrivalAirportId,
            LocalDate departureDate);

    /**
     * Get flights by airplane
     */
    Page<FlightResponse> getFlightsByAirplane(Integer airplaneId, Pageable pageable);

    /**
     * Get flights by departure airport
     */
    Page<FlightResponse> getFlightsByDepartureAirport(Integer departureAirportId, Pageable pageable);

    /**
     * Get flights by arrival airport
     */
    Page<FlightResponse> getFlightsByArrivalAirport(Integer arrivalAirportId, Pageable pageable);

    /**
     * Get flights by status
     */
    Page<FlightResponse> getFlightsByStatus(FlightStatus status, Pageable pageable);

    /**
     * Get flights departing between dates
     */
    Page<FlightResponse> getFlightsByDepartureTimeBetween(Date startDate, Date endDate, Pageable pageable);

    /**
     * Update flight status
     */
    FlightResponse updateFlightStatus(Long id, FlightStatus status);

    /**
     * Cancel flight
     */
    FlightResponse cancelFlight(Long id);

}