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

import com.vnair.air.dto.request.create.TicketTypeCreateRequest;
import com.vnair.air.dto.request.update.TicketTypeUpdateRequest;
import com.vnair.air.dto.response.TicketTypeResponse;
import com.vnair.air.dto.response.TicketTypePageResponse;
import com.vnair.air.service.TicketTypeService;
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
@RequestMapping("/api/v1/ticket-type")
@Tag(name = "Ticket Type Controller", description = "Ticket type management endpoints")
@Slf4j(topic = "TICKET-TYPE-CONTROLLER")
@RequiredArgsConstructor
public class TicketTypeController {

        private final TicketTypeService ticketTypeService;

        @Operation(summary = "Create new ticket type", description = "Create a new ticket type with the provided details (Admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ticket type created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin required")
        })
        @PostMapping
        @PreAuthorize("hasAuthority('TICKET_TYPE:CREATE:ALL')")
        public ResponseData<TicketTypeResponse> createTicketType(
                        @Valid @RequestBody TicketTypeCreateRequest request) {

                log.info("Creating ticket type with request: {}", request);

                TicketTypeResponse data = ticketTypeService.createTicketType(request);

                return new ResponseData<>(HttpStatus.OK.value(), "Ticket type created successfully", data);
        }

        @Operation(summary = "Update ticket type", description = "Update an existing ticket type by ID (Admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ticket type updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Ticket type not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin required")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('TICKET_TYPE:UPDATE:ALL')")
        public ResponseData<TicketTypeResponse> updateTicketType(
                        @Parameter(description = "Ticket type ID") @PathVariable Integer id,
                        @Valid @RequestBody TicketTypeUpdateRequest request) {

                log.info("Updating ticket type with ID: {}, request: {}", id, request);

                TicketTypeResponse data = ticketTypeService.updateTicketType(id, request);

                return new ResponseData<>(HttpStatus.OK.value(), "Ticket type updated successfully", data);
        }

        @Operation(summary = "Delete ticket type", description = "Delete an existing ticket type by ID (Admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ticket type deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Ticket type not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin required")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('TICKET_TYPE:DELETE:ALL')")
        public ResponseData<Void> deleteTicketType(
                        @Parameter(description = "Ticket type ID") @PathVariable Integer id) {

                log.info("Deleting ticket type with ID: {}", id);

                ticketTypeService.deleteTicketType(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Ticket type deleted successfully", null);
        }

        @Operation(summary = "Get ticket type by ID", description = "Retrieve ticket type details by ID (All users)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ticket type found"),
                        @ApiResponse(responseCode = "404", description = "Ticket type not found")
        })
        @GetMapping("/{id}")
        public ResponseData<TicketTypeResponse> getTicketTypeById(
                        @Parameter(description = "Ticket type ID") @PathVariable Integer id) {

                log.info("Getting ticket type by ID: {}", id);

                TicketTypeResponse data = ticketTypeService.getTicketTypeById(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Ticket type retrieved successfully", data);
        }

        @Operation(summary = "Get all ticket types", description = "Retrieve all ticket types with pagination (All users)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Ticket types retrieved successfully")
        })
        @GetMapping
        public ResponseData<TicketTypePageResponse> getAllTicketTypes(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting all ticket types with page: {}, size: {}", page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<TicketTypeResponse> pageData = ticketTypeService.getAllTicketTypes(pageable);

                TicketTypePageResponse data = new TicketTypePageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Ticket types retrieved successfully", data);
        }

        @Operation(summary = "Update available tickets count", description = "Update the available tickets count for a ticket type (Admin only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Available tickets count updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Ticket type not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Admin required")
        })
        @PutMapping("/{id}/available-tickets")
        @PreAuthorize("hasAuthority('TICKET_TYPE:UPDATE:ALL')")
        public ResponseData<TicketTypeResponse> updateAvailableTickets(
                        @Parameter(description = "Ticket type ID") @PathVariable Integer id,
                        @Parameter(description = "New available tickets count") @RequestParam Integer newCount) {

                log.info("Updating available tickets for ticket type ID: {} to count: {}", id, newCount);

                TicketTypeResponse data = ticketTypeService.updateAvailableTickets(id, newCount);

                return new ResponseData<>(HttpStatus.OK.value(), "Available tickets count updated successfully", data);
        }

        @Operation(summary = "Release tickets", description = "Release tickets (increase available count) - Internal use by booking system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Tickets released successfully"),
                        @ApiResponse(responseCode = "404", description = "Ticket type not found")
        })
        @PutMapping("/{id}/release")
        @PreAuthorize("hasAuthority('TICKET_TYPE:UPDATE:ALL') or hasAuthority('BOOKING:CANCEL:ALL')")
        public ResponseData<TicketTypeResponse> releaseTickets(
                        @Parameter(description = "Ticket type ID") @PathVariable Integer id,
                        @Parameter(description = "Quantity to release") @RequestParam Integer quantity) {

                log.info("Releasing {} tickets for ticket type ID: {}", quantity, id);

                TicketTypeResponse data = ticketTypeService.releaseTickets(id, quantity);

                return new ResponseData<>(HttpStatus.OK.value(), "Tickets released successfully", data);
        }

        @Operation(summary = "Get total available tickets by flight", description = "Get the total number of available tickets for a specific flight (All users)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Total available tickets retrieved successfully")
        })
        @GetMapping("/flight/{flightId}/total-available")
        public ResponseData<Integer> getTotalAvailableTicketsByFlight(
                        @Parameter(description = "Flight ID") @PathVariable Long flightId) {

                log.info("Getting total available tickets for flight ID: {}", flightId);

                Integer data = ticketTypeService.getTotalAvailableTicketsByFlight(flightId);

                return new ResponseData<>(HttpStatus.OK.value(), "Total available tickets retrieved successfully",
                                data);
        }
}