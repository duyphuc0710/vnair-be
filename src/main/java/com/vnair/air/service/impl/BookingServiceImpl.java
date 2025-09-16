package com.vnair.air.service.impl;

import com.vnair.air.dto.request.create.BookingCreateRequest;
import com.vnair.air.dto.request.update.BookingUpdateRequest;
import com.vnair.air.dto.response.BookingResponse;
import com.vnair.air.enums.BookingStatus;
import com.vnair.air.enums.TicketStatus;
import com.vnair.air.exception.BookingNotFoundException;
import com.vnair.air.exception.TicketNotAvailableException;
import com.vnair.common.exception.ResourceNotFoundException;
import com.vnair.air.model.BookingModel;
import com.vnair.air.model.BookingTicketModel;
import com.vnair.air.model.TicketModel;
import com.vnair.air.repository.BookingRepository;
import com.vnair.air.repository.BookingTicketRepository;
import com.vnair.air.repository.TicketRepository;
import com.vnair.air.service.BookingService;
import com.vnair.user.model.UserEntity;
import com.vnair.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingTicketRepository bookingTicketRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User ", "id", request.getUserId()));

        List<TicketModel> tickets = ticketRepository.findAllById(request.getTicketIds());

        if (tickets.size() != request.getTicketIds().size()) {
            throw new TicketNotAvailableException("Some tickets not found");
        }

        List<TicketModel> unavailableTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus() != TicketStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (!unavailableTickets.isEmpty()) {
            throw new TicketNotAvailableException(
                    "Tickets are not available: " +
                            unavailableTickets.stream()
                                    .map(t -> t.getId().toString())
                                    .collect(Collectors.joining(", ")));
        }

        BigDecimal totalAmount = calculateBookingTotalFromTickets(tickets);

        BookingModel booking = new BookingModel();
        booking.setUser(user);
        booking.setBookingDate(request.getBookingDate() != null ? request.getBookingDate() : new Date());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(request.getStatus() != null ? request.getStatus() : BookingStatus.PENDING);

        booking = bookingRepository.save(booking);

        for (TicketModel ticket : tickets) {
            ticket.setStatus(TicketStatus.BOOKED);
            ticketRepository.save(ticket);

            BookingTicketModel bookingTicket = new BookingTicketModel();
            bookingTicket.setBooking(booking);
            bookingTicket.setTicket(ticket);

            bookingTicketRepository.save(bookingTicket);
        }

        return convertToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Long id, BookingUpdateRequest request) {
        BookingModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        if (request.getBookingDate() != null) {
            booking.setBookingDate(request.getBookingDate());
        }

        // Update total amount if provided
        if (request.getTotalAmount() != null) {
            booking.setTotalAmount(request.getTotalAmount());
        }

        // Update status if provided
        if (request.getStatus() != null) {
            booking.setStatus(request.getStatus());
        }

        booking = bookingRepository.save(booking);
        return convertToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        BookingModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        return convertToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        Page<BookingModel> bookingPage = bookingRepository.findAll(pageable);
        List<BookingResponse> responses = bookingPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, bookingPage.getTotalElements());
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        BookingModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        // Release all tickets back to AVAILABLE status
        List<BookingTicketModel> bookingTickets = booking.getBookingTickets();
        for (BookingTicketModel bookingTicket : bookingTickets) {
            TicketModel ticket = bookingTicket.getTicket();
            ticket.setStatus(TicketStatus.AVAILABLE);
            ticketRepository.save(ticket);
        }

        // Delete booking (cascade will delete booking tickets)
        bookingRepository.delete(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByCustomerEmail(String customerEmail, Pageable pageable) {
        Page<BookingModel> bookingPage = bookingRepository.findByUserEmail(customerEmail, pageable);
        List<BookingResponse> responses = bookingPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, bookingPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByCustomerPhone(String customerPhone, Pageable pageable) {
        Page<BookingModel> bookingPage = bookingRepository.findByUserPhone(customerPhone, pageable);
        List<BookingResponse> responses = bookingPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, bookingPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUsername(String username) {
        // Tìm user theo username
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        // Lấy tất cả bookings của user này
        List<BookingModel> userBookings = bookingRepository.findByUserId(user.getId());
        
        // Convert sang BookingResponse
        return userBookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(String username) {
        // Tìm user theo username để lấy user ID
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Long userId = user.getId();
        
        // Lấy tất cả bookings của user theo ID
        List<BookingModel> userBookings = bookingRepository.findByUserId(userId);
        
        // Convert sang BookingResponse
        return userBookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookingsByCustomerName(String customerName, Pageable pageable) {
        Page<BookingModel> bookingPage = bookingRepository.findByUserFullNameContainingIgnoreCase(customerName,
                pageable);
        List<BookingResponse> responses = bookingPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, bookingPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByStatus(BookingStatus status, Pageable pageable) {
        // Since BookingModel doesn't have status field, return empty page for now
        return new PageImpl<>(List.of(), pageable, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Page<BookingModel> bookingPage = bookingRepository.findByBookingDateBetween(start, end, pageable);
        List<BookingResponse> responses = bookingPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, bookingPage.getTotalElements());
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long id, BookingStatus status) {
        BookingModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        return convertToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        BookingModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        // Release all tickets back to AVAILABLE status
        List<BookingTicketModel> bookingTickets = booking.getBookingTickets();
        for (BookingTicketModel bookingTicket : bookingTickets) {
            TicketModel ticket = bookingTicket.getTicket();
            ticket.setStatus(TicketStatus.AVAILABLE);
            ticketRepository.save(ticket);
        }

        return convertToResponse(booking);
    }

    @Override
    public String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateBookingTotal(Long bookingId) {
        BookingModel booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));

        return booking.getTotalAmount();
    }

    /**
     * Calculate total amount from list of tickets
     */
    private BigDecimal calculateBookingTotalFromTickets(List<TicketModel> tickets) {
        return tickets.stream()
                .map(ticket -> {
                    // Use base price from flight or a default amount
                    BigDecimal basePrice = new BigDecimal("100.00"); // Default base price
                    return basePrice.multiply(ticket.getTicketType().getPriceMultiplier());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Convert BookingModel to BookingResponse
     */
    private BookingResponse convertToResponse(BookingModel booking) {
        return BookingResponse.builder()
                .userEmail(booking.getUser().getEmail())
                .bookingDate(booking.getBookingDate())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}