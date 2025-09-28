package com.vnair.air.service;

import com.vnair.air.enums.BookingStatus;
import com.vnair.air.enums.TicketStatus;
import com.vnair.air.model.BookingModel;
import com.vnair.air.model.BookingTicketModel;
import com.vnair.air.model.TicketModel;
import com.vnair.air.repository.BookingRepository;
import com.vnair.air.repository.BookingTicketRepository;
import com.vnair.air.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketBookingQueue {
    
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final BookingTicketRepository bookingTicketRepository;
    
    @Async
    @Transactional
    public void processTickets(Long bookingId, List<Long> ticketIds) {
        try {
            // Lấy tickets và check available
            List<TicketModel> tickets = ticketRepository.findAllById(ticketIds);
            
            // Check availability
            boolean allAvailable = tickets.stream()
                    .allMatch(ticket -> ticket.getStatus() == TicketStatus.AVAILABLE);
                    
            if (!allAvailable) {
                // Mark booking as CANCELLED
                updateBookingStatus(bookingId, BookingStatus.CANCELED);
                return;
            }
            
            // Update tickets to BOOKED
            tickets.forEach(ticket -> {
                ticket.setStatus(TicketStatus.BOOKED);
                ticketRepository.save(ticket);
            });
            
            // Create booking-ticket associations
            BookingModel booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null) {
                tickets.forEach(ticket -> {
                    BookingTicketModel bookingTicket = new BookingTicketModel();
                    bookingTicket.setBooking(booking);
                    bookingTicket.setTicket(ticket);
                    bookingTicketRepository.save(bookingTicket);
                });
                
                // Calculate total và update booking
                BigDecimal total = calculateTotal(tickets);
                booking.setTotalAmount(total);
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
            }
            
        } catch (Exception e) {
            log.error("Failed to process tickets for booking {}", bookingId, e);
            updateBookingStatus(bookingId, BookingStatus.CANCELED);
        }
    }
    
    private void updateBookingStatus(Long bookingId, BookingStatus status) {
        BookingModel booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setStatus(status);
            bookingRepository.save(booking);
        }
    }
    
    private BigDecimal calculateTotal(List<TicketModel> tickets) {
        return tickets.stream()
                .map(ticket -> {
                    BigDecimal basePrice = ticket.getFlight().getBasePrice();
                    return basePrice.multiply(ticket.getTicketType().getPriceMultiplier());
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}