package com.vnair.air.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.vnair.air.dto.request.create.AirportCreateRequest;
import com.vnair.air.dto.request.update.AirportUpdateRequest;
import com.vnair.air.dto.response.AirportResponse;
import com.vnair.air.dto.response.AirportPageResponse;
import com.vnair.air.service.AirportService;
import com.vnair.common.model.ResponseData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/airport")
@Tag(name = "Airport Controller", description = "Airport management endpoints")
@Slf4j(topic = "AIRPORT-CONTROLLER")
@RequiredArgsConstructor
public class AirportController {

        private final AirportService airportService;

        @Operation(summary = "Create new airport", description = "Create a new airport with the provided details (Admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airport created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or duplicate airport code"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin required")
        })
        @PostMapping
        @PreAuthorize("hasAuthority('AIRPORT:CREATE:ALL')")
        public ResponseData<AirportResponse> createAirport(
                        @Valid @RequestBody AirportCreateRequest request) {

                log.info("Creating airport with request: {}", request);

                AirportResponse data = airportService.createAirport(request);

                return new ResponseData<>(HttpStatus.OK.value(), "Airport created successfully", data);
        }

        @Operation(summary = "Update airport", description = "Update an existing airport by ID (Admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airport updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Airport not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or duplicate airport code"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin required")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('AIRPORT:UPDATE:ALL')")
        public ResponseData<AirportResponse> updateAirport(
                        @Parameter(description = "Airport ID") @PathVariable Integer id,
                        @Valid @RequestBody AirportUpdateRequest request) {

                log.info("Updating airport with ID: {}, request: {}", id, request);

                AirportResponse data = airportService.updateAirport(id, request);

                return new ResponseData<>(HttpStatus.OK.value(), "Airport updated successfully", data);
        }

        @Operation(summary = "Delete airport", description = "Delete an existing airport by ID (Admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airport deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Airport not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin required")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('AIRPORT:DELETE:ALL')")
        public ResponseData<Void> deleteAirport(
                        @Parameter(description = "Airport ID") @PathVariable Integer id) {

                log.info("Deleting airport with ID: {}", id);

                airportService.deleteAirport(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Airport deleted successfully", null);
        }

        @Operation(summary = "Get airport by ID", description = "Retrieve airport details by ID (All users)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airport found"),
                        @ApiResponse(responseCode = "404", description = "Airport not found")
        })
        @GetMapping("/{id}")
        public ResponseData<AirportResponse> getAirportById(
                        @Parameter(description = "Airport ID") @PathVariable Integer id) {

                log.info("Getting airport by ID: {}", id);

                AirportResponse data = airportService.getAirportById(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Airport retrieved successfully", data);
        }

        @Operation(summary = "Get all airports", description = "Retrieve all airports with pagination (All users)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airports retrieved successfully")
        })
        @GetMapping
        public ResponseData<AirportPageResponse> getAllAirports(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting all airports with page: {}, size: {}", page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<AirportResponse> pageData = airportService.getAllAirports(pageable);

                AirportPageResponse data = new AirportPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Airports retrieved successfully", data);
        }
}