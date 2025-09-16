1. UserResponseDto

fullName – string

email – string

phone – string

cccdPassport – string

dateOfBirth – date

createdAt – datetime

updatedAt – datetime

2. AirportResponseDto

code – string

name – string

city – string

country – string

createdAt – datetime

updatedAt – datetime

3. AirplaneResponseDto

model – string

seatCapacity – int

airline – string

createdAt – datetime

updatedAt – datetime

4. FlightResponseDto

airplane – string (model name hoặc code)

departureAirport – string (airport code)

arrivalAirport – string (airport code)

departureTime – datetime

arrivalTime – datetime

basePrice – decimal

status – enum {SCHEDULED, DELAYED, CANCELED}

createdAt – datetime

updatedAt – datetime

5. TicketTypeResponseDto

name – string (Economy, Business, First)

priceMultiplier – decimal

createdAt – datetime

updatedAt – datetime

6. TicketResponseDto

flight – string (mã chuyến bay hoặc route)

ticketType – string

seatNumber – string

status – enum {AVAILABLE, BOOKED, PAID, CANCELED}

createdAt – datetime

updatedAt – datetime

7. BookingResponseDto

userEmail – string

bookingDate – datetime

totalAmount – decimal

status – enum {PENDING, CONFIRMED, CANCELED}

createdAt – datetime

updatedAt – datetime

8. BookingTicketResponseDto

bookingCode – string (có thể sinh tự động, thay cho id)

ticketSeat – string (ví dụ: 12A)

createdAt – datetime

updatedAt – datetime

9. PaymentResponseDto

bookingCode – string

amount – decimal

method – enum {CREDIT_CARD, MOMO, BANKING, CASH}

status – enum {SUCCESS, FAILED, PENDING}

paidAt – datetime

createdAt – datetime

updatedAt – datetime