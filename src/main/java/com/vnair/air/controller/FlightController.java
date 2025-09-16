package com.vnair.air.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vnair.air.dto.request.create.FlightCreateRequest;
import com.vnair.air.dto.request.update.FlightUpdateRequest;
import com.vnair.air.dto.response.FlightResponse;
import com.vnair.air.dto.response.FlightPageResponse;
import com.vnair.air.enums.FlightStatus;
import com.vnair.air.service.FlightService;
import com.vnair.common.model.ResponseData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/flight")
@Tag(name = "Flight Controller", description = "Flight management endpoints - Module trung gian")
@Slf4j(topic = "FLIGHT-CONTROLLER")
@RequiredArgsConstructor
public class FlightController {

        private final FlightService flightService;

        @Operation(summary = "Create new flight", description = "Create a new flight with airplane, airports, time and price (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flight created successfully and tickets generated"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @PostMapping
        @PreAuthorize("hasAuthority('FLIGHT:CREATE:ALL')")
        public ResponseData<FlightResponse> createFlight(
                        @Valid @RequestBody FlightCreateRequest request) {

                log.info("Creating flight with request: {}", request);

                FlightResponse data = flightService.createFlight(request);

                return new ResponseData<>(HttpStatus.OK.value(), "Flight created successfully and tickets generated",
                                data);
        }

        @Operation(summary = "Update flight", description = "Update an existing flight by ID (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flight updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Flight not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('FLIGHT:UPDATE:ALL')")
        public ResponseData<FlightResponse> updateFlight(
                        @Parameter(description = "Flight ID") @PathVariable Long id,
                        @Valid @RequestBody FlightUpdateRequest request) {

                log.info("Updating flight with ID: {}, request: {}", id, request);

                FlightResponse data = flightService.updateFlight(id, request);

                return new ResponseData<>(HttpStatus.OK.value(), "Flight updated successfully", data);
        }

        @Operation(summary = "Update flight status", description = "Update flight status (Manager only). If CANCELED, related tickets and bookings will be processed")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flight status updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Flight not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @PutMapping("/{id}/status")
        @PreAuthorize("hasAuthority('FLIGHT:UPDATE:ALL')")
        public ResponseData<FlightResponse> updateFlightStatus(
                        @Parameter(description = "Flight ID") @PathVariable Long id,
                        @Parameter(description = "New flight status") @RequestParam FlightStatus status) {

                log.info("Updating flight status for ID: {} to status: {}", id, status);

                FlightResponse data = flightService.updateFlightStatus(id, status);

                return new ResponseData<>(HttpStatus.OK.value(), "Flight status updated successfully", data);
        }

        @Operation(summary = "Cancel flight", description = "Cancel a flight (Manager only). Related tickets and bookings will be processed")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flight canceled successfully"),
                        @ApiResponse(responseCode = "404", description = "Flight not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @PutMapping("/{id}/cancel")
        @PreAuthorize("hasAuthority('FLIGHT:CANCEL:ALL')")
        public ResponseData<FlightResponse> cancelFlight(
                        @Parameter(description = "Flight ID") @PathVariable Long id) {

                log.info("Canceling flight with ID: {}", id);

                FlightResponse data = flightService.cancelFlight(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Flight canceled successfully", data);
        }

        @Operation(summary = "Delete flight", description = "Delete a flight by ID (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flight deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Flight not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('FLIGHT:DELETE:ALL')")
        public ResponseData<Void> deleteFlight(
                        @Parameter(description = "Flight ID") @PathVariable Long id) {

                log.info("Deleting flight with ID: {}", id);

                flightService.deleteFlight(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Flight deleted successfully", null);
        }

        @Operation(summary = "Get flight by ID", description = "Retrieve flight details by ID (All users)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flight found"),
                        @ApiResponse(responseCode = "404", description = "Flight not found")
        })
        @GetMapping("/{id}")
        public ResponseData<FlightResponse> getFlightById(
                        @Parameter(description = "Flight ID") @PathVariable Long id) {

                log.info("Getting flight by ID: {}", id);

                FlightResponse data = flightService.getFlightById(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Flight retrieved successfully", data);
        }

        @Operation(summary = "Get all flights", description = "Retrieve all flights with pagination (All users)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flights retrieved successfully")
        })
        @GetMapping
        public ResponseData<FlightPageResponse> getAllFlights(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting all flights with page: {}, size: {}", page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<FlightResponse> pageData = flightService.getAllFlights(pageable);

                FlightPageResponse data = new FlightPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Flights retrieved successfully", data);
        }

        @Operation(summary = "Search flights", description = "Search flights by departure airport, arrival airport and departure date (Customer primary use case)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flights retrieved successfully")
        })
        @GetMapping("/search")
        public ResponseData<List<FlightResponse>> searchFlights(
                        @Parameter(description = "Departure airport ID") @RequestParam Integer departureAirportId,
                        @Parameter(description = "Arrival airport ID") @RequestParam Integer arrivalAirportId,
                        @Parameter(description = "Departure date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate) {

                log.info("Searching flights from airport {} to {} on date {}", departureAirportId, arrivalAirportId,
                                departureDate);

                List<FlightResponse> data = flightService.searchFlights(departureAirportId, arrivalAirportId,
                                departureDate);

                return new ResponseData<>(HttpStatus.OK.value(), "Flights retrieved successfully", data);
        }

        @Operation(summary = "Get flights by airplane", description = "Retrieve flights for a specific airplane with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flights retrieved successfully")
        })
        @GetMapping("/airplane/{airplaneId}")
        public ResponseData<FlightPageResponse> getFlightsByAirplane(
                        @Parameter(description = "Airplane ID") @PathVariable Integer airplaneId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting flights for airplane ID: {} with page: {}, size: {}", airplaneId, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<FlightResponse> pageData = flightService.getFlightsByAirplane(airplaneId, pageable);

                FlightPageResponse data = new FlightPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Flights retrieved successfully", data);
        }

        @Operation(summary = "Get flights by departure airport", description = "Retrieve flights departing from a specific airport with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flights retrieved successfully")
        })
        @GetMapping("/departure-airport/{departureAirportId}")
        public ResponseData<FlightPageResponse> getFlightsByDepartureAirport(
                        @Parameter(description = "Departure airport ID") @PathVariable Integer departureAirportId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting flights by departure airport ID: {} with page: {}, size: {}", departureAirportId,
                                page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<FlightResponse> pageData = flightService.getFlightsByDepartureAirport(departureAirportId,
                                pageable);

                FlightPageResponse data = new FlightPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Flights retrieved successfully", data);
        }

        @Operation(summary = "Get flights by arrival airport", description = "Retrieve flights arriving at a specific airport with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flights retrieved successfully")
        })
        @GetMapping("/arrival-airport/{arrivalAirportId}")
        public ResponseData<FlightPageResponse> getFlightsByArrivalAirport(
                        @Parameter(description = "Arrival airport ID") @PathVariable Integer arrivalAirportId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting flights by arrival airport ID: {} with page: {}, size: {}", arrivalAirportId, page,
                                size);

                Pageable pageable = PageRequest.of(page, size);
                Page<FlightResponse> pageData = flightService.getFlightsByArrivalAirport(arrivalAirportId, pageable);

                FlightPageResponse data = new FlightPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Flights retrieved successfully", data);
        }

        @Operation(summary = "Get flights by status", description = "Retrieve flights by status with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flights retrieved successfully")
        })
        @GetMapping("/status/{status}")
        public ResponseData<FlightPageResponse> getFlightsByStatus(
                        @Parameter(description = "Flight status (SCHEDULED, DELAYED, CANCELED)") @PathVariable FlightStatus status,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting flights by status: {} with page: {}, size: {}", status, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<FlightResponse> pageData = flightService.getFlightsByStatus(status, pageable);

                FlightPageResponse data = new FlightPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Flights retrieved successfully", data);
        }

        @Operation(summary = "Get flights by departure time range", description = "Retrieve flights departing between specific dates with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Flights retrieved successfully")
        })
        @GetMapping("/departure-time-range")
        public ResponseData<FlightPageResponse> getFlightsByDepartureTimeBetween(
                        @Parameter(description = "Start date (yyyy-MM-dd HH:mm:ss)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                        @Parameter(description = "End date (yyyy-MM-dd HH:mm:ss)") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting flights by departure time range: {} to {} with page: {}, size: {}", startDate,
                                endDate, page,
                                size);

                Pageable pageable = PageRequest.of(page, size);
                Page<FlightResponse> pageData = flightService.getFlightsByDepartureTimeBetween(startDate, endDate,
                                pageable);

                FlightPageResponse data = new FlightPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Flights retrieved successfully", data);
        }

}