# User Management REST API

Ứng dụng REST API quản lý user được xây dựng với Spring Boot và PostgreSQL.

## Công nghệ sử dụng

- **Java 17**
- **Spring Boot 3.5.4**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL**
- **Maven**
- **BCrypt** (mã hóa mật khẩu)

## Cấu trúc dự án

```
src/main/java/com/vnair/usermanagement/
├── controller/          # REST Controllers
├── service/            # Business Logic Layer
├── repository/         # Data Access Layer
├── model/            # JPA Entities
├── dto/               # Data Transfer Objects
├── exception/         # Exception Handling
└── UserManagementApiApplication.java
```

## Cài đặt và chạy ứng dụng

### Yêu cầu hệ thống

- **Java 17** hoặc cao hơn
- **Maven 3.6+**
- **Docker** và **Docker Compose**

### Bước 1: Khởi động PostgreSQL Database với Docker Compose

```bash
# Khởi động PostgreSQL container
docker-compose up -d

# Kiểm tra trạng thái container
docker-compose ps
```

**Thông tin Database:**
- Host: `localhost`
- Port: `5431`
- Database: `vnair_user_db`
- Username: `postgres`
- Password: `123456`

### Bước 2: Chạy dự án bằng Maven Spring Boot

```bash
# Chạy ứng dụng Spring Boot
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: **http://localhost:8080**

### Các lệnh Docker Compose hữu ích

```bash
# Khởi động PostgreSQL service
docker-compose up -d

# Dừng services
docker-compose down

# Xem logs PostgreSQL
docker-compose logs -f postgres

# Kiểm tra trạng thái containers
docker-compose ps
```
