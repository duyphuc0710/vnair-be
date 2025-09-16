1. UserRequestDto

fullName – string (not blank, min 2, max 100)

email – string (valid email format, unique)

phone – string (10–15 digits, only numbers/+, required)

password – string (not blank, min 6, max 50)

cccdPassport – string (not blank, min 6, max 20)

dateOfBirth – date (must be in the past, age ≥ 18)

2. AirportRequestDto

code – string (length = 3, uppercase, not blank)

name – string (not blank, max 100)

city – string (not blank, max 50)

country – string (not blank, max 50)

3. AirplaneRequestDto

model – string (not blank, max 50)

seatCapacity – int (> 0, realistic range 50–600)

airline – string (not blank, max 50)

4. FlightRequestDto

airplaneId – long (must exist in airplanes)

departureAirportId – int (must exist in airports, not equal to arrivalAirportId)

arrivalAirportId – int (must exist in airports)

departureTime – datetime (must be in the future)

arrivalTime – datetime (must be after departureTime)

basePrice – decimal (> 0, precision 2)

status – enum {SCHEDULED, DELAYED, CANCELED} (default SCHEDULED)

5. TicketTypeRequestDto

name – string (not blank, values: Economy, Business, First)

priceMultiplier – decimal (≥ 1.0, max 3.0)

6. TicketRequestDto

flightId – long (must exist in flights)

ticketTypeId – int (must exist in ticket_types)

seatNumber – string (not blank, format like 12A)

status – enum {AVAILABLE, BOOKED, PAID, CANCELED} (default AVAILABLE)

7. BookingRequestDto

userId – long (must exist in users)

bookingDate – datetime (default = now, cannot be in the past)

totalAmount – decimal (≥ 0, precision 2)

status – enum {PENDING, CONFIRMED, CANCELED} (default PENDING)

8. BookingTicketRequestDto

bookingId – long (must exist in bookings)

ticketId – long (must exist in tickets, must be AVAILABLE)

9. PaymentRequestDto

bookingId – long (must exist in bookings)

amount – decimal (> 0, must equal booking.totalAmount)

method – enum {CREDIT_CARD, MOMO, BANKING, CASH} (required)

status – enum {SUCCESS, FAILED, PENDING} (default PENDING)

paidAt – datetime (nullable, required only if SUCCESS)