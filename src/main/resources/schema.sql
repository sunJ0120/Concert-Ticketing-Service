-- ================================================
-- TICKETBLITZ - Option B local sample data
-- ================================================

-- ------------------------------------------------
-- 1. 회원 (users)
-- ------------------------------------------------
CREATE TABLE users
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255)        NULL,
    name       VARCHAR(100)        NOT NULL,
    role       VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------
-- 1-1. 소셜 계정 연동 (social_accounts)
-- ------------------------------------------------
CREATE TABLE social_accounts
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    provider    VARCHAR(20)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_social_account_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_provider_account UNIQUE (provider, provider_id)
);

-- ------------------------------------------------
-- 2. 공연장 템플릿 (section_templates)
-- ------------------------------------------------
CREATE TABLE section_templates
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_name    VARCHAR(100) NOT NULL,
    section_name  VARCHAR(50)  NOT NULL,
    row_count     INT          NOT NULL,
    seats_per_row INT          NOT NULL,
    color         VARCHAR(7) DEFAULT '#808080',
    created_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_venue_section UNIQUE (venue_name, section_name),
    CONSTRAINT chk_row_count_positive CHECK (row_count > 0),
    CONSTRAINT chk_seats_per_row_positive CHECK (seats_per_row > 0)
);

-- ------------------------------------------------
-- 3. 공연 (concerts) - Venue Embedded
-- ------------------------------------------------
CREATE TABLE concerts
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    title            VARCHAR(300) NOT NULL,
    artist           VARCHAR(200),
    description      TEXT,
    poster_url       VARCHAR(500),

    -- Venue Embedded 필드들
    venue_name       VARCHAR(100) NOT NULL, -- @Column(name = "venue_name")
    venue_address    VARCHAR(300) NOT NULL, -- @Column(name = "venue_address")
    venue_seats      INT,                   -- @Column(name = "venue_seats") -> totalSeats 매핑
    venue_latitude   DOUBLE,                -- @Column(name = "venue_latitude")
    venue_longitude  DOUBLE,                -- @Column(name = "venue_longitude")

    start_date       TIMESTAMP    NOT NULL,
    end_date         TIMESTAMP    NOT NULL,
    booking_start_at TIMESTAMP    NOT NULL,
    booking_end_at   TIMESTAMP    NOT NULL,
    status           VARCHAR(20) DEFAULT 'SCHEDULED',

    created_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_concert_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_booking_dates CHECK (booking_end_at >= booking_start_at)
);

-- ------------------------------------------------
-- 4. 공연별 구역 (concert_sections)
-- ------------------------------------------------
CREATE TABLE concert_sections
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    concert_id   BIGINT         NOT NULL,
    template_id  BIGINT         NOT NULL,
    price        DECIMAL(10, 0) NOT NULL,
    is_available BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_concert_section_concert
        FOREIGN KEY (concert_id) REFERENCES concerts (id) ON DELETE CASCADE,
    CONSTRAINT fk_concert_section_template
        FOREIGN KEY (template_id) REFERENCES section_templates (id),
    CONSTRAINT uk_concert_template UNIQUE (concert_id, template_id),
    CONSTRAINT chk_price_positive CHECK (price >= 0)
);

-- ------------------------------------------------
-- 5. 공연별 좌석 (concert_seats)
-- ------------------------------------------------
CREATE TABLE concert_seats
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_id  BIGINT      NOT NULL,
    row_num     INT         NOT NULL,
    seat_num    INT         NOT NULL,
    seat_label  VARCHAR(20),
    seat_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at  TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_concert_seat_section
        FOREIGN KEY (section_id) REFERENCES concert_sections (id) ON DELETE CASCADE,
    CONSTRAINT uk_seat_position UNIQUE (section_id, row_num, seat_num),
    CONSTRAINT chk_row_positive CHECK (row_num > 0),
    CONSTRAINT chk_seat_positive CHECK (seat_num > 0)
);

-- ------------------------------------------------
-- 6. 예매 (reservations)
-- ------------------------------------------------
CREATE TABLE reservations
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id            BIGINT         NOT NULL,
    seat_id            BIGINT         NOT NULL,
    price              DECIMAL(10, 0) NOT NULL,
    reservation_status VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    reserved_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at       TIMESTAMP      NULL,
    cancelled_at       TIMESTAMP      NULL,
    expires_at         TIMESTAMP      NULL,
    created_at         TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reservation_seat
        FOREIGN KEY (seat_id) REFERENCES concert_seats (id),
    CONSTRAINT chk_reservation_price_positive CHECK (price >= 0)
);

-- ------------------------------------------------
-- 7. 결제 (payments)
-- ------------------------------------------------
CREATE TABLE payments
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id    BIGINT         NOT NULL,
    order_id          VARCHAR(50)    NOT NULL UNIQUE,

    amount            DECIMAL(10, 0) NOT NULL,
    payment_method    VARCHAR(20)    NOT NULL,
    payment_status    VARCHAR(20)    NOT NULL DEFAULT 'PENDING',

    -- PG 응답 정보
    pg_transaction_id VARCHAR(100), -- 다날: transactionId
    pg_response_code  VARCHAR(20),  -- 다날: code (SUCCESS, FAIL 등)

    -- 타임스탬프
    initiated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at      TIMESTAMP      NULL,
    refunded_at       TIMESTAMP      NULL,
    created_at        TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE RESTRICT,
    CONSTRAINT chk_payment_amount_positive CHECK (amount >= 0)
);

-- payment_logs (감사 추적)
CREATE TABLE payment_logs
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id          BIGINT      NOT NULL,
    event_type          VARCHAR(20) NOT NULL, -- INITIATED, CONFIRMED, FAILED, REFUNDED
    old_status          VARCHAR(20),
    new_status          VARCHAR(20),

    -- PG 상세 응답 (로그용)
    pg_response_code    VARCHAR(20),          -- SUCCESS, FAIL
    pg_response_message TEXT,                 -- 상세 메시지
    pg_raw_response     TEXT,                 -- 원본 JSON (디버깅용)

    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_log_payment
        FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE
);


-- ================================================
-- 샘플 데이터
-- ================================================

-- ------------------------------------------------
-- 1. 공연장 템플릿 (section_templates)
-- ------------------------------------------------
INSERT INTO section_templates (venue_name, section_name, row_count, seats_per_row, color)
VALUES ('올림픽공원 체조경기장', 'VIP', 2, 12, '#FFD700'),
       ('올림픽공원 체조경기장', 'R', 8, 16, '#E066FF'),
       ('올림픽공원 체조경기장', 'S', 6, 20, '#00BFFF'),
       ('올림픽공원 체조경기장', 'A', 4, 22, '#7CFC00');

INSERT INTO section_templates (venue_name, section_name, row_count, seats_per_row, color)
VALUES ('고척스카이돔', 'VIP', 3, 15, '#FFD700'),
       ('고척스카이돔', 'R', 10, 20, '#E066FF'),
       ('고척스카이돔', 'S', 8, 25, '#00BFFF'),
       ('고척스카이돔', 'A', 6, 30, '#7CFC00');

INSERT INTO section_templates (venue_name, section_name, row_count, seats_per_row, color)
VALUES ('잠실실내체육관', 'VIP', 2, 10, '#FFD700'),
       ('잠실실내체육관', 'R', 6, 15, '#E066FF'),
       ('잠실실내체육관', 'S', 5, 18, '#00BFFF');

-- ------------------------------------------------
-- 2. 공연 (concerts) - Venue Embedded
-- ------------------------------------------------
INSERT INTO concerts (title, artist, description, poster_url,
                      venue_name, venue_address, venue_seats,
                      start_date, end_date,
                      booking_start_at, booking_end_at,
                      status)
VALUES ('IU Concert: The Golden Hour',
        'IU',
        '아이유의 감성 라이브 콘서트',
        'https://example.com/posters/iu-golden-hour.jpg',
        '올림픽공원 체조경기장',
        '서울특별시 송파구 올림픽로 424',
        15000,
        '2025-03-15 19:00:00',
        '2025-03-15 22:00:00',
        '2025-01-15 20:00:00',
        '2025-03-15 18:00:00',
        'BOOKING_OPEN'), -- ✅ BOOKING → BOOKING_OPEN
       ('BTS Yet To Come',
        'BTS',
        'BTS 컴백 콘서트',
        'https://example.com/posters/bts-yet-to-come.jpg',
        '올림픽공원 체조경기장',
        '서울특별시 송파구 올림픽로 424',
        15000,
        '2025-04-20 18:00:00',
        '2025-04-20 22:00:00',
        '2025-02-20 20:00:00',
        '2025-04-20 17:00:00',
        'SCHEDULED');

-- ------------------------------------------------
-- 3. 공연별 구역 (concert_sections)
-- ------------------------------------------------
INSERT INTO concert_sections (concert_id, template_id, price, is_available)
VALUES (1, 1, 220000, TRUE), -- VIP
       (1, 2, 154000, TRUE), -- R
       (1, 3, 110000, TRUE), -- S
       (1, 4, 77000, TRUE); -- A

INSERT INTO concert_sections (concert_id, template_id, price, is_available)
VALUES (2, 1, 300000, TRUE), -- VIP
       (2, 2, 200000, TRUE), -- R
       (2, 3, 150000, TRUE), -- S
       (2, 4, 100000, TRUE);
-- A

-- ------------------------------------------------
-- 4. 공연별 좌석 (concert_seats) - 샘플만
-- ------------------------------------------------
-- IU 콘서트 VIP 좌석 (전체: 2줄 × 12석 = 24석)
INSERT INTO concert_seats (section_id, row_num, seat_num, seat_label, seat_status)
SELECT 1                                          as section_id,
       r.row_num,
       s.seat_num,
       CONCAT('VIP-', r.row_num, '-', s.seat_num) as seat_label,
       'AVAILABLE'                                as seat_status
FROM (SELECT 1 as row_num UNION SELECT 2) r,
     (SELECT 1 as seat_num
      UNION
      SELECT 2
      UNION
      SELECT 3
      UNION
      SELECT 4
      UNION
      SELECT 5
      UNION
      SELECT 6
      UNION
      SELECT 7
      UNION
      SELECT 8
      UNION
      SELECT 9
      UNION
      SELECT 10
      UNION
      SELECT 11
      UNION
      SELECT 12) s;

-- IU 콘서트 R 좌석 (샘플: 처음 2줄만)
INSERT INTO concert_seats (section_id, row_num, seat_num, seat_label, seat_status)
SELECT 2                                        as section_id,
       r.row_num,
       s.seat_num,
       CONCAT('R-', r.row_num, '-', s.seat_num) as seat_label,
       'AVAILABLE'                              as seat_status
FROM (SELECT 1 as row_num UNION SELECT 2) r,
     (SELECT 1 as seat_num
      UNION
      SELECT 2
      UNION
      SELECT 3
      UNION
      SELECT 4
      UNION
      SELECT 5
      UNION
      SELECT 6
      UNION
      SELECT 7
      UNION
      SELECT 8
      UNION
      SELECT 9
      UNION
      SELECT 10
      UNION
      SELECT 11
      UNION
      SELECT 12
      UNION
      SELECT 13
      UNION
      SELECT 14
      UNION
      SELECT 15
      UNION
      SELECT 16) s;

-- BTS 콘서트 VIP 좌석 (전체: 2줄 × 12석 = 24석)
INSERT INTO concert_seats (section_id, row_num, seat_num, seat_label, seat_status)
SELECT 5                                          as section_id,
       r.row_num,
       s.seat_num,
       CONCAT('VIP-', r.row_num, '-', s.seat_num) as seat_label,
       'AVAILABLE'                                as seat_status
FROM (SELECT 1 as row_num UNION SELECT 2) r,
     (SELECT 1 as seat_num
      UNION
      SELECT 2
      UNION
      SELECT 3
      UNION
      SELECT 4
      UNION
      SELECT 5
      UNION
      SELECT 6
      UNION
      SELECT 7
      UNION
      SELECT 8
      UNION
      SELECT 9
      UNION
      SELECT 10
      UNION
      SELECT 11
      UNION
      SELECT 12) s;

-- ------------------------------------------------
-- 5. 사용자 (users)
-- ------------------------------------------------
INSERT INTO users (email, password, name, role)
VALUES ('admin@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.wF5gH1C5MNKJsWqE.m',
        '관리자', 'ADMIN'),
       ('user1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.wF5gH1C5MNKJsWqE.m',
        '김철수', 'USER'),
       ('user2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.wF5gH1C5MNKJsWqE.m',
        '이영희', 'USER');


-- ================================================
-- 샘플 예매 데이터
-- ================================================

-- user1이 IU 콘서트 VIP-1-1 예매
INSERT INTO reservations (user_id, seat_id, price, reservation_status, reserved_at,
                          expires_at)
VALUES (2, 1, 220000, 'CONFIRMED', CURRENT_TIMESTAMP, DATEADD('MINUTE', 15, CURRENT_TIMESTAMP));

-- user2가 IU 콘서트 VIP-1-2 예매 (결제 대기)
INSERT INTO reservations (user_id, seat_id, price, reservation_status, reserved_at, expires_at)
VALUES (3, 2, 220000, 'PENDING', CURRENT_TIMESTAMP, DATEADD('MINUTE', 15, CURRENT_TIMESTAMP));

-- 예매한 좌석 상태 업데이트
UPDATE concert_seats
SET seat_status = 'SOLD'
WHERE id = 1;
UPDATE concert_seats
SET seat_status = 'RESERVED'
WHERE id = 2;