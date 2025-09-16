-- Database initialization script for VNair Backend
-- PostgreSQL Database Setup

-- Drop existing tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS tbl_user_has_role CASCADE;
DROP TABLE IF EXISTS tbl_role_has_permission CASCADE;
DROP TABLE IF EXISTS tbl_address CASCADE;
DROP TABLE IF EXISTS cabin CASCADE;
DROP TABLE IF EXISTS aircraft CASCADE;
DROP TABLE IF EXISTS airplane CASCADE;
DROP TABLE IF EXISTS tbl_permission CASCADE;
DROP TABLE IF EXISTS tbl_role CASCADE;
DROP TABLE IF EXISTS tbl_user CASCADE;

-- Create User table
CREATE TABLE tbl_user (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    cccd_passport VARCHAR(20),
    date_of_birth DATE,
    user_name VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'NONE' CHECK (status IN ('NONE', 'ACTIVE', 'INACTIVE')),
    type VARCHAR(20) DEFAULT 'CUSTOMER' CHECK (type IN ('ADMIN', 'CUSTOMER')),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Role table
CREATE TABLE tbl_role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Permission table
CREATE TABLE tbl_permission (
    id SERIAL PRIMARY KEY,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    scope VARCHAR(100) NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(resource, action, scope)
);

-- Create Address table
CREATE TABLE tbl_address (
    id BIGSERIAL PRIMARY KEY,
    street VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    address_type INTEGER,
    user_id BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- Create User-Role junction table
CREATE TABLE tbl_user_has_role (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id INTEGER NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES tbl_role(id) ON DELETE CASCADE,
    UNIQUE(user_id, role_id)
);

-- Create Role-Permission junction table
CREATE TABLE tbl_role_has_permission (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL,
    permission_id INTEGER NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES tbl_role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES tbl_permission(id) ON DELETE CASCADE,
    UNIQUE(role_id, permission_id)
);

-- Create Aircraft table
CREATE TABLE aircraft (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL
);

-- Create Cabin table
CREATE TABLE cabin (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    position VARCHAR(20) CHECK (position IN ('FRONT', 'MIDDLE', 'REAR')),
    seat_count INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    aircraft_id BIGINT NOT NULL,
    FOREIGN KEY (aircraft_id) REFERENCES aircraft(id) ON DELETE CASCADE
);

-- Create Airplane table (matching JPA entity `AirplaneModel` mapped to `airplane`)
CREATE TABLE IF NOT EXISTS airplane (
    id BIGSERIAL PRIMARY KEY,
    model VARCHAR(255),
    seat_capacity INTEGER,
    airline VARCHAR(255),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_user_email ON tbl_user(email);
CREATE INDEX idx_user_username ON tbl_user(user_name);
CREATE INDEX idx_user_status ON tbl_user(status);
CREATE INDEX idx_address_user_id ON tbl_address(user_id);
CREATE INDEX idx_user_role_user_id ON tbl_user_has_role(user_id);
CREATE INDEX idx_user_role_role_id ON tbl_user_has_role(role_id);
CREATE INDEX idx_role_permission_role_id ON tbl_role_has_permission(role_id);
CREATE INDEX idx_role_permission_permission_id ON tbl_role_has_permission(permission_id);
CREATE INDEX idx_aircraft_code ON aircraft(code);
CREATE INDEX idx_cabin_aircraft_id ON cabin(aircraft_id);

-- Insert default roles (only ADMIN and CUSTOMER)
INSERT INTO tbl_role (name, description, created_by) VALUES
('ADMIN', 'System Administrator with full access', 'system'),
('CUSTOMER', 'Customer with limited payment access', 'system');

-- Insert default permissions for Air Module
INSERT INTO tbl_permission (resource, action, scope, created_by) VALUES
-- User management permissions
('USER', 'CREATE', 'ALL', 'system'),
('USER', 'READ', 'ALL', 'system'),
('USER', 'UPDATE', 'ALL', 'system'),
('USER', 'DELETE', 'ALL', 'system'),
('USER', 'READ', 'SELF', 'system'),
('USER', 'UPDATE', 'SELF', 'system'),

-- Role management permissions
('ROLE', 'CREATE', 'ALL', 'system'),
('ROLE', 'READ', 'ALL', 'system'),
('ROLE', 'UPDATE', 'ALL', 'system'),
('ROLE', 'DELETE', 'ALL', 'system'),

-- Permission management permissions
('PERMISSION', 'CREATE', 'ALL', 'system'),
('PERMISSION', 'READ', 'ALL', 'system'),
('PERMISSION', 'UPDATE', 'ALL', 'system'),
('PERMISSION', 'DELETE', 'ALL', 'system'),

-- Airplane permissions
('AIRPLANE', 'CREATE', 'ALL', 'system'),
('AIRPLANE', 'UPDATE', 'ALL', 'system'),
('AIRPLANE', 'DELETE', 'ALL', 'system'),
('AIRPLANE', 'READ', 'ALL', 'system'),
('AIRPLANE', 'READ', 'SELF', 'system'),

-- Airport permissions
('AIRPORT', 'CREATE', 'ALL', 'system'),
('AIRPORT', 'UPDATE', 'ALL', 'system'),
('AIRPORT', 'DELETE', 'ALL', 'system'),
('AIRPORT', 'READ', 'ALL', 'system'),

-- Flight permissions
('FLIGHT', 'CREATE', 'ALL', 'system'),
('FLIGHT', 'UPDATE', 'ALL', 'system'),
('FLIGHT', 'CANCEL', 'ALL', 'system'),
('FLIGHT', 'DELETE', 'ALL', 'system'),
('FLIGHT', 'READ', 'ALL', 'system'),

-- Ticket Type permissions
('TICKET_TYPE', 'CREATE', 'ALL', 'system'),
('TICKET_TYPE', 'UPDATE', 'ALL', 'system'),
('TICKET_TYPE', 'DELETE', 'ALL', 'system'),
('TICKET_TYPE', 'READ', 'ALL', 'system'),

-- Ticket permissions
('TICKET', 'CREATE', 'ALL', 'system'),
('TICKET', 'UPDATE', 'ALL', 'system'),
('TICKET', 'DELETE', 'ALL', 'system'),
('TICKET', 'CANCEL', 'ALL', 'system'),
('TICKET', 'CHECKIN', 'ALL', 'system'),
('TICKET', 'READ', 'ALL', 'system'),
('TICKET', 'GENERATE', 'ALL', 'system'),

-- Booking permissions
('BOOKING', 'CREATE', 'ALL', 'system'),
('BOOKING', 'CREATE', 'OWN', 'system'),
('BOOKING', 'UPDATE', 'ALL', 'system'),
('BOOKING', 'DELETE', 'ALL', 'system'),
('BOOKING', 'CANCEL', 'ALL', 'system'),
('BOOKING', 'CANCEL', 'OWN', 'system'),
('BOOKING', 'READ', 'ALL', 'system'),
('BOOKING', 'READ', 'OWN', 'system'),
('BOOKING', 'GENERATE', 'ALL', 'system'),

-- Payment permissions
('PAYMENT', 'CREATE', 'ALL', 'system'),
('PAYMENT', 'CREATE', 'OWN', 'system'),
('PAYMENT', 'UPDATE', 'ALL', 'system'),
('PAYMENT', 'DELETE', 'ALL', 'system'),
('PAYMENT', 'CANCEL', 'ALL', 'system'),
('PAYMENT', 'READ', 'ALL', 'system'),
('PAYMENT', 'READ', 'OWN', 'system'),
('PAYMENT', 'GENERATE', 'ALL', 'system'),

-- Legacy Aircraft/Cabin permissions (for backward compatibility)
('AIRCRAFT', 'CREATE', 'ALL', 'system'),
('AIRCRAFT', 'READ', 'ALL', 'system'),
('AIRCRAFT', 'UPDATE', 'ALL', 'system'),
('AIRCRAFT', 'DELETE', 'ALL', 'system'),
('CABIN', 'CREATE', 'ALL', 'system'),
('CABIN', 'READ', 'ALL', 'system'),
('CABIN', 'UPDATE', 'ALL', 'system'),
('CABIN', 'DELETE', 'ALL', 'system');

-- Assign permissions to roles
-- ADMIN: Full access to everything
INSERT INTO tbl_role_has_permission (role_id, permission_id, created_by) 
SELECT r.id, p.id, 'system'
FROM tbl_role r, tbl_permission p 
WHERE r.name = 'ADMIN';

-- CUSTOMER: PAYMENT:READ:ALL and BOOKING:READ:OWN permissions
INSERT INTO tbl_role_has_permission (role_id, permission_id, created_by) VALUES
((SELECT id FROM tbl_role WHERE name = 'CUSTOMER'), 47, 'system'),
((SELECT id FROM tbl_role WHERE name = 'CUSTOMER'), 55, 'system'),
((SELECT id FROM tbl_role WHERE name = 'CUSTOMER'), 41, 'system'),
((SELECT id FROM tbl_role WHERE name = 'CUSTOMER'), 50, 'system');

-- Create sample users (2 admin, 4 customer)
INSERT INTO tbl_user (full_name, email, phone, password_hash, cccd_passport, date_of_birth, user_name, status, type, created_by) VALUES
-- Admin users
('Nguyen Van Admin', 'admin1@vnair.com', '+84901234567', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvox1Eubyhyp.9S.jSAcJ2RgO', '123456789012', '1985-03-15', 'admin1', 'ACTIVE', 'ADMIN', 'system'),
('Tran Thi Manager', 'admin2@vnair.com', '+84901234568', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvox1Eubyhyp.9S.jSAcJ2RgO', '987654321098', '1987-07-22', 'admin2', 'ACTIVE', 'ADMIN', 'system'),

-- Customer users  
('Le Van Khach', 'customer1@example.com', '+84912345678', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvox1Eubyhyp.9S.jSAcJ2RgO', '111222333444', '1990-01-10', 'customer1', 'ACTIVE', 'CUSTOMER', 'system'),
('Pham Thi Lan', 'customer2@example.com', '+84912345679', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvox1Eubyhyp.9S.jSAcJ2RgO', '555666777888', '1992-05-20', 'customer2', 'ACTIVE', 'CUSTOMER', 'system'),
('Hoang Van Duc', 'customer3@example.com', '+84912345680', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvox1Eubyhyp.9S.jSAcJ2RgO', '999000111222', '1988-12-03', 'customer3', 'ACTIVE', 'CUSTOMER', 'system'),
('Vu Thi Mai', 'customer4@example.com', '+84912345681', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRdvox1Eubyhyp.9S.jSAcJ2RgO', '333444555666', '1995-08-18', 'customer4', 'ACTIVE', 'CUSTOMER', 'system');

-- Assign roles to users
-- Assign ADMIN role to admin users
INSERT INTO tbl_user_has_role (user_id, role_id, created_by) 
SELECT u.id, r.id, 'system'
FROM tbl_user u, tbl_role r 
WHERE u.user_name IN ('admin1', 'admin2') AND r.name = 'ADMIN';

-- Assign CUSTOMER role to customer users
INSERT INTO tbl_user_has_role (user_id, role_id, created_by) 
SELECT u.id, r.id, 'system'
FROM tbl_user u, tbl_role r 
WHERE u.user_name IN ('customer1', 'customer2', 'customer3', 'customer4') AND r.name = 'CUSTOMER';

-- Insert sample aircraft data
INSERT INTO aircraft (code, name) VALUES
('VN-A123', 'Boeing 787-9 Dreamliner'),
('VN-A124', 'Airbus A350-900'),
('VN-A125', 'Boeing 737 MAX 8'),
('VN-A126', 'Airbus A321neo');

-- Insert sample cabin data
INSERT INTO cabin (name, position, seat_count, description, aircraft_id) VALUES
-- Boeing 787-9 cabins
('Business Class', 'FRONT', 28, 'Premium business class cabin with lie-flat seats', 1),
('Premium Economy', 'MIDDLE', 35, 'Enhanced comfort with extra legroom', 1),
('Economy Class', 'REAR', 247, 'Standard economy seating', 1),

-- Airbus A350-900 cabins
('Business Class', 'FRONT', 32, 'Luxury business class with direct aisle access', 2),
('Premium Economy', 'MIDDLE', 40, 'Comfortable seating with enhanced service', 2),
('Economy Class', 'REAR', 253, 'Standard economy configuration', 2),

-- Boeing 737 MAX 8 cabins
('Business Class', 'FRONT', 12, 'Regional business class seating', 3),
('Economy Class', 'REAR', 162, 'Single-class economy configuration', 3),

-- Airbus A321neo cabins
('Business Class', 'FRONT', 16, 'Premium cabin for medium-haul flights', 4),
('Economy Class', 'REAR', 184, 'Efficient economy class layout', 4);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to all tables with updated_at column
CREATE TRIGGER update_user_updated_at BEFORE UPDATE ON tbl_user FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_role_updated_at BEFORE UPDATE ON tbl_role FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_permission_updated_at BEFORE UPDATE ON tbl_permission FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_address_updated_at BEFORE UPDATE ON tbl_address FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_user_role_updated_at BEFORE UPDATE ON tbl_user_has_role FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_role_permission_updated_at BEFORE UPDATE ON tbl_role_has_permission FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Grant necessary permissions (adjust as needed for your database setup)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO vnair_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO vnair_user;

-- ========================================
-- AIR MODULE TABLES AND SAMPLE DATA
-- ========================================

-- Create Airport table
CREATE TABLE IF NOT EXISTS airport (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(255),
    city VARCHAR(255),
    country VARCHAR(255),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Flight table
CREATE TABLE IF NOT EXISTS flight (
    id BIGSERIAL PRIMARY KEY,
    airplane_id INTEGER NOT NULL,
    departure_airport_id INTEGER NOT NULL,
    arrival_airport_id INTEGER NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'DELAYED', 'CANCELED')),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (airplane_id) REFERENCES airplane(id) ON DELETE CASCADE,
    FOREIGN KEY (departure_airport_id) REFERENCES airport(id) ON DELETE CASCADE,
    FOREIGN KEY (arrival_airport_id) REFERENCES airport(id) ON DELETE CASCADE
);

-- Create Ticket Type table
CREATE TABLE IF NOT EXISTS ticket_type (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL CHECK (name IN ('Economy', 'Business', 'First')),
    price_multiplier DECIMAL(3,2) NOT NULL CHECK (price_multiplier >= 1.0 AND price_multiplier <= 3.0),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Ticket table
CREATE TABLE IF NOT EXISTS ticket (
    id BIGSERIAL PRIMARY KEY,
    flight_id BIGINT NOT NULL,
    ticket_type_id INTEGER NOT NULL,
    seat_number VARCHAR(10),
    status VARCHAR(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'BOOKED', 'PAID', 'CANCELED')),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flight_id) REFERENCES flight(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_type_id) REFERENCES ticket_type(id) ON DELETE CASCADE
);

-- Create Booking table
CREATE TABLE IF NOT EXISTS booking (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'PAID', 'CANCELED')),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- Create Booking Ticket junction table
CREATE TABLE IF NOT EXISTS booking_ticket (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE CASCADE,
    UNIQUE(booking_id, ticket_id)
);

-- Create Payment table
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    transaction_id VARCHAR(255) UNIQUE,
    method VARCHAR(20) CHECK (method IN ('CREDIT_CARD', 'MOMO', 'BANKING', 'CASH')),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING')),
    paid_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE
);

-- Create indexes for air module tables
CREATE INDEX IF NOT EXISTS idx_airport_code ON airport(code);
CREATE INDEX IF NOT EXISTS idx_flight_airplane_id ON flight(airplane_id);
CREATE INDEX IF NOT EXISTS idx_flight_departure_airport ON flight(departure_airport_id);
CREATE INDEX IF NOT EXISTS idx_flight_arrival_airport ON flight(arrival_airport_id);
CREATE INDEX IF NOT EXISTS idx_flight_departure_time ON flight(departure_time);
CREATE INDEX IF NOT EXISTS idx_ticket_flight_id ON ticket(flight_id);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON ticket(status);
CREATE INDEX IF NOT EXISTS idx_booking_user_id ON booking(user_id);
CREATE INDEX IF NOT EXISTS idx_booking_status ON booking(status);
CREATE INDEX IF NOT EXISTS idx_booking_ticket_booking_id ON booking_ticket(booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_ticket_ticket_id ON booking_ticket(ticket_id);
CREATE INDEX IF NOT EXISTS idx_payment_booking_id ON payment(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_transaction_id ON payment(transaction_id);

-- Apply triggers to air module tables
CREATE TRIGGER update_airport_updated_at BEFORE UPDATE ON airport FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_airplane_updated_at BEFORE UPDATE ON airplane FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_flight_updated_at BEFORE UPDATE ON flight FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_ticket_type_updated_at BEFORE UPDATE ON ticket_type FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_ticket_updated_at BEFORE UPDATE ON ticket FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_booking_updated_at BEFORE UPDATE ON booking FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_booking_ticket_updated_at BEFORE UPDATE ON booking_ticket FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payment_updated_at BEFORE UPDATE ON payment FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========================================
-- SAMPLE DATA FOR AIR MODULE
-- ========================================

-- Insert sample Airport data (Vietnam and International airports)
INSERT INTO airport (code, name, city, country, created_by) VALUES
('SGN', 'Tan Son Nhat International Airport', 'Ho Chi Minh City', 'Vietnam', 'system'),
('HAN', 'Noi Bai International Airport', 'Hanoi', 'Vietnam', 'system'),
('DAD', 'Da Nang International Airport', 'Da Nang', 'Vietnam', 'system'),
('BKK', 'Suvarnabhumi Airport', 'Bangkok', 'Thailand', 'system'),
('SIN', 'Singapore Changi Airport', 'Singapore', 'Singapore', 'system');

-- Insert additional sample Airplane data (5 airplanes total including existing one)
INSERT INTO airplane (model, seat_capacity, airline, created_by) VALUES
('Boeing 777-300ER', 350, 'Vietnam Airlines', 'system'),
('Airbus A330-300', 290, 'Vietnam Airlines', 'system'),
('Boeing 737-800', 189, 'VietJet Air', 'system'),
('Embraer E190', 100, 'Bamboo Airways', 'system');

-- Insert sample Ticket Type data (3 standard classes)
INSERT INTO ticket_type (name, price_multiplier, created_by) VALUES
('Economy', 1.00, 'system'),
('Business', 2.50, 'system'),
('First', 3.00, 'system');

-- Insert sample Flight data (domestic and international routes)
INSERT INTO flight (airplane_id, departure_airport_id, arrival_airport_id, departure_time, arrival_time, base_price, status, created_by) VALUES
(1, 1, 2, '2025-09-20 08:00:00', '2025-09-20 10:00:00', 1500000.00, 'SCHEDULED', 'system'),  -- SGN to HAN
(2, 2, 1, '2025-09-20 14:00:00', '2025-09-20 16:00:00', 1500000.00, 'SCHEDULED', 'system'),  -- HAN to SGN
(3, 1, 3, '2025-09-21 06:30:00', '2025-09-21 07:45:00', 800000.00, 'SCHEDULED', 'system'),   -- SGN to DAD
(4, 1, 4, '2025-09-22 09:15:00', '2025-09-22 10:30:00', 3500000.00, 'SCHEDULED', 'system'),  -- SGN to BKK
(1, 2, 5, '2025-09-23 11:00:00', '2025-09-23 14:30:00', 4200000.00, 'SCHEDULED', 'system');  -- HAN to SIN

-- Insert sample Ticket data (various seat types and statuses)
INSERT INTO ticket (flight_id, ticket_type_id, seat_number, status, created_by) VALUES
-- Flight 1 (SGN to HAN) - Economy tickets
(1, 1, '12A', 'AVAILABLE', 'system'),
(1, 1, '12B', 'BOOKED', 'system'),
(1, 2, '2A', 'AVAILABLE', 'system'),
-- Flight 2 (HAN to SGN) - Mixed tickets
(2, 1, '15C', 'PAID', 'system'),
(2, 2, '3B', 'AVAILABLE', 'system'),
-- Flight 3 (SGN to DAD) - Additional ticket for Customer4
(3, 1, '8A', 'BOOKED', 'system');

-- Insert sample Booking data (using the 6 sample users created above)
INSERT INTO booking (user_id, booking_date, total_amount, status, created_by) VALUES
(1, '2025-09-15 10:30:00', 1500000.00, 'CONFIRMED', 'system'),  -- Admin1 booking
(2, '2025-09-15 11:45:00', 3750000.00, 'PAID', 'system'),      -- Admin2 booking (Business class)
(3, '2025-09-15 12:00:00', 800000.00, 'PENDING', 'system'),    -- Customer1 booking
(4, '2025-09-15 13:15:00', 3500000.00, 'CONFIRMED', 'system'), -- Customer2 booking
(5, '2025-09-15 14:30:00', 4200000.00, 'PAID', 'system'),      -- Customer3 booking
(6, '2025-09-15 15:45:00', 2100000.00, 'CONFIRMED', 'system'); -- Customer4 booking

-- Insert sample Booking-Ticket relationships
INSERT INTO booking_ticket (booking_id, ticket_id, created_by) VALUES
(1, 2, 'system'),  -- Booking 1 -> Ticket 2 (12B - BOOKED)
(2, 3, 'system'),  -- Booking 2 -> Ticket 3 (2A - Business)
(3, 1, 'system'),  -- Booking 3 -> Ticket 1 (12A - Available)
(4, 4, 'system'),  -- Booking 4 -> Ticket 4 (15C - PAID)
(5, 5, 'system'),  -- Booking 5 -> Ticket 5 (3B - Business)
(6, 6, 'system');  -- Booking 6 -> Ticket 6 (8A - BOOKED)

-- Insert sample Payment data (various payment methods and statuses)
INSERT INTO payment (booking_id, amount, transaction_id, method, status, paid_at, created_by) VALUES
(1, 1500000.00, 'TXN001_20250915_103000', 'CREDIT_CARD', 'SUCCESS', '2025-09-15 10:35:00', 'system'),
(2, 3750000.00, 'TXN002_20250915_114500', 'MOMO', 'SUCCESS', '2025-09-15 11:50:00', 'system'),
(3, 800000.00, 'TXN003_20250915_120000', 'BANKING', 'PENDING', NULL, 'system'),
(4, 3500000.00, 'TXN004_20250915_131500', 'CREDIT_CARD', 'SUCCESS', '2025-09-15 13:20:00', 'system'),
(5, 4200000.00, 'TXN005_20250915_143000', 'CASH', 'SUCCESS', '2025-09-15 14:35:00', 'system'),
(6, 2100000.00, 'TXN006_20250915_154500', 'MOMO', 'SUCCESS', '2025-09-15 15:50:00', 'system');