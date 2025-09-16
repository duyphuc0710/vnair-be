Custom exceptions extends RuntimeException.

Dùng một @ControllerAdvice để chuyển exceptions thành HTTP response theo mapping ở trên.

Ghi log (error level) kèm traceId cho mỗi exception.

Với đặt vé:

Sử dụng @Transactional ở service; ném RuntimeException để rollback.

Dùng optimistic locking (@Version) trên Ticket hoặc SELECT ... FOR UPDATE (pessimistic lock) khi book để tránh double-book. Khi lock thất bại -> ném ConcurrentModificationException.

Đảm bảo idempotency cho endpoint thanh toán (Idempotency key) để tránh trừ tiền 2 lần; nếu duplicate -> trả IdempotencyException + current payment status.

Với external payment: wrap lỗi trong ExternalPaymentException, log chi tiết, trả lỗi tạm thời (502) cho client, lưu transaction để reconcile.

Không leak stacktrace nhạy cảm ra client — chỉ trả errorCode + user-friendly message.



1. User Management (users)
Đăng ký tài khoản (register)

Exceptions:

ValidationException (400) — dữ liệu đầu vào không hợp lệ (email, password, phone, age <18).

DuplicateResourceException (409) — email hoặc phone đã tồn tại.

DatabaseException (500) — lỗi lưu DB.

Đăng nhập (login)

Exceptions:

AuthenticationException (401) — email/password sai.

UserNotFoundException (404) — email không tồn tại.

AccountLockedException (403) — tài khoản bị khóa.

Update profile

Exceptions:

UserNotFoundException (404) — user không tồn tại (token stale).

UnauthorizedException (403) — user cố cập nhật profile của người khác.

ValidationException (400) — dữ liệu update invalid.

DatabaseException (500)

Admin tạo manager / xóa user

Exceptions:

ForbiddenException (403) — caller không có quyền Admin.

UserNotFoundException (404) — khi xóa user không tồn tại.

2. Airport Management (airports)
Tạo / Cập nhật sân bay

Exceptions:

ForbiddenException (403) — chỉ admin.

ValidationException (400) — code không hợp lệ (không 3 ký tự).

DuplicateResourceException (409) — code trùng.

DatabaseException (500)

Xóa sân bay

Exceptions:

ForbiddenException (403)

ResourceInUseException (409) — không thể xóa nếu có flight FK → báo rõ (ví dụ AIRPORT_IN_USE).

AirportNotFoundException (404)

Xem danh sách

Thường không ném, ngoại trừ DatabaseException nếu DB lỗi.

3. Airplane Management (airplanes)
Tạo / Cập nhật / Xóa

Exceptions:

ForbiddenException (403) — chỉ Admin.

ValidationException (400) — seatCapacity <= 0.

DuplicateResourceException (409) — nếu có identifier unique.

ResourceInUseException (409) — không xóa nếu đã gán flight.

DatabaseException (500)

4. TicketType Management (ticket_types)
CRUD loại vé

Exceptions:

ForbiddenException (403) — Admin only.

ValidationException (400) — multiplier < 1 hoặc > max.

DuplicateResourceException (409) — tên loại vé trùng.

DatabaseException (500)

5. Flight Management (flights)
Tạo flight

Exceptions:

ForbiddenException (403) — chỉ Manager/Admin.

ValidationException (400) — departure/arrival/ thời gian không hợp lệ.

ResourceNotFoundException (404) — airplaneId / airportId không tồn tại.

DuplicateResourceException (409) — nếu tồn tại flight trùng (tuỳ business rule).

TicketGenerationException (500) — lỗi khi tự sinh tickets.

Cập nhật trạng thái (DELAY/CANCEL)

Exceptions:

FlightNotFoundException (404)

InvalidOperationException (400) — ví dụ cố gắng hạ thấp trạng thái của flight đã cất cánh cách đây quá lâu.

FlightCancelProcessingException (500) — khi cập nhật dẫn đến refund xử lý mà lỗi xảy ra.

Tìm/tra cứu flight

Exceptions:

ValidationException (400) — param tìm kiếm sai (date format).

DatabaseException (500)

6. Ticket Management (tickets)
Sinh vé (generate)

Exceptions:

TicketGenerationException (500) — dữ liệu thiếu (seatCapacity null) hoặc lỗi insert bulk.

Book / lock seat (khi user chọn ghế)

Exceptions:

TicketNotFoundException (404) — ticketId không tồn tại.

TicketAlreadyBookedException (409) — ticket không còn AVAILABLE.

ConcurrentModificationException / TicketLockException (409) — khi lock thất bại do race condition.

ValidationException (400) — seatNumber format invalid.

Update status (PAID/CANCELED)

Exceptions:

InvalidStatusTransitionException (400) — transition không hợp lệ (ví dụ PAID → BOOKED).

DatabaseException (500)

7. Booking Management (bookings, booking_tickets)
Tạo booking (Create)

Steps recap (kèm exceptions):

Validate input (flightId, ticketIds, userId) → ValidationException (400).

Lấy user → UserNotFoundException (404).

Lấy ticket(s) → TicketNotFoundException (404) nếu ticket nào mất.

Kiểm tra ticket.status == AVAILABLE → nếu không → TicketAlreadyBookedException (409).

Để chống race: dùng optimistic lock (@Version) hoặc DB lock; nếu fail → ConcurrentModificationException (409).

Tạo Booking (status = PENDING) → bookingRepository.save(); nếu lỗi → DatabaseException.

Tạo mapping booking_tickets → DatabaseException/BookingCreationException.

Update ticket.status = BOOKED → DatabaseException/OptimisticLockingFailureException.

Hủy booking

Exceptions:

BookingNotFoundException (404)

UnauthorizedException (403) — khách hủy booking người khác (trừ manager).

InvalidOperationException (400) — không thể hủy (đã PAID & vượt hạn hủy).

RefundException (502/500) — nếu tự động refund và external service lỗi.

Xem booking

Exceptions:

BookingNotFoundException (404)

UnauthorizedException (403) — customer chỉ xem booking của mình.

8. Payment Management (payments)
Thực hiện thanh toán (Create payment)

Steps & Exceptions:

Validate input (bookingId, amount, method) → ValidationException (400).

Lấy booking → BookingNotFoundException (404).

Kiểm tra booking.status (PENDING) → InvalidOperationException (400) nếu không thể thanh toán.

Kiểm tra amount == booking.totalAmount → PaymentAmountMismatchException (400).

Kiểm tra idempotency (nếu client gửi idempotency key) → IdempotencyException (409) nếu duplicate attempt in-progress/completed.

Gọi cổng thanh toán (external) → có thể ném ExternalPaymentException (502) hoặc PaymentGatewayException (502/424).

Nếu success:

paymentRepository.save(status=SUCCESS)

update booking.status = CONFIRMED, ticket.status = PAID.

Nếu cập nhật DB gặp lỗi sau khi payment success → CRITICAL: cần xử lý compensation / retry; ném DatabaseException (500) và lưu event để reconcile.

Nếu fail: PaymentFailedException (402/400) → lưu payment status = FAILED, giữ booking = PENDING.

Refund

Exceptions:

RefundNotAllowedException (400) — policy không cho refund.

ExternalRefundException (502) — cổng trả lỗi.

AlreadyRefundedException (409)

Xem lịch sử thanh toán

Exceptions:

UnauthorizedException (403)

DatabaseException (500)

Cross-cutting / Technical exceptions (xuất hiện khắp service)

AuthenticationException (401) — token expired/invalid.

AuthorizationException / ForbiddenException (403) — role không đủ.

ValidationException (400) — input invalid.

ResourceNotFoundException (404) — chung cho users/flights/tickets/booking…

ConflictException / DuplicateResourceException (409) — trùng dữ liệu.

ConcurrentModificationException / OptimisticLockingFailureException (409) — concurrency.

ExternalServiceException (502) — cổng thanh toán, email service, v.v.

DatabaseException (500) — SQL/DB lỗi không mong muốn.

FileStorageException (500) — nếu lưu tài liệu.

RateLimitException (429) — nếu giới hạn tần suất.

Thiết kế các Exception (note tách riêng) — tên, ý nghĩa, fields, HTTP mapping
Cấu trúc chung cho custom exceptions

Tất cả extends RuntimeException.

Mỗi exception class có:

errorCode (String) — ví dụ "TICKET_ALREADY_BOOKED"

message (String) — người đọc/FE hiển thị (có thể i18n)

httpStatus (HttpStatus) — gợi ý mapping (ControllerAdvice sẽ dùng).

optional details (Map<String,Object>) — trường hợp cần truyền thêm (ticketId, bookingId).

Danh sách đề xuất (ít nhất những cần dùng)

ValidationException — 400 — errorCode = VALIDATION_ERROR

ResourceNotFoundException — 404 — errorCode = RESOURCE_NOT_FOUND (subclass: UserNotFoundException, FlightNotFoundException, TicketNotFoundException, BookingNotFoundException)

DuplicateResourceException — 409 — errorCode = DUPLICATE_RESOURCE (eg. email exists, airport code exists)

ConflictException — 409 — errorCode = CONFLICT (generic)

UnauthorizedException — 401 — errorCode = UNAUTHORIZED

ForbiddenException — 403 — errorCode = FORBIDDEN

TicketAlreadyBookedException — 409 — errorCode = TICKET_ALREADY_BOOKED

ConcurrentModificationException — 409 — errorCode = CONCURRENT_MODIFICATION

InvalidOperationException — 400 — errorCode = INVALID_OPERATION (bad status transitions)

PaymentFailedException — 402/500 — errorCode = PAYMENT_FAILED

ExternalPaymentException — 502 — errorCode = PAYMENT_GATEWAY_ERROR

PaymentAmountMismatchException — 400 — errorCode = PAYMENT_AMOUNT_MISMATCH

RefundException / ExternalRefundException — 502/500 — errorCode = REFUND_FAILED

DatabaseException — 500 — errorCode = INTERNAL_DB_ERROR

TicketGenerationException — 500 — errorCode = TICKET_GENERATION_FAILED

ResourceInUseException — 409 — errorCode = RESOURCE_IN_USE

Ví dụ schema JSON phản hồi lỗi (standard)

{
  "timestamp": "2025-09-13T21:00:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "TICKET_ALREADY_BOOKED",
  "message": "Ticket 123 is already booked",
  "details": {"ticketId": 123, "flightId": 456},
  "path": "/api/bookings",
  "traceId": "xxxx-yyy-zzz"
}


   @ExceptionHandler({ConstraintViolationException.class,
            MissingServletRequestParameterException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(BAD_REQUEST)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Handle exception when the data invalid. (@RequestBody, @RequestParam, @PathVariable)",
                                    summary = "Handle Bad Request",
                                    value = """
                                            {
                                                 "timestamp": "2024-04-07T11:38:56.368+00:00",
                                                 "status": 400,
                                                 "path": "/api/v1/...",
                                                 "error": "Invalid Payload",
                                                 "message": "{data} must be not blank"
                                             }
                                            """
                            ))})
    })