1. Bảng users (khách hàng)

    user_id (PK) – long

    full_name – string

    email – string

    phone – string

    password_hash – string

    cccd_passport – string

    date_of_birth – date


2. Bảng airports (sân bay)

    airport_id (PK) – int

    code – string (3 ký tự, ví dụ: HAN, SGN)

    name – string

    city – string

    country – string

3. Bảng airplanes (máy bay)

    airplane_id (PK) – int

    model – string

    seat_capacity – int

    airline – string

4. Bảng flights (chuyến bay)

    flight_id (PK) – long

    airplane_id (FK → airplanes.airplane_id) – int

    departure_airport_id (FK → airports.airport_id) – int

    arrival_airport_id (FK → airports.airport_id) – int

    departure_time – datetime

    arrival_time – datetime

    base_price – decimal

    status – enum {SCHEDULED, DELAYED, CANCELED}

5. Bảng ticket_types (loại vé)

    ticket_type_id (PK) – int

    name – string (Phổ thông, Thương gia, Hạng nhất)

    price_multiplier – decimal (ví dụ: 1.0, 1.5, 2.0)

6. Bảng tickets (vé)

    ticket_id (PK) – long

    flight_id (FK → flights.flight_id) – long

    ticket_type_id (FK → ticket_types.ticket_type_id) – int

    seat_number – string (ví dụ: 12A, 15C)

    status – enum {AVAILABLE, BOOKED, PAID, CANCELED}

7. Bảng bookings (đặt vé)

    booking_id (PK) – long

    user_id (FK → users.user_id) – long

    booking_date – datetime

    total_amount – decimal

    status – enum {PENDING, CONFIRMED, CANCELED}

8. Bảng booking_tickets (chi tiết vé trong 1 booking)

    id (PK) – long

    booking_id (FK → bookings.booking_id) – long

    ticket_id (FK → tickets.ticket_id) – long

9. Bảng payments (thanh toán)

    payment_id (PK) – long

    booking_id (FK → bookings.booking_id) – long

    amount – decimal

    method – enum {CREDIT_CARD, MOMO, BANKING, CASH}

    status – enum {SUCCESS, FAILED, PENDING}

    paid_at – datetime

👉 Với thiết kế này:

Người dùng có thể đặt nhiều vé trong 1 booking.

Mỗi vé gắn với 1 chuyến bay và 1 loại vé.

Thanh toán gắn với booking, không gắn trực tiếp từng vé.

