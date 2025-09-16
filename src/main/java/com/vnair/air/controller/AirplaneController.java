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

import com.vnair.air.dto.request.create.AirplaneCreateRequest;
import com.vnair.air.dto.request.update.AirplaneUpdateRequest;
import com.vnair.air.dto.response.AirplaneResponse;
import com.vnair.air.dto.response.AirplanePageResponse;
import com.vnair.air.service.AirplaneService;
import com.vnair.common.model.ResponseData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airplane")
@Tag(name = "Airplane Controller")
@Slf4j(topic = "AIRPLANE-CONTROLLER")
@RequiredArgsConstructor
public class AirplaneController {

        private final AirplaneService airplaneService;

        @Operation(summary = "Create new airplane", description = "Create a new airplane with the provided details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airplane created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @PostMapping
        @PreAuthorize("hasAuthority('AIRPLANE:CREATE:ALL')")
        public ResponseData<AirplaneResponse> createAirplane(
                        @Valid @RequestBody AirplaneCreateRequest request) {

                log.info("Creating airplane with request: {}", request);

                AirplaneResponse data = airplaneService.createAirplane(request);

                return new ResponseData<>(HttpStatus.OK.value(), "Airplane created successfully", data);
        }

        @Operation(summary = "Update airplane", description = "Update an existing airplane by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airplane updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Airplane not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('AIRPLANE:UPDATE:ALL')")
        public ResponseData<AirplaneResponse> updateAirplane(
                        @Parameter(description = "Airplane ID") @PathVariable Integer id,
                        @Valid @RequestBody AirplaneUpdateRequest request) {

                log.info("Updating airplane with ID: {}, request: {}", id, request);

                AirplaneResponse data = airplaneService.updateAirplane(id, request);

                return new ResponseData<>(HttpStatus.OK.value(), "Airplane updated successfully", data);
        }

        @Operation(summary = "Get airplane by ID", description = "Retrieve airplane details by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airplane found"),
                        @ApiResponse(responseCode = "404", description = "Airplane not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('AIRPLANE:READ:ALL') or hasAuthority('AIRPLANE:READ:SELF')")
        public ResponseData<AirplaneResponse> getAirplaneById(
                        @Parameter(description = "Airplane ID") @PathVariable Integer id) {

                log.info("Getting airplane by ID: {}", id);

                AirplaneResponse data = airplaneService.getAirplaneById(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Airplane retrieved successfully", data);
        }

        @Operation(summary = "Get all airplanes with pagination", description = "Retrieve all airplanes with pagination support")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airplanes retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @GetMapping
        @PreAuthorize("hasAuthority('AIRPLANE:READ:ALL')")
        public ResponseData<AirplanePageResponse> getAllAirplanes(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting all airplanes with page: {}, size: {}", page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<AirplaneResponse> pageData = airplaneService.getAllAirplanes(pageable);

                AirplanePageResponse data = new AirplanePageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Airplanes retrieved successfully", data);
        }

        @Operation(summary = "Get all airplanes without pagination", description = "Retrieve all airplanes as a list")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airplanes retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @GetMapping("/all")
        @PreAuthorize("hasAuthority('AIRPLANE:READ:ALL')")
        public ResponseData<List<AirplaneResponse>> getAllAirplanesList() {

                log.info("Getting all airplanes as list");

                List<AirplaneResponse> data = airplaneService.getAllAirplanes();

                return new ResponseData<>(HttpStatus.OK.value(), "Airplanes retrieved successfully", data);
        }

        @Operation(summary = "Delete airplane", description = "Delete an airplane by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airplane deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Airplane not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('AIRPLANE:DELETE:ALL')")
        public ResponseData<Object> deleteAirplane(
                        @Parameter(description = "Airplane ID") @PathVariable Integer id) {

                log.info("Deleting airplane with ID: {}", id);

                airplaneService.deleteAirplane(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Airplane deleted successfully");
        }

        @Operation(summary = "Find airplanes by model", description = "Search airplanes by model with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Airplanes found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @GetMapping("/search/model")
        @PreAuthorize("hasAuthority('AIRPLANE:READ:ALL')")
        public ResponseData<AirplanePageResponse> findByModel(
                        @Parameter(description = "Airplane model") @RequestParam String model,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Finding airplanes by model: {}, page: {}, size: {}", model, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<AirplaneResponse> pageData = airplaneService.findByModel(model, pageable);

                AirplanePageResponse data = new AirplanePageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Airplanes found by model", data);
        }

}