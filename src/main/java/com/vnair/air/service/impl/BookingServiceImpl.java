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
import com.vnair.air.service.TicketBookingQueue;
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
    private final TicketBookingQueue ticketBookingQueue;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        // Validate input and fetch entities
        UserEntity user = validateAndFetchUser(request.getUserId());
        List<TicketModel> tickets = validateAndFetchTickets(request.getTicketIds());

        // Calculate total amount
        BigDecimal totalAmount = calculateBookingTotalFromTickets(tickets);

        // Create and save booking entity
        BookingModel booking = createAndSaveBooking(user, request, totalAmount);
        booking.setStatus(BookingStatus.PENDING);
        booking = bookingRepository.save(booking);

        // Gửi vào queue để xử lý ticket updates
        ticketBookingQueue.processTickets(booking.getId(), request.getTicketIds());

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
        BookingModel booking = validateAndFetchBooking(id);
        return convertToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        Page<BookingModel> bookingPage = bookingRepository.findAll(pageable);
        return convertPageToResponses(bookingPage, pageable);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        BookingModel booking = validateAndFetchBooking(id);
        releaseAssociatedTickets(booking);
        bookingRepository.delete(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByCustomerEmail(String customerEmail, Pageable pageable) {
        Page<BookingModel> bookingPage = bookingRepository.findByUserEmail(customerEmail, pageable);
        return convertPageToResponses(bookingPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUsername(String username) {
        UserEntity user = validateAndFetchUserByUsername(username);
        List<BookingModel> userBookings = bookingRepository.findByUserId(user.getId());
        return convertBookingsToResponses(userBookings);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByStatus(BookingStatus status, Pageable pageable) {
        Page<BookingModel> bookingPage = bookingRepository.findByStatus(status, pageable);
        return convertPageToResponses(bookingPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Page<BookingModel> bookingPage = bookingRepository.findByBookingDateBetween(start, end, pageable);
        return convertPageToResponses(bookingPage, pageable);
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long id, BookingStatus status) {
        BookingModel booking = validateAndFetchBooking(id);
        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        return convertToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        BookingModel booking = validateAndFetchBooking(id);
        releaseAssociatedTickets(booking);
        return convertToResponse(booking);
    }

    @Override
    public String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateBookingTotal(Long bookingId) {
        BookingModel booking = validateAndFetchBooking(bookingId);
        return booking.getTotalAmount();
    }

    /**
     * Calculate total amount from list of tickets
     */
    private BigDecimal calculateBookingTotalFromTickets(List<TicketModel> tickets) {
        return tickets.stream()
                .map(ticket -> {
                    // Use base price from flight or a default amount
                    BigDecimal basePrice = ticket.getFlight().getBasePrice(); // Lấy giá trị base price từ flight
                    return basePrice.multiply(ticket.getTicketType().getPriceMultiplier());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validate and fetch user by ID
     * Single Responsibility: Only handles user validation and fetching
     */
    private UserEntity validateAndFetchUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User ", "id", userId));
    }

    /**
     * Validate and fetch tickets by IDs
     * Single Responsibility: Only handles ticket validation and fetching
     */
    private List<TicketModel> validateAndFetchTickets(List<Long> ticketIds) {
        List<TicketModel> tickets = ticketRepository.findAllById(ticketIds);

        validateTicketsExist(tickets, ticketIds);
        validateTicketsAvailable(tickets);

        return tickets;
    }

    /**
     * Validate that all requested tickets exist
     */
    private void validateTicketsExist(List<TicketModel> tickets, List<Long> requestedIds) {
        if (tickets.size() != requestedIds.size()) {
            throw new TicketNotAvailableException("Some tickets not found");
        }
    }

    /**
     * Validate that all tickets are available for booking
     */
    private void validateTicketsAvailable(List<TicketModel> tickets) {
        List<TicketModel> unavailableTickets = tickets.stream()
                .filter(ticket -> ticket.getStatus() != TicketStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (!unavailableTickets.isEmpty()) {
            String unavailableIds = unavailableTickets.stream()
                    .map(t -> t.getId().toString())
                    .collect(Collectors.joining(", "));
            throw new TicketNotAvailableException("Tickets are not available: " + unavailableIds);
        }
    }

    /**
     * Create and save booking entity
     * Single Responsibility: Only handles booking entity creation
     */
    private BookingModel createAndSaveBooking(UserEntity user, BookingCreateRequest request, BigDecimal totalAmount) {
        BookingModel booking = new BookingModel();
        booking.setUser(user);
        booking.setBookingDate(request.getBookingDate() != null ? request.getBookingDate() : new Date());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(request.getStatus() != null ? request.getStatus() : BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    /**
     * Create associations between booking and tickets
     * Single Responsibility: Only handles booking-ticket relationship creation
     */
    private void createBookingTicketAssociations(BookingModel booking, List<TicketModel> tickets) {
        tickets.forEach(ticket -> {
            updateTicketStatus(ticket);
            createBookingTicketRelation(booking, ticket);
        });
    }

    /**
     * Update ticket status to BOOKED
     */
    private void updateTicketStatus(TicketModel ticket) {
        ticket.setStatus(TicketStatus.BOOKED);
        ticketRepository.save(ticket);
    }

    /**
     * Create booking-ticket relationship entity
     */
    private void createBookingTicketRelation(BookingModel booking, TicketModel ticket) {
        BookingTicketModel bookingTicket = new BookingTicketModel();
        bookingTicket.setBooking(booking);
        bookingTicket.setTicket(ticket);
        bookingTicketRepository.save(bookingTicket);
    }

    /**
     * Validate and fetch booking by ID
     * Single Responsibility: Only handles booking validation and fetching
     */
    private BookingModel validateAndFetchBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));
    }

    /**
     * Validate and fetch user by username
     * Single Responsibility: Only handles user validation and fetching by username
     */
    private UserEntity validateAndFetchUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Release all tickets associated with a booking back to AVAILABLE status
     * Single Responsibility: Only handles ticket status release
     */
    private void releaseAssociatedTickets(BookingModel booking) {
        List<BookingTicketModel> bookingTickets = booking.getBookingTickets();
        bookingTickets.forEach(bookingTicket -> {
            TicketModel ticket = bookingTicket.getTicket();
            ticket.setStatus(TicketStatus.AVAILABLE);
            ticketRepository.save(ticket);
        });
    }

    /**
     * Convert list of BookingModel to list of BookingResponse
     * Single Responsibility: Only handles bulk conversion
     */
    private List<BookingResponse> convertBookingsToResponses(List<BookingModel> bookings) {
        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Page of BookingModel to Page of BookingResponse
     * Single Responsibility: Only handles page conversion
     */
    private Page<BookingResponse> convertPageToResponses(Page<BookingModel> bookingPage, Pageable pageable) {
        List<BookingResponse> responses = convertBookingsToResponses(bookingPage.getContent());
        return new PageImpl<>(responses, pageable, bookingPage.getTotalElements());
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