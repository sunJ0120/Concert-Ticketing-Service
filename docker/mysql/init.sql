-- ================================================
-- TICKETBLITZ - MySQL 버전
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
    updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
-- 2. 건물 (buildings)
-- ------------------------------------------------
CREATE TABLE buildings
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    address    VARCHAR(300) NOT NULL,
    latitude   DOUBLE       NOT NULL,
    longitude  DOUBLE       NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ------------------------------------------------
-- 3. 홀 템플릿 (hall_templates)
-- ------------------------------------------------
CREATE TABLE hall_templates
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT       NOT NULL,
    hall_name   VARCHAR(100) NOT NULL,
    total_seats INT          NOT NULL,
    total_rows  INT          NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_hall_template_building
        FOREIGN KEY (building_id) REFERENCES buildings (id) ON DELETE CASCADE,
    CONSTRAINT uk_building_hall UNIQUE (building_id, hall_name),
    CONSTRAINT chk_total_seats_positive CHECK (total_seats > 0),
    CONSTRAINT chk_total_rows_positive CHECK (total_rows > 0)
);

-- ------------------------------------------------
-- 4. 홀 좌석 위치 (hall_seat_positions)
-- ------------------------------------------------
CREATE TABLE hall_seat_positions
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    hall_template_id BIGINT NOT NULL,
    row_num          INT    NOT NULL,
    seat_num         INT    NOT NULL,
    x_coord          DOUBLE NOT NULL,
    y_coord          DOUBLE NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_hall_seat_position_hall
        FOREIGN KEY (hall_template_id) REFERENCES hall_templates (id) ON DELETE CASCADE,
    CONSTRAINT uk_hall_position UNIQUE (hall_template_id, row_num, seat_num),
    CONSTRAINT chk_row_num_positive CHECK (row_num > 0),
    CONSTRAINT chk_seat_num_positive CHECK (seat_num > 0)
);

-- ------------------------------------------------
-- 5. 공연 (concerts)
-- ------------------------------------------------
CREATE TABLE concerts
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    hall_template_id BIGINT       NOT NULL,
    title            VARCHAR(300) NOT NULL,
    artist           VARCHAR(200),
    description      TEXT,
    poster_url       VARCHAR(500),

    start_date       TIMESTAMP    NOT NULL,
    end_date         TIMESTAMP    NOT NULL,
    booking_start_at TIMESTAMP    NOT NULL,
    booking_end_at   TIMESTAMP    NOT NULL,
    concert_status   VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',

    created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_concert_hall_template
        FOREIGN KEY (hall_template_id) REFERENCES hall_templates (id) ON DELETE RESTRICT,
    CONSTRAINT chk_concert_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_booking_dates CHECK (booking_end_at >= booking_start_at)
);

-- ------------------------------------------------
-- 6. 공연별 구역 (concert_sections)
-- ------------------------------------------------
CREATE TABLE concert_sections
(
    concert_id   BIGINT         NOT NULL,
    section_name VARCHAR(50)    NOT NULL,
    row_start    INT            NOT NULL,
    row_end      INT            NOT NULL,
    price        DECIMAL(10, 0) NOT NULL,
    color        VARCHAR(7) DEFAULT '#808080',

    CONSTRAINT fk_concert_section_concert
        FOREIGN KEY (concert_id) REFERENCES concerts (id) ON DELETE CASCADE,
    CONSTRAINT chk_section_rows CHECK (row_end >= row_start),
    CONSTRAINT chk_section_price_positive CHECK (price >= 0),

    PRIMARY KEY (concert_id, section_name)
);

-- ------------------------------------------------
-- 7. 공연별 좌석 (concert_seats)
-- ------------------------------------------------
CREATE TABLE concert_seats
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    concert_id            BIGINT      NOT NULL,
    hall_seat_position_id BIGINT      NOT NULL,
    section_name          VARCHAR(50) NOT NULL,
    seat_status           VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at            TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_concert_seat_concert
        FOREIGN KEY (concert_id) REFERENCES concerts (id) ON DELETE CASCADE,
    CONSTRAINT fk_concert_seat_position
        FOREIGN KEY (hall_seat_position_id) REFERENCES hall_seat_positions (id),
    CONSTRAINT uk_concert_seat UNIQUE (concert_id, hall_seat_position_id)
);

-- ------------------------------------------------
-- 8. 예매 (reservations)
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
    updated_at         TIMESTAMP               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_reservation_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reservation_seat
        FOREIGN KEY (seat_id) REFERENCES concert_seats (id),
    CONSTRAINT chk_reservation_price_positive CHECK (price >= 0)
);

-- ------------------------------------------------
-- 9. 결제 (payments)
-- ------------------------------------------------
CREATE TABLE payments
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id    BIGINT         NOT NULL,
    order_id          VARCHAR(50)    NOT NULL UNIQUE,

    amount            DECIMAL(10, 0) NOT NULL,
    payment_method    VARCHAR(20)    NOT NULL,
    payment_status    VARCHAR(20)    NOT NULL DEFAULT 'PENDING',

    pg_transaction_id VARCHAR(100),
    pg_response_code  VARCHAR(20),

    initiated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at      TIMESTAMP      NULL,
    refunded_at       TIMESTAMP      NULL,
    created_at        TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE RESTRICT,
    CONSTRAINT chk_payment_amount_positive CHECK (amount >= 0)
);

-- ------------------------------------------------
-- 10. 결제 로그 (payment_logs)
-- ------------------------------------------------
CREATE TABLE payment_logs
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id          BIGINT      NOT NULL,
    event_type          VARCHAR(20) NOT NULL,
    old_status          VARCHAR(20),
    new_status          VARCHAR(20),

    pg_response_code    VARCHAR(20),
    pg_response_message TEXT,
    pg_raw_response     TEXT,

    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_log_payment
        FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE
);


-- ================================================
-- 샘플 데이터
-- ================================================

-- ------------------------------------------------
-- 1. 건물 (buildings)
-- ------------------------------------------------
INSERT INTO buildings (name, address, latitude, longitude)
VALUES ('올림픽공원', '서울특별시 송파구 올림픽로 424', 37.5219, 127.1241),
       ('고척돔', '서울특별시 구로구 경인로 430', 37.4989, 126.8672),
       ('잠실종합운동장', '서울특별시 송파구 올림픽로 25', 37.5145, 127.0719);

-- ------------------------------------------------
-- 2. 홀 템플릿 (hall_templates)
-- ------------------------------------------------
INSERT INTO hall_templates (building_id, hall_name, total_seats, total_rows)
VALUES (1, '체조경기장', 15000, 20),
       (2, '스카이돔', 25000, 27),
       (3, '실내체육관', 12000, 13);

-- ------------------------------------------------
-- 3. 홀 좌석 위치 (hall_seat_positions)
-- ------------------------------------------------
-- VIP 구역 (1~2행, 각 12석)
INSERT INTO hall_seat_positions (hall_template_id, row_num, seat_num, x_coord, y_coord)
WITH RECURSIVE
    rows AS (SELECT 1 AS row_num
             UNION ALL
             SELECT row_num + 1
             FROM rows
             WHERE row_num < 2),
    seats AS (SELECT 1 AS seat_num
              UNION ALL
              SELECT seat_num + 1
              FROM seats
              WHERE seat_num < 12)
SELECT 1                   AS hall_template_id,
       r.row_num,
       s.seat_num,
       (s.seat_num * 10.0) AS x_coord,
       (r.row_num * 15.0)  AS y_coord
FROM rows r
         CROSS JOIN seats s;

-- R 구역 (3~4행, 각 16석)
INSERT INTO hall_seat_positions (hall_template_id, row_num, seat_num, x_coord, y_coord)
WITH RECURSIVE
    rows AS (SELECT 3 AS row_num
             UNION ALL
             SELECT row_num + 1
             FROM rows
             WHERE row_num < 4),
    seats AS (SELECT 1 AS seat_num
              UNION ALL
              SELECT seat_num + 1
              FROM seats
              WHERE seat_num < 16)
SELECT 1                   AS hall_template_id,
       r.row_num,
       s.seat_num,
       (s.seat_num * 10.0) AS x_coord,
       (r.row_num * 15.0)  AS y_coord
FROM rows r
         CROSS JOIN seats s;

-- S 구역 (11~12행, 각 20석)
INSERT INTO hall_seat_positions (hall_template_id, row_num, seat_num, x_coord, y_coord)
WITH RECURSIVE
    rows AS (SELECT 11 AS row_num
             UNION ALL
             SELECT row_num + 1
             FROM rows
             WHERE row_num < 12),
    seats AS (SELECT 1 AS seat_num
              UNION ALL
              SELECT seat_num + 1
              FROM seats
              WHERE seat_num < 20)
SELECT 1                   AS hall_template_id,
       r.row_num,
       s.seat_num,
       (s.seat_num * 10.0) AS x_coord,
       (r.row_num * 15.0)  AS y_coord
FROM rows r
         CROSS JOIN seats s;

-- A 구역 (17행, 22석)
INSERT INTO hall_seat_positions (hall_template_id, row_num, seat_num, x_coord, y_coord)
WITH RECURSIVE seats AS (SELECT 1 AS seat_num
                         UNION ALL
                         SELECT seat_num + 1
                         FROM seats
                         WHERE seat_num < 22)
SELECT 1                   AS hall_template_id,
       17                  AS row_num,
       s.seat_num,
       (s.seat_num * 10.0) AS x_coord,
       (17 * 15.0)         AS y_coord
FROM seats s;

-- ------------------------------------------------
-- 4. 공연 (concerts)
-- ------------------------------------------------
INSERT INTO concerts (hall_template_id, title, artist, description, poster_url,
                      start_date, end_date, booking_start_at, booking_end_at, concert_status)
VALUES (1,
        'IU Concert: The Golden Hour',
        'IU',
        '아이유의 감성 라이브 콘서트',
        'https://example.com/posters/iu-golden-hour.jpg',
        '2025-03-15 19:00:00',
        '2025-03-15 22:00:00',
        '2025-01-15 20:00:00',
        '2025-03-15 18:00:00',
        'BOOKING_OPEN'),
       (1,
        'BTS Yet To Come',
        'BTS',
        'BTS 컴백 콘서트',
        'https://example.com/posters/bts-yet-to-come.jpg',
        '2025-04-20 18:00:00',
        '2025-04-20 22:00:00',
        '2025-02-20 20:00:00',
        '2025-04-20 17:00:00',
        'SCHEDULED');

-- ------------------------------------------------
-- 5. 공연별 구역 (concert_sections)
-- ------------------------------------------------
-- IU 콘서트 (concert_id = 1)
INSERT INTO concert_sections (concert_id, section_name, row_start, row_end, price)
VALUES (1, 'VIP', 1, 2, 220000),
       (1, 'R석', 3, 10, 154000),
       (1, 'S석', 11, 16, 110000),
       (1, 'A석', 17, 20, 77000);

-- BTS 콘서트 (concert_id = 2)
INSERT INTO concert_sections (concert_id, section_name, row_start, row_end, price)
VALUES (2, 'VIP', 1, 4, 300000),
       (2, 'R석', 5, 12, 200000),
       (2, 'S석', 13, 18, 150000),
       (2, 'A석', 19, 20, 100000);

-- ------------------------------------------------
-- 6. 공연별 좌석 (concert_seats)
-- ------------------------------------------------
-- IU 콘서트 VIP 좌석 (1~2행)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 1           AS concert_id,
       hsp.id      AS hall_seat_position_id,
       'VIP'       AS section_name,
       'AVAILABLE' AS seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 1 AND 2;

-- IU 콘서트 R석 좌석 (3~4행)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 1           AS concert_id,
       hsp.id      AS hall_seat_position_id,
       'R석'        AS section_name,
       'AVAILABLE' AS seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 3 AND 4;

-- IU 콘서트 S석 좌석 (11~12행)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 1           AS concert_id,
       hsp.id      AS hall_seat_position_id,
       'S석'        AS section_name,
       'AVAILABLE' AS seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 11 AND 12;

-- IU 콘서트 A석 좌석 (17행)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 1           AS concert_id,
       hsp.id      AS hall_seat_position_id,
       'A석'        AS section_name,
       'AVAILABLE' AS seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num = 17;

-- BTS 콘서트 VIP 좌석 (1~2행)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 2           AS concert_id,
       hsp.id      AS hall_seat_position_id,
       'VIP'       AS section_name,
       'AVAILABLE' AS seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 1 AND 2;

-- BTS 콘서트 R석 좌석 (3~4행)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 2           AS concert_id,
       hsp.id      AS hall_seat_position_id,
       'R석'        AS section_name,
       'AVAILABLE' AS seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 3 AND 4;

-- ------------------------------------------------
-- 7. 사용자 (users)
-- ------------------------------------------------
INSERT INTO users (email, password, name, role)
VALUES ('admin@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.wF5gH1C5MNKJsWqE.m',
        '관리자', 'ADMIN'),
       ('user1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.wF5gH1C5MNKJsWqE.m',
        '김철수', 'USER'),
       ('user2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.wF5gH1C5MNKJsWqE.m',
        '이영희', 'USER');

-- ------------------------------------------------
-- 8. 예매 (reservations)
-- ------------------------------------------------
-- user1이 IU 콘서트 1행 1번 좌석 예매 (확정)
INSERT INTO reservations (user_id, seat_id, price, reservation_status, reserved_at, confirmed_at,
                          expires_at)
VALUES (2, 1, 220000, 'CONFIRMED', NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE));

-- user2가 IU 콘서트 1행 2번 좌석 예매 (결제 대기)
INSERT INTO reservations (user_id, seat_id, price, reservation_status, reserved_at, expires_at)
VALUES (3, 2, 220000, 'PENDING', NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE));

-- 예매한 좌석 상태 업데이트
UPDATE concert_seats
SET seat_status = 'SOLD'
WHERE id = 1;
UPDATE concert_seats
SET seat_status = 'RESERVED'
WHERE id = 2;

-- ------------------------------------------------
-- 9. 결제 (payments)
-- ------------------------------------------------
INSERT INTO payments (reservation_id, order_id, amount, payment_method, payment_status,
                      pg_transaction_id, pg_response_code, initiated_at, confirmed_at)
VALUES (1, 'ORDER-2025-001', 220000, 'CARD', 'CONFIRMED',
        'PG-TXN-12345', 'SUCCESS', NOW(), NOW());