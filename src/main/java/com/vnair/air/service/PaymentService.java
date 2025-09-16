package com.vnair.air.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vnair.air.dto.request.create.PaymentCreateRequest;
import com.vnair.air.dto.request.update.PaymentUpdateRequest;
import com.vnair.air.dto.response.PaymentResponse;
import com.vnair.air.enums.PaymentMethod;
import com.vnair.air.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    
    /**
     * Create new payment
     */
    PaymentResponse createPayment(PaymentCreateRequest request);
    
    /**
     * Update existing payment
     */
    PaymentResponse updatePayment(Long id, PaymentUpdateRequest request);
    
    /**
     * Get payment by ID
     */
    PaymentResponse getPaymentById(Long id);
    
    /**
     * Get all payments with pagination
     */
    Page<PaymentResponse> getAllPayments(Pageable pageable);
    
    
    /**
     * Delete payment by ID
     */
    void deletePayment(Long id);
    
    /**
     * Get payments by booking
     */
    List<PaymentResponse> getPaymentsByBooking(Long bookingId);
    
    /**
     * Get payments by username (for current logged-in user)
     */
    List<PaymentResponse> getPaymentsByUsername(String username);
    
    /**
     * Get payments by user ID (for current logged-in user)
     */
    List<PaymentResponse> getPaymentsByUserId(String username);
    
    
    /**
     * Get payments by status
     */
    Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable);
    
    /**
     * Get payments by payment method
     */
    Page<PaymentResponse> getPaymentsByMethod(PaymentMethod method, Pageable pageable);
    
    /**
     * Find payments by transaction ID
     */
    List<PaymentResponse> findPaymentsByTransactionId(String transactionId);
    
    /**
     * Get payments by amount range
     */
    Page<PaymentResponse> getPaymentsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Update payment status
     */
    PaymentResponse updatePaymentStatus(Long id, PaymentStatus status);
    
    /**
     * Cancel payment
     */
    PaymentResponse cancelPayment(Long id);
    

    
    /**
     * Generate payment transaction ID
     */
    String generateTransactionId();
    
}