package com.vnair.air.service.impl;

import com.vnair.air.dto.request.create.PaymentCreateRequest;
import com.vnair.air.dto.request.update.PaymentUpdateRequest;
import com.vnair.air.dto.response.PaymentResponse;
import com.vnair.air.enums.BookingStatus;
import com.vnair.air.enums.PaymentMethod;
import com.vnair.air.enums.PaymentStatus;
import com.vnair.air.enums.TicketStatus;
import com.vnair.air.exception.BookingNotFoundException;
import com.vnair.air.exception.PaymentNotFoundException;
import com.vnair.air.model.BookingModel;
import com.vnair.air.model.BookingTicketModel;
import com.vnair.air.model.PaymentModel;
import com.vnair.air.model.TicketModel;
import com.vnair.air.repository.BookingRepository;
import com.vnair.air.repository.PaymentRepository;
import com.vnair.air.repository.TicketRepository;
import com.vnair.air.service.PaymentService;
import com.vnair.user.repository.UserRepository;
import com.vnair.user.model.UserEntity;
import com.vnair.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        BookingModel booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(
                        () -> new BookingNotFoundException("Booking not found with id: " + request.getBookingId()));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new BookingNotFoundException("Booking CONFIRMED");
        }
        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new BookingNotFoundException("Booking CANCELED");
        }

        // Create payment
        PaymentModel payment = new PaymentModel();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setMethod(PaymentMethod.valueOf(request.getMethod()));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(new Date());

        payment = paymentRepository.save(payment);
        payment.setTransactionId(generateTransactionId());

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        payment = paymentRepository.save(payment);
        return convertToResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse updatePayment(Long id, PaymentUpdateRequest request) {
        PaymentModel payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        if (request.getAmount() != null) {
            payment.setAmount(request.getAmount());
        }

        if (request.getMethod() != null) {
            payment.setMethod(request.getMethod());
        }

        payment = paymentRepository.save(payment);
        return convertToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        PaymentModel payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        return convertToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<PaymentModel> paymentPage = paymentRepository.findAll(pageable);
        List<PaymentResponse> responses = paymentPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, paymentPage.getTotalElements());
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        PaymentModel payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            BookingModel booking = payment.getBooking();
            List<BookingTicketModel> bookingTickets = booking.getBookingTickets();
            for (BookingTicketModel bookingTicket : bookingTickets) {
                TicketModel ticket = bookingTicket.getTicket();
                ticket.setStatus(TicketStatus.BOOKED);
                ticketRepository.save(ticket);
            }
        }

        paymentRepository.delete(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByBooking(Long bookingId) {
        List<PaymentModel> payments = paymentRepository.findByBookingId(bookingId);
        return payments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        List<BookingModel> userBookings = bookingRepository.findByUserId(user.getId());
        
        List<Long> bookingIds = userBookings.stream()
                .map(BookingModel::getId)
                .collect(Collectors.toList());
        
        if (bookingIds.isEmpty()) {
            return List.of();
        }
        
        List<PaymentModel> payments = paymentRepository.findByBookingIdIn(bookingIds);
        
        return payments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByUserId(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Long userId = user.getId();
        
        List<BookingModel> userBookings = bookingRepository.findByUserId(userId);
        
        List<Long> bookingIds = userBookings.stream()
                .map(BookingModel::getId)
                .collect(Collectors.toList());
        
        if (bookingIds.isEmpty()) {
            return List.of();
        }
        
        List<PaymentModel> payments = paymentRepository.findByBookingIdIn(bookingIds);
        
        return payments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        Page<PaymentModel> paymentPage = paymentRepository.findByStatus(status, pageable);
        List<PaymentResponse> responses = paymentPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, paymentPage.getTotalElements());
    }

    @Override
    public Page<PaymentResponse> getPaymentsByMethod(PaymentMethod method, Pageable pageable) {
        Page<PaymentModel> payments = paymentRepository.findByMethod(method, pageable);
        return payments.map(this::convertToResponse);
    }

    @Override
    public Page<PaymentResponse> getPaymentsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount,
            Pageable pageable) {
        Page<PaymentModel> payments = paymentRepository.findByAmountBetween(minAmount, maxAmount, pageable);
        return payments.map(this::convertToResponse);
    }

    @Override
    public List<PaymentResponse> findPaymentsByTransactionId(String transactionId) {
        List<PaymentModel> payments = paymentRepository.findByTransactionId(transactionId);
        return payments.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public PaymentResponse updatePaymentStatus(Long id, PaymentStatus status) {
        PaymentModel payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        payment.setStatus(status);
        payment = paymentRepository.save(payment);
        return convertToResponse(payment);
    }

    @Override
    public PaymentResponse cancelPayment(Long paymentId) {
        return updatePaymentStatus(paymentId, PaymentStatus.FAILED);
    }

    @Override
    public String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + ((int) (Math.random() * 1000));
    }

    /**
     * Convert PaymentModel to PaymentResponse
     */
    private PaymentResponse convertToResponse(PaymentModel payment) {
        return PaymentResponse.builder()
                .bookingCode("BK-" + payment.getBooking().getId())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

}