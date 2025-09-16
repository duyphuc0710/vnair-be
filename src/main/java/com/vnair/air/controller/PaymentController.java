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

import com.vnair.air.dto.request.create.PaymentCreateRequest;
import com.vnair.air.dto.request.update.PaymentUpdateRequest;
import com.vnair.air.dto.response.PaymentResponse;
import com.vnair.air.dto.response.PaymentPageResponse;
import com.vnair.air.enums.PaymentMethod;
import com.vnair.air.enums.PaymentStatus;
import com.vnair.air.service.PaymentService;
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
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/payment")
@Tag(name = "Payment Controller", description = "Payment management endpoints - Module nghiệp vụ phức tạp")
@Slf4j(topic = "PAYMENT-CONTROLLER")
@RequiredArgsConstructor
public class PaymentController {

        private final PaymentService paymentService;

        @Operation(summary = "Create payment for booking", description = "Process payment for booking (Customer) - Calculate total amount, create payment record, update booking & ticket status based on result")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid payment data or booking not eligible for payment"),
                        @ApiResponse(responseCode = "404", description = "Booking not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @PostMapping
        @PreAuthorize("hasAuthority('PAYMENT:CREATE:ALL') or hasAuthority('PAYMENT:CREATE:OWN')")
        public ResponseData<PaymentResponse> createPayment(
                        @Valid @RequestBody PaymentCreateRequest request) {

                log.info("Processing payment with request: {}", request);

                PaymentResponse data = paymentService.createPayment(request);

                return new ResponseData<>(HttpStatus.OK.value(), "Payment processed successfully", data);
        }

        @Operation(summary = "Update payment", description = "Update an existing payment by ID (Manager/System only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Payment not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager/System required")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('PAYMENT:UPDATE:ALL')")
        public ResponseData<PaymentResponse> updatePayment(
                        @Parameter(description = "Payment ID") @PathVariable Long id,
                        @Valid @RequestBody PaymentUpdateRequest request) {

                log.info("Updating payment with ID: {}, request: {}", id, request);

                PaymentResponse data = paymentService.updatePayment(id, request);

                return new ResponseData<>(HttpStatus.OK.value(), "Payment updated successfully", data);
        }

        @Operation(summary = "Update payment status", description = "Update payment status (System/Manager only) - SUCCESS/FAILED/PENDING")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Payment not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - System/Manager required")
        })
        @PutMapping("/{id}/status")
        @PreAuthorize("hasAuthority('PAYMENT:UPDATE:ALL')")
        public ResponseData<PaymentResponse> updatePaymentStatus(
                        @Parameter(description = "Payment ID") @PathVariable Long id,
                        @Parameter(description = "New payment status") @RequestParam PaymentStatus status) {

                log.info("Updating payment status for ID: {} to status: {}", id, status);

                PaymentResponse data = paymentService.updatePaymentStatus(id, status);

                return new ResponseData<>(HttpStatus.OK.value(), "Payment status updated successfully", data);
        }

        @Operation(summary = "Cancel payment", description = "Cancel a payment (System/Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment canceled successfully"),
                        @ApiResponse(responseCode = "404", description = "Payment not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - System/Manager required")
        })
        @PutMapping("/{id}/cancel")
        @PreAuthorize("hasAuthority('PAYMENT:CANCEL:ALL')")
        public ResponseData<PaymentResponse> cancelPayment(
                        @Parameter(description = "Payment ID") @PathVariable Long id) {

                log.info("Canceling payment with ID: {}", id);

                PaymentResponse data = paymentService.cancelPayment(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Payment canceled successfully", data);
        }

        @Operation(summary = "Delete payment", description = "Delete a payment by ID (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Payment not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('PAYMENT:DELETE:ALL')")
        public ResponseData<Void> deletePayment(
                        @Parameter(description = "Payment ID") @PathVariable Long id) {

                log.info("Deleting payment with ID: {}", id);

                paymentService.deletePayment(id);

                return new ResponseData<>(HttpStatus.OK.value(), "Payment deleted successfully", null);
        }

        @Operation(summary = "Get current user payments", description = "Retrieve all payments of the currently logged-in user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User payments found"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied"),
                        @ApiResponse(responseCode = "401", description = "Authentication required")
        })
        @GetMapping("/me")
        @PreAuthorize("hasAuthority('PAYMENT:READ:OWN')")
        public ResponseData<List<PaymentResponse>> getCurrentUserPayments() {

                // Lấy Authentication từ SecurityContextHolder
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                
                if (authentication == null || authentication.getName() == null) {
                        log.error("Authentication is null or username is null");
                        return new ResponseData<>(HttpStatus.UNAUTHORIZED.value(), "Authentication required", null);
                }

                String username = authentication.getName();
                
                log.info("Getting payments for current user: {}", username);

                List<PaymentResponse> data = paymentService.getPaymentsByUserId(username);

                return new ResponseData<>(HttpStatus.OK.value(), "User payments retrieved successfully", data);
        }

        @Operation(summary = "Get all payments", description = "Retrieve all payments with pagination (Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - Manager required")
        })
        @GetMapping
        @PreAuthorize("hasAuthority('PAYMENT:READ:ALL')")
        public ResponseData<PaymentPageResponse> getAllPayments(
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting all payments with page: {}, size: {}", page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<PaymentResponse> pageData = paymentService.getAllPayments(pageable);

                PaymentPageResponse data = new PaymentPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Payments retrieved successfully", data);
        }

        @Operation(summary = "Get payments by booking", description = "Retrieve all payments for a specific booking (Customer: own bookings only, Manager: all)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "Booking not found")
        })
        @GetMapping("/booking/{bookingId}")
        @PreAuthorize("hasAuthority('PAYMENT:READ:ALL') or hasAuthority('PAYMENT:READ:OWN')")
        public ResponseData<List<PaymentResponse>> getPaymentsByBooking(
                        @Parameter(description = "Booking ID") @PathVariable Long bookingId) {

                log.info("Getting payments for booking ID: {}", bookingId);

                List<PaymentResponse> data = paymentService.getPaymentsByBooking(bookingId);

                return new ResponseData<>(HttpStatus.OK.value(), "Payments retrieved successfully", data);
        }

        @Operation(summary = "Get payments by status", description = "Retrieve payments by status with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
        })
        @GetMapping("/status/{status}")
        @PreAuthorize("hasAuthority('PAYMENT:READ:ALL')")
        public ResponseData<PaymentPageResponse> getPaymentsByStatus(
                        @Parameter(description = "Payment status (SUCCESS, FAILED, PENDING)") @PathVariable PaymentStatus status,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting payments by status: {} with page: {}, size: {}", status, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<PaymentResponse> pageData = paymentService.getPaymentsByStatus(status, pageable);

                PaymentPageResponse data = new PaymentPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Payments retrieved successfully", data);
        }

        @Operation(summary = "Get payments by payment method", description = "Retrieve payments by payment method with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
        })
        @GetMapping("/method/{method}")
        @PreAuthorize("hasAuthority('PAYMENT:READ:ALL')")
        public ResponseData<PaymentPageResponse> getPaymentsByMethod(
                        @Parameter(description = "Payment method (CREDIT_CARD, MOMO, BANKING, CASH)") @PathVariable PaymentMethod method,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting payments by method: {} with page: {}, size: {}", method, page, size);

                Pageable pageable = PageRequest.of(page, size);
                Page<PaymentResponse> pageData = paymentService.getPaymentsByMethod(method, pageable);

                PaymentPageResponse data = new PaymentPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Payments retrieved successfully", data);
        }

        @Operation(summary = "Find payments by transaction ID", description = "Find payments by transaction ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully")
        })
        @GetMapping("/transaction")
        @PreAuthorize("hasAuthority('PAYMENT:READ:ALL')")
        public ResponseData<List<PaymentResponse>> findPaymentsByTransactionId(
                        @Parameter(description = "Transaction ID") @RequestParam String transactionId) {

                log.info("Finding payments by transaction ID: {}", transactionId);

                List<PaymentResponse> data = paymentService.findPaymentsByTransactionId(transactionId);

                return new ResponseData<>(HttpStatus.OK.value(), "Payments retrieved successfully", data);
        }

        @Operation(summary = "Get payments by amount range", description = "Retrieve payments within specified amount range with pagination")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid amount range")
        })
        @GetMapping("/amount-range")
        @PreAuthorize("hasAuthority('PAYMENT:READ:ALL')")
        public ResponseData<PaymentPageResponse> getPaymentsByAmountRange(
                        @Parameter(description = "Minimum amount") @RequestParam BigDecimal minAmount,
                        @Parameter(description = "Maximum amount") @RequestParam BigDecimal maxAmount,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

                log.info("Getting payments between amounts: {} and {} with page: {}, size: {}", minAmount, maxAmount,
                                page,
                                size);

                Pageable pageable = PageRequest.of(page, size);
                Page<PaymentResponse> pageData = paymentService.getPaymentsByAmountRange(minAmount, maxAmount,
                                pageable);

                PaymentPageResponse data = new PaymentPageResponse();
                data.setContent(pageData.getContent());
                data.setPageNumber(pageData.getNumber());
                data.setPageSize(pageData.getSize());
                data.setTotalPages(pageData.getTotalPages());
                data.setTotalElements(pageData.getTotalElements());

                return new ResponseData<>(HttpStatus.OK.value(), "Payments retrieved successfully", data);
        }

        @Operation(summary = "Generate transaction ID", description = "Generate a unique transaction ID for payment (System/Manager only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Transaction ID generated successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied - System/Manager required")
        })
        @GetMapping("/generate-transaction-id")
        @PreAuthorize("hasAuthority('PAYMENT:GENERATE:ALL')")
        public ResponseData<String> generateTransactionId() {

                log.info("Generating transaction ID");

                String data = paymentService.generateTransactionId();

                return new ResponseData<>(HttpStatus.OK.value(), "Transaction ID generated successfully", data);
        }

     
   

}