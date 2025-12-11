-- ================================================
-- 좌석 예매 시스템 스키마 (확장 버전)
-- ================================================

USE ticketing_db;

-- ------------------------------------------------
-- 1. 회원 (users)
-- ------------------------------------------------
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NULL,           -- NULL 허용 (소셜 로그인)
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------
-- 1-1. 소셜 계정 연동 (social_accounts)
-- ------------------------------------------------
CREATE TABLE social_accounts (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     user_id BIGINT NOT NULL,
     provider ENUM('KAKAO', 'NAVER', 'GOOGLE') NOT NULL,
     provider_id VARCHAR(255) NOT NULL,
     email VARCHAR(255),
     name VARCHAR(100),
     profile_image VARCHAR(500),
     access_token VARCHAR(500),
     refresh_token VARCHAR(500),
     token_expires_at TIMESTAMP NULL,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
     UNIQUE KEY uk_provider_account (provider, provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------
-- 2. 공연장 (venues)
-- ------------------------------------------------
CREATE TABLE venues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    total_seats INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------
-- 3. 구역 (sections) - VIP, R, S, A 등
-- ------------------------------------------------
CREATE TABLE sections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,           -- VIP, R, S, A
    base_price DECIMAL(10, 0) NOT NULL,  -- 기본 가격
    color VARCHAR(7),                     -- UI 색상 (#FFD700)
    row_count INT NOT NULL DEFAULT 0,
    seats_per_row INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------
-- 4. 좌석 (seats) - 공연장에 고정
-- ------------------------------------------------
CREATE TABLE seats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_id BIGINT NOT NULL,
    row_num INT NOT NULL,                 -- 열 번호
    seat_num INT NOT NULL,                -- 좌석 번호
    seat_label VARCHAR(20),               -- 표시용 (A1, A2, VIP-1-5)
    is_active BOOLEAN DEFAULT TRUE,       -- 좌석 사용 가능 여부
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE,
    UNIQUE KEY uk_seat_position (section_id, row_num, seat_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------
-- 5. 공연 (concerts)
-- ------------------------------------------------
CREATE TABLE concerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    artist VARCHAR(200),
    description TEXT,
    poster_url VARCHAR(500),
    concert_date DATE NOT NULL,
    concert_time TIME NOT NULL,
    booking_start_at TIMESTAMP NOT NULL,  -- 예매 오픈 시간
    booking_end_at TIMESTAMP NOT NULL,    -- 예매 마감 시간
    status ENUM('SCHEDULED', 'BOOKING', 'SOLD_OUT', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (venue_id) REFERENCES venues(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------
-- 6. 예매 (reservations)
-- ------------------------------------------------
CREATE TABLE reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    concert_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED') DEFAULT 'PENDING',
    price DECIMAL(10, 0) NOT NULL,
    reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,            -- 결제 대기 만료 시간

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (concert_id) REFERENCES concerts(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id),
    UNIQUE KEY uk_concert_seat (concert_id, seat_id)  -- 같은 공연, 같은 좌석 중복 예매 방지
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------
-- 7. 결제 (payments)
-- ------------------------------------------------
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    -- 다날페이 연동 필드
    order_id VARCHAR(50) NOT NULL UNIQUE,  -- 가맹점 주문번호 (필수)
    pg_transaction_id VARCHAR(32),          -- 다날 transactionId
    merchant_id VARCHAR(20),                -- CPID

    amount DECIMAL(10, 0) NOT NULL,
    payment_method ENUM('CARD', 'MOBILE', 'TRANSFER', 'VACCOUNT') NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'PARTIAL_REFUND') DEFAULT 'PENDING',

    -- PG 응답 저장
    pg_response_code VARCHAR(20),
    pg_response_message TEXT,

    paid_at TIMESTAMP NULL,
    refunded_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ------------------------------------------------
-- 8. 대기열 (waiting_queue) - 트래픽 대비
-- ------------------------------------------------
CREATE TABLE waiting_queue (
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   user_id BIGINT NOT NULL,
   concert_id BIGINT NOT NULL,
   queue_token VARCHAR(100) UNIQUE NOT NULL,  -- 대기열 토큰
   position INT NOT NULL,                      -- 대기 순번
   status ENUM('WAITING', 'PROCESSING', 'COMPLETED', 'EXPIRED') DEFAULT 'WAITING',
   entered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   activated_at TIMESTAMP NULL,               -- 입장 허용 시간
   expires_at TIMESTAMP NULL,                 -- 만료 시간

   FOREIGN KEY (user_id) REFERENCES users(id),
   FOREIGN KEY (concert_id) REFERENCES concerts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 샘플 데이터
-- ================================================

-- 공연장
INSERT INTO venues (name, address, total_seats) VALUES
    ('올림픽공원 체조경기장', '서울특별시 송파구 올림픽로 424', 15000),
    ('고척스카이돔', '서울특별시 구로구 경인로 430', 20000),
    ('잠실실내체육관', '서울특별시 송파구 올림픽로 25', 11000);

-- 구역 (체조경기장 기준)
INSERT INTO sections (venue_id, name, base_price, color, row_count, seats_per_row) VALUES
    (1, 'VIP', 220000, '#FFD700', 2, 12),
    (1, 'R', 154000, '#E066FF', 8, 16),
    (1, 'S', 110000, '#00BFFF', 6, 20),
    (1, 'A', 77000, '#7CFC00', 4, 22);

-- 좌석 생성 (VIP 구역 예시 - 2열 x 12석)
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

-- 공연 샘플
INSERT INTO concerts (venue_id, title, artist, concert_date, concert_time, booking_start_at, booking_end_at, status) VALUES
    (1, 'IU Concert: The Golden Hour', 'IU', '2025-03-15', '19:00:00', '2025-01-15 20:00:00', '2025-03-15 18:00:00', 'BOOKING'),
    (1, 'BTS Yet To Come', 'BTS', '2025-04-20', '18:00:00', '2025-02-20 20:00:00', '2025-04-20 17:00:00', 'SCHEDULED');

-- 테스트 유저 (password: test1234 - BCrypt 암호화 필요)
INSERT INTO users (email, password, name, phone, role) VALUES
    ('admin@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.wF5gH1C5MNKJsWqE.m', '관리자', '010-1234-5678', 'ADMIN'),
    ('user@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.wF5gH1C5MNKJsWqE.m', '테스트유저', '010-9876-5432', 'USER');