Module cơ sở dữ liệu (đơn bảng – chỉ CRUD JPA)

2. Airport Management (airports)

Thêm sân bay (Admin) → save()

Sửa sân bay (Admin) → save()

Xóa sân bay (Admin) → deleteById()

Xem danh sách sân bay (All) → findAll()

3. Airplane Management (airplanes)

Thêm máy bay (Admin) → save()

Sửa máy bay (Admin) → save()

Xóa máy bay (Admin) → deleteById()

Xem danh sách máy bay (Manager, Admin) → findAll()

4. TicketType Management (ticket_types)

Thêm loại vé (Admin) → save()

Sửa loại vé (Admin) → save()

Xóa loại vé (Admin) → deleteById()

Xem danh sách loại vé (All) → findAll()

🟡 Module trung gian (có quan hệ 1-n, chủ yếu CRUD + vài query đơn giản)
5. Flight Management (flights)

Tạo chuyến bay (Manager)

Input: airplaneId, departureAirportId, arrivalAirportId, departureTime, arrivalTime, basePrice.

Validate (giờ, sân bay…).

flightRepository.save(flight).

Sinh vé tự động cho flight (ticketService).

Cập nhật trạng thái chuyến bay (Manager)

Input: flightId, status.

flightRepository.save(flight).

Nếu status = CANCELED → xử lý vé & booking liên quan.

Xem danh sách chuyến bay (Customer)

Tìm theo departureAirport, arrivalAirport, departureDate.

Viết query:

@Query("SELECT f FROM Flight f WHERE f.departureAirport.id = :dep AND f.arrivalAirport.id = :arr AND DATE(f.departureTime) = :date")
List<Flight> searchFlights(...);

6. Ticket Management (tickets)

Sinh vé (Manager/System)

Sau khi tạo flight → dựa trên airplane seatCapacity + ticketTypes.

ticketRepository.saveAll(tickets).

Cập nhật trạng thái vé (System/Manager)

BOOKED khi booking → save().

PAID khi payment success → save().

AVAILABLE khi booking bị hủy → save().

Xem vé của chuyến bay (Customer/Manager)

Customer: chỉ vé AVAILABLE.

Manager: tất cả vé của flight.

Query:

List<Ticket> findByFlightIdAndStatus(Long flightId, TicketStatus status);

🔴 Module nghiệp vụ phức tạp (đa bảng, dùng @Query hoặc service logic)
7. Booking Management (bookings, booking_tickets)

Đặt vé (Customer)

Input: flightId, ticketIds[].

Kiểm tra ticket.status = AVAILABLE.

Tạo booking với status = PENDING.

Lưu booking → bookingRepository.save(booking).

Tạo booking_tickets → saveAll().

Update ticket.status = BOOKED.

Hủy booking (Customer/Manager)

Customer: chỉ hủy booking của mình.

Manager: hủy booking của customer.

Update booking.status = CANCELED.

Update vé → AVAILABLE.

Xem booking

Customer: chỉ booking của mình → findByUserId(userId).

Manager: tất cả booking → findAll().

8. Payment Management (payments)

Thanh toán booking (Customer)

Input: bookingId, method.

Tính tổng tiền = sum(ticket.price).

Insert payment record → paymentRepository.save(payment).

Nếu success → update booking.status = CONFIRMED, ticket.status = PAID.

Nếu fail → payment.status = FAILED, booking giữ PENDING.

Xem lịch sử thanh toán

Customer: query by userId join booking → @Query.

Manager: query tất cả → findAll().