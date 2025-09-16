package com.vnair.air.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vnair.air.dto.request.create.BookingCreateRequest;
import com.vnair.air.dto.request.update.BookingUpdateRequest;
import com.vnair.air.dto.response.BookingResponse;
import com.vnair.air.dto.response.BookingPageResponse;
import com.vnair.air.enums.BookingStatus;
import com.vnair.air.service.BookingService;
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
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
@Tag(name = "Booking Controller", description = "Booking management endpoints - Module nghiệp vụ phức tạp")
@Slf4j(topic = "BOOKING-CONTROLLER")
@RequiredArgsConstructor
public class BookingController {

        private final BookingService bookingService;

        @Operation(summary = "Create new booking", description = "Book tickets for flight (Customer) - Check ticket availability, create PENDING booking, update tickets to BOOKED")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data or tickets not available"),
                        @ApiResponse(responseCode = "404", description = "Tickets or user not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @PostMapping
        @PreAuthorize("hasAuthority('BOOKING:CREATE:ALL') or hasAuthority('BOOKING:CREATE:OWN')")
        public ResponseData<BookingResponse> createBooking(
                        @Valid @RequestBody BookingCreateRequest request) {

                log.info("Creating booking with request: {}", request);

                BookingResponse data = bookingService.createBooking(request);

                return new ResponseData<>(HttpStatus.OK.value(), "Booking created successfully", data);
        }

        @Operation(summary = "Update booking", description = "Update an existing booking by ID (Manager/System only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Booking not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager/System required")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('BOOKING:UPDATE:ALL')")
        public ResponseData<BookingResponse> updateBooking(
                        @Parameter(description = "Booking ID") @PathVariable Long id,
                        @Valid @RequestBody BookingUpdateRequest request) {

                log.info("Updating booking with ID: {}, request: {}", id, request);

                BookingResponse data = bookingService.updateBooking(id, request);

                return new ResponseData<>(HttpStatus.OK.value(), "Booking updated successfully", data);
        }

        @Operation(summary = "Update booking status", description = "Update booking status (System/Manager only) - PENDING -> CONFIRMED -> CANCELED")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking status updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Booking not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - System/Manager required")
        })
        @PutMapping("/{id}/status")
        @PreAuthorize("hasAuthority('BOOKING:UPDATE:ALL')")
        public ResponseData<BookingResponse> updateBookingStatus(
                        @Parameter(description = "Booking ID") @PathVariable Long id,
                        @Parameter(description = "New booking status") @RequestParam BookingStatus status) {

                log.info("Updating booking status for ID: {} to status: {}", id, status);

                BookingResponse data = bookingService.updateBookingStatus(id, status);

                return new ResponseData<>(HttpStatus.OK.value(), "Booking status updated successfully", data);
        }

        @Operation(summary = "Cancel booking", description = "Cancel booking (Customer: own bookings only, Manager: all bookings) - Updates tickets back to AVAILABLE")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking canceled successfully"),
                        @ApiResponse(responseCode = "404", description = "Booking not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied or not authorized to cancel this booking")
        })
        @PutMapping("/{id}/cancel")
        @PreAuthorize("hasAuthority('BOOKING:CANCEL:ALL') or hasAuthority('BOOKING:CANCEL:OWN')")
        public ResponseData<BookingResponse> cancelBooking(
                        @Parameter(description = "Booking ID") @PathVariable Long id) {

                log.info("Canceling booking with ID: {}", id);

                BookingResponse data = bookingService.cancelBooking(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Booking canceled successfully", data);
        }

        @Operation(summary = "Delete booking", description = "Delete a booking by ID (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Booking not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('BOOKING:DELETE:ALL')")
        public ResponseData<Void> deleteBooking(
                        @Parameter(description = "Booking ID") @PathVariable Long id) {

                log.info("Deleting booking with ID: {}", id);

                bookingService.deleteBooking(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Booking deleted successfully", null);
        }

        @Operation(summary = "Get current user bookings", description = "Retrieve all bookings of the currently logged-in user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User bookings found"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied"),
                        @ApiResponse(responseCode = "401", description = "Authentication required")
        })
        @GetMapping("/me")
        @PreAuthorize("hasAuthority('BOOKING:READ:OWN')")
        public ResponseData<List<BookingResponse>> getCurrentUserBookings() {

                // Lấy Authentication từ SecurityContextHolder
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication == null || authentication.getName() == null) {
                        log.error("Authentication is null or username is null");
                        return new ResponseData<>(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null);
                }

                String username = authentication.getName();

                log.info("Getting bookings for current user: {}", username);

                List<BookingResponse> data = bookingService.getBookingsByUserId(username);

                return new ResponseData<>(HttpStatus.OK.value(), "User bookings retrieved successfully", data);
        }

        @Operation(summary = "Get all bookings", description = "Retrieve all bookings with pagination (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @GetMapping
        @PreAuthorize("hasAuthority('BOOKING:READ:ALL')")
        public ResponseData<BookingPageResponse> getAllBookings(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting all bookings with page: {}, size: {}", page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<BookingResponse> pageData = bookingService.getAllBookings(pageable);

                BookingPageResponse data = new BookingPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Bookings retrieved successfully", data);
        }

        @Operation(summary = "Get bookings by customer email", description = "Retrieve bookings for a specific customer by email (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @GetMapping("/customer/email")
        @PreAuthorize("hasAuthority('BOOKING:READ:ALL')")
        public ResponseData<BookingPageResponse> getBookingsByCustomerEmail(
                        @Parameter(description = "Customer email") @RequestParam String email,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting bookings for customer email: {} with page: {}, size: {}", email, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<BookingResponse> pageData = bookingService.getBookingsByCustomerEmail(email, pageable);

                BookingPageResponse data = new BookingPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Bookings retrieved successfully", data);
        }

        @Operation(summary = "Get bookings by customer phone", description = "Retrieve bookings for a specific customer by phone (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @GetMapping("/customer/phone")
        @PreAuthorize("hasAuthority('BOOKING:READ:ALL')")
        public ResponseData<BookingPageResponse> getBookingsByCustomerPhone(
                        @Parameter(description = "Customer phone") @RequestParam String phone,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting bookings for customer phone: {} with page: {}, size: {}", phone, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<BookingResponse> pageData = bookingService.getBookingsByCustomerPhone(phone, pageable);

                BookingPageResponse data = new BookingPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Bookings retrieved successfully", data);
        }

        @Operation(summary = "Search bookings by customer name", description = "Search bookings by customer name with pagination (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @GetMapping("/search/customer-name")
        @PreAuthorize("hasAuthority('BOOKING:READ:ALL')")
        public ResponseData<BookingPageResponse> searchBookingsByCustomerName(
                        @Parameter(description = "Customer name") @RequestParam String customerName,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Searching bookings by customer name: {} with page: {}, size: {}", customerName, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<BookingResponse> pageData = bookingService.searchBookingsByCustomerName(customerName, pageable);

                BookingPageResponse data = new BookingPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Bookings retrieved successfully", data);
        }

        @Operation(summary = "Get bookings by status", description = "Retrieve bookings by status with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully")
        })
        @GetMapping("/status/{status}")
        @PreAuthorize("hasAuthority('BOOKING:READ:ALL')")
        public ResponseData<BookingPageResponse> getBookingsByStatus(
                        @Parameter(description = "Booking status (PENDING, CONFIRMED, CANCELED)") @PathVariable BookingStatus status,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting bookings by status: {} with page: {}, size: {}", status, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<BookingResponse> pageData = bookingService.getBookingsByStatus(status, pageable);

                BookingPageResponse data = new BookingPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Bookings retrieved successfully", data);
        }

        @Operation(summary = "Get bookings by date range", description = "Retrieve bookings created between specified dates with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid date range")
        })
        @GetMapping("/date-range")
        @PreAuthorize("hasAuthority('BOOKING:READ:ALL')")
        public ResponseData<BookingPageResponse> getBookingsByDateRange(
                        @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam LocalDate startDate,
                        @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam LocalDate endDate,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting bookings between dates: {} and {} with page: {}, size: {}", startDate, endDate, page,
                                size);

                Pageable pageable = PageRequest.of(page, size);
                Page<BookingResponse> pageData = bookingService.getBookingsByDateRange(startDate, endDate, pageable);

                BookingPageResponse data = new BookingPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Bookings retrieved successfully", data);
        }

        @Operation(summary = "Generate booking reference", description = "Generate a unique booking reference number (System/Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking reference generated successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - System/Manager required")
        })
        @GetMapping("/generate-reference")
        @PreAuthorize("hasAuthority('BOOKING:GENERATE:ALL')")
        public ResponseData<String> generateBookingReference() {

                log.info("Generating booking reference");

                String data = bookingService.generateBookingReference();

                return new ResponseData<>(HttpStatus.OK.value(), "Booking reference generated successfully", data);
        }

        @Operation(summary = "Calculate booking total", description = "Calculate total amount for a booking")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Booking total calculated successfully"),
                        @ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @GetMapping("/{id}/total")
        @PreAuthorize("hasAuthority('BOOKING:READ:ALL') or hasAuthority('BOOKING:READ:OWN')")
        public ResponseData<BigDecimal> calculateBookingTotal(
                        @Parameter(description = "Booking ID") @PathVariable Long id) {

                log.info("Calculating total for booking ID: {}", id);

                BigDecimal data = bookingService.calculateBookingTotal(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Booking total calculated successfully", data);
        }

        @Operation(summary = "Get my bookings", description = "Get bookings for the current user (Customer: own bookings only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User bookings retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @GetMapping("/my-bookings")
        @PreAuthorize("hasAuthority('BOOKING:READ:OWN') or hasAuthority('BOOKING:READ:ALL')")
        public ResponseData<BookingPageResponse> getMyBookings(
                        @Parameter(description = "User ID") @RequestParam Long userId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting bookings for user ID: {} with page: {}, size: {}", userId, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<BookingResponse> pageData = bookingService.getBookingsByCustomerEmail("", pageable); // This would
                                                                                                          // need a
                                                                                                          // method by
                                                                                                          // userId

                BookingPageResponse data = new BookingPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "User bookings retrieved successfully", data);
        }
}