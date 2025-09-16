1. UserUpdateRequestDto

fullName – string (nullable, min 2, max 100)

email – string (nullable, valid email format, unique)

phone – string (nullable, 10–15 digits)

password – string (nullable, min 6, max 50)

cccdPassport – string (nullable, min 6, max 20)

dateOfBirth – date (nullable, must be in the past)

2. AirportUpdateRequestDto

code – string (nullable, length = 3, uppercase)

name – string (nullable, max 100)

city – string (nullable, max 50)

country – string (nullable, max 50)

3. AirplaneUpdateRequestDto

model – string (nullable, max 50)

seatCapacity – int (nullable, > 0)

airline – string (nullable, max 50)

4. FlightUpdateRequestDto

airplaneId – long (nullable, must exist in airplanes)

departureAirportId – int (nullable, must exist in airports)

arrivalAirportId – int (nullable, must exist in airports)

departureTime – datetime (nullable, must be in the future)

arrivalTime – datetime (nullable, must be after departureTime)

basePrice – decimal (nullable, > 0)

status – enum {SCHEDULED, DELAYED, CANCELED} (nullable)

5. TicketTypeUpdateRequestDto

name – string (nullable, values: Economy, Business, First)

priceMultiplier – decimal (nullable, ≥ 1.0, max 3.0)

6. TicketUpdateRequestDto

flightId – long (nullable, must exist in flights)

ticketTypeId – int (nullable, must exist in ticket_types)

seatNumber – string (nullable, format like 12A)

status – enum {AVAILABLE, BOOKED, PAID, CANCELED} (nullable)

7. BookingUpdateRequestDto

userId – long (nullable, must exist in users)

bookingDate – datetime (nullable, cannot be in the past)

totalAmount – decimal (nullable, ≥ 0)

status – enum {PENDING, CONFIRMED, CANCELED} (nullable)

8. BookingTicketUpdateRequestDto

bookingId – long (nullable, must exist in bookings)

ticketId – long (nullable, must exist in tickets)

9. PaymentUpdateRequestDto

bookingId – long (nullable, must exist in bookings)

amount – decimal (nullable, > 0)

method – enum {CREDIT_CARD, MOMO, BANKING, CASH} (nullable)

status – enum {SUCCESS, FAILED, PENDING} (nullable)

paidAt – datetime (nullable, required only if SUCCESS)