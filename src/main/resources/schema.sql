-- ================================================
-- 좌석 예매 시스템 스키마 (H2 호환 버전)
-- ================================================

-- ------------------------------------------------
-- 1. 회원 (users)
-- ------------------------------------------------
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------
-- 1-1. 소셜 계정 연동 (social_accounts)
-- ------------------------------------------------
CREATE TABLE social_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    name VARCHAR(100),
    profile_image VARCHAR(500),
    access_token VARCHAR(500),
    refresh_token VARCHAR(500),
    token_expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_provider_account UNIQUE (provider, provider_id)
);

-- ------------------------------------------------
-- 2. 공연장 (venues)
-- ------------------------------------------------
CREATE TABLE venues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    total_seats INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------
-- 3. 구역 (sections)
-- ------------------------------------------------
CREATE TABLE sections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    base_price DECIMAL(10, 0) NOT NULL,
    color VARCHAR(7),
    row_count INT NOT NULL DEFAULT 0,
    seats_per_row INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
);

-- ------------------------------------------------
-- 4. 좌석 (seats)
-- ------------------------------------------------
CREATE TABLE seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_id BIGINT NOT NULL,
    row_num INT NOT NULL,
    seat_num INT NOT NULL,
    seat_label VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE,
    CONSTRAINT uk_seat_position UNIQUE (section_id, row_num, seat_num)
);

-- ------------------------------------------------
-- 5. 공연 (concerts)
-- ------------------------------------------------
CREATE TABLE concerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    artist VARCHAR(200),
    description CLOB,
    poster_url VARCHAR(500),
    concert_date DATE NOT NULL,
    concert_time TIME NOT NULL,
    booking_start_at TIMESTAMP NOT NULL,
    booking_end_at TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (venue_id) REFERENCES venues(id)
);

-- ------------------------------------------------
-- 6. 예매 (reservations)
-- ------------------------------------------------
CREATE TABLE reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    concert_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    price DECIMAL(10, 0) NOT NULL,
    reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (concert_id) REFERENCES concerts(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id),
    CONSTRAINT uk_concert_seat UNIQUE (concert_id, seat_id)
);

-- ------------------------------------------------
-- 7. 결제 (payments)
-- ------------------------------------------------
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id VARCHAR(50) NOT NULL UNIQUE,
    pg_transaction_id VARCHAR(32),
    merchant_id VARCHAR(20),
    amount DECIMAL(10, 0) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    pg_response_code VARCHAR(20),
    pg_response_message CLOB,
    paid_at TIMESTAMP NULL,
    refunded_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ------------------------------------------------
-- 8. 대기열 (waiting_queue)
-- ------------------------------------------------
CREATE TABLE waiting_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    concert_id BIGINT NOT NULL,
    queue_token VARCHAR(100) UNIQUE NOT NULL,
    position INT NOT NULL,
    status VARCHAR(20) DEFAULT 'WAITING',
    entered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (concert_id) REFERENCES concerts(id)
);

-- ================================================
-- 샘플 데이터
-- ================================================

INSERT INTO venues (name, address, total_seats) VALUES
    ('올림픽공원 체조경기장', '서울특별시 송파구 올림픽로 424', 15000),
    ('고척스카이돔', '서울특별시 구로구 경인로 430', 20000),
    ('잠실실내체육관', '서울특별시 송파구 올림픽로 25', 11000);

INSERT INTO sections (venue_id, name, base_price, color, row_count, seats_per_row) VALUES
    (1, 'VIP', 220000, '#FFD700', 2, 12),
    (1, 'R', 154000, '#E066FF', 8, 16),
    (1, 'S', 110000, '#00BFFF', 6, 20),
    (1, 'A', 77000, '#7CFC00', 4, 22);

-- 좌석 생성 (VIP 구역)
INSERT INTO seats (section_id, row_num, seat_num, seat_label)
SELECT
    1 as section_id,
    r.row_num,
    s.seat_num,
    CONCAT('VIP-', r.row_num, '-', s.seat_num) as seat_label
FROM
    (SELECT 1 as row_num UNION SELECT 2) r,
    (SELECT 1 as seat_num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6
     UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12) s;

INSERT INTO concerts (venue_id, title, artist, concert_date, concert_time, booking_start_at, booking_end_at, status) VALUES
    (1, 'IU Concert: The Golden Hour', 'IU', '2025-03-15', '19:00:00', '2025-01-15 20:00:00', '2025-03-15 18:00:00', 'BOOKING'),
    (1, 'BTS Yet To Come', 'BTS', '2025-04-20', '18:00:00', '2025-02-20 20:00:00', '2025-04-20 17:00:00', 'SCHEDULED');

INSERT INTO users (email, password, name, phone, role) VALUES
    ('admin@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.wF5gH1C5MNKJsWqE.m', '관리자', '010-1234-5678', 'ADMIN');