-- ================================================
-- TICKETBLITZ - 최신 설계 반영
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
-- 2. 건물 (buildings)
-- ------------------------------------------------
CREATE TABLE buildings
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL, -- "블루스퀘어"
    address    VARCHAR(300) NOT NULL,
    latitude   DOUBLE       NOT NULL,
    longitude  DOUBLE       NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------
-- 3. 홀 템플릿 (hall_templates)
-- ------------------------------------------------
CREATE TABLE hall_templates
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT       NOT NULL,
    hall_name   VARCHAR(100) NOT NULL, -- "신한카드홀"
    total_seats INT          NOT NULL,
    total_rows  INT          NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

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
    x_coord          DOUBLE NOT NULL, -- 좌석 좌표 x
    y_coord          DOUBLE NOT NULL, -- 좌석 좌표 y
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

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
    genre            VARCHAR(20)  NOT NULL,
    view_count       BIGINT       NOT NULL DEFAULT 0,

    created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_concert_hall_template
        FOREIGN KEY (hall_template_id) REFERENCES hall_templates (id) ON DELETE RESTRICT,
    CONSTRAINT chk_concert_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_booking_dates CHECK (booking_end_at >= booking_start_at)
);

-- ------------------------------------------------
-- 6. 공연별 구역 (concert_sections)
-- @ElementCollection - JPA가 자동 관리
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

    -- @ElementCollection은 복합키 사용
    PRIMARY KEY (concert_id, section_name)
);

-- ------------------------------------------------
-- 7. 공연별 좌석 (concert_seats)
-- ------------------------------------------------
CREATE TABLE concert_seats
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    concert_id            BIGINT      NOT NULL,
    hall_seat_position_id BIGINT      NOT NULL, -- 물리적 좌석 참조
    section_name          VARCHAR(50) NOT NULL, -- 어느 섹션인지
    seat_status           VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at            TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,

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
    updated_at         TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,

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

    -- PG 응답 정보
    pg_transaction_id VARCHAR(100),
    pg_response_code  VARCHAR(20),

    -- 타임스탬프
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
-- 샘플 데이터 - 최신 설계 반영
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
VALUES (1, '체조경기장', 15000, 20), -- 올림픽공원 체조경기장
       (2, '스카이돔', 25000, 27),  -- 고척 스카이돔
       (3, '실내체육관', 12000, 13);
-- 잠실 실내체육관

-- ------------------------------------------------
-- 3. 홀 좌석 위치 (hall_seat_positions)
-- ------------------------------------------------
-- 올림픽공원 체조경기장 (hall_template_id = 1)
-- VIP 구역 (1~2행, 각 12석) - 샘플
INSERT INTO hall_seat_positions (hall_template_id, row_num, seat_num, x_coord, y_coord)
SELECT 1                   as hall_template_id,
       r.row_num,
       s.seat_num,
       (s.seat_num * 10.0) as x_coord,
       (r.row_num * 15.0)  as y_coord
FROM (SELECT 1 as row_num UNION SELECT 2) r
         CROSS JOIN (SELECT 1 as seat_num
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

-- R 구역 (3~10행, 각 16석) - 샘플 (처음 2행만)
INSERT INTO hall_seat_positions (hall_template_id, row_num, seat_num, x_coord, y_coord)
SELECT 1                   as hall_template_id,
       r.row_num,
       s.seat_num,
       (s.seat_num * 10.0) as x_coord,
       (r.row_num * 15.0)  as y_coord
FROM (SELECT 3 as row_num UNION SELECT 4) r
         CROSS JOIN (SELECT 1 as seat_num
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

-- S 구역 (11~16행, 각 20석) - 샘플 (처음 2행만)
INSERT INTO hall_seat_positions (hall_template_id, row_num, seat_num, x_coord, y_coord)
SELECT 1                   as hall_template_id,
       r.row_num,
       s.seat_num,
       (s.seat_num * 10.0) as x_coord,
       (r.row_num * 15.0)  as y_coord
FROM (SELECT 11 as row_num UNION SELECT 12) r
         CROSS JOIN (SELECT 1 as seat_num
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
                     SELECT 16
                     UNION
                     SELECT 17
                     UNION
                     SELECT 18
                     UNION
                     SELECT 19
                     UNION
                     SELECT 20) s;

-- A 구역 (17~20행, 각 22석) - 샘플 (처음 1행만)
INSERT INTO hall_seat_positions (hall_template_id, row_num, seat_num, x_coord, y_coord)
SELECT 1                   as hall_template_id,
       17                  as row_num,
       s.seat_num,
       (s.seat_num * 10.0) as x_coord,
       (17 * 15.0)         as y_coord
FROM (SELECT 1 as seat_num
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
      SELECT 16
      UNION
      SELECT 17
      UNION
      SELECT 18
      UNION
      SELECT 19
      UNION
      SELECT 20
      UNION
      SELECT 21
      UNION
      SELECT 22) s;

-- ------------------------------------------------
-- 4. 공연 (concerts)
-- ------------------------------------------------
INSERT INTO concerts (hall_template_id, title, artist, description, poster_url,
                      start_date, end_date, booking_start_at, booking_end_at, concert_status, genre,
                      view_count)
VALUES (1, -- 올림픽공원 체조경기장
        'IU Concert: The Golden Hour',
        'IU',
        '아이유의 감성 라이브 콘서트',
        'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=400&h=533&fit=crop',
        '2026-01-20 19:00:00',
        '2026-01-23 22:00:00',
        '2025-12-24 23:59:00', -- 오늘 밤 (타이머 테스트용)
        '2025-12-27 18:00:00',
        'SCHEDULED', 'KPOP', 10), -- 아직 오픈 전이니까 SCHEDULED
       (1, -- 올림픽공원 체조경기장
        'BTS Yet To Come',
        'BTS',
        'BTS 컴백 콘서트',
        'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=400&h=533&fit=crop',
        '2026-01-10 18:00:00',
        '2026-01-13 22:00:00',
        '2025-12-20 20:00:00', -- 이미 지남 (예매중)
        '2025-12-25 17:00:00',
        'BOOKING_OPEN', 'KPOP', 12);
-- 이미 오픈됨

-- ------------------------------------------------
-- 5. 공연별 구역 (concert_sections)
-- @ElementCollection - 공연마다 섹션 정의가 다름
-- ------------------------------------------------
-- IU 콘서트 (concert_id = 1)
INSERT INTO concert_sections (concert_id, section_name, row_start, row_end, price)
VALUES (1, 'VIP', 1, 2, 220000), -- 1~2행: VIP
       (1, 'R', 3, 10, 154000),  -- 3~10행: R석
       (1, 'S', 11, 16, 110000), -- 11~16행: S석
       (1, 'A', 17, 20, 77000);
-- 17~20행: A석

-- BTS 콘서트 (concert_id = 2) - 다른 가격, 다른 구분!
INSERT INTO concert_sections (concert_id, section_name, row_start, row_end, price)
VALUES (2, 'VIP', 1, 4, 300000), -- 1~4행: VIP (더 넓음)
       (2, 'R', 5, 12, 200000),  -- 5~12행: R석
       (2, 'S', 13, 18, 150000), -- 13~18행: S석
       (2, 'A', 19, 20, 100000);
-- 19~20행: A석

-- ------------------------------------------------
-- 6. 공연별 좌석 (concert_seats)
-- hall_seat_position_id 참조 + section_name 매핑
-- ------------------------------------------------
-- IU 콘서트 VIP 좌석 (1~2행, hall_seat_position_id 1~24)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 1           as concert_id,
       hsp.id      as hall_seat_position_id,
       'VIP'       as section_name,
       'AVAILABLE' as seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 1 AND 2;

-- IU 콘서트 R석 좌석 (3~10행) - 샘플 (3~4행만)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 1           as concert_id,
       hsp.id      as hall_seat_position_id,
       'R'         as section_name,
       'AVAILABLE' as seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 3 AND 4;

-- IU 콘서트 S석 좌석 (11~16행) - 샘플 (11~12행만)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 1           as concert_id,
       hsp.id      as hall_seat_position_id,
       'S'         as section_name,
       'AVAILABLE' as seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 11 AND 12;

-- IU 콘서트 A석 좌석 (17~20행) - 샘플 (17행만)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 1           as concert_id,
       hsp.id      as hall_seat_position_id,
       'A'         as section_name,
       'AVAILABLE' as seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num = 17;

-- BTS 콘서트 VIP 좌석 (1~4행) - 1~2행만 샘플
-- 같은 hall_seat_position이지만 다른 concert_id, 다른 section_name!
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 2           as concert_id,
       hsp.id      as hall_seat_position_id,
       'VIP'       as section_name, -- BTS는 1~4행이 VIP
       'AVAILABLE' as seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 1 AND 2;

-- BTS 콘서트 R석 좌석 (5~12행) - 샘플 (3~4행 위치 재사용)
INSERT INTO concert_seats (concert_id, hall_seat_position_id, section_name, seat_status)
SELECT 2           as concert_id,
       hsp.id      as hall_seat_position_id,
       'R'         as section_name, -- BTS는 5~12행이 R석
       'AVAILABLE' as seat_status
FROM hall_seat_positions hsp
WHERE hsp.hall_template_id = 1
  AND hsp.row_num BETWEEN 3 AND 4;
-- 실제로는 5~12행이지만 샘플 데이터로 3~4행 재사용

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
VALUES (2, 1, 220000, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        DATEADD('MINUTE', 15, CURRENT_TIMESTAMP));

-- user2가 IU 콘서트 1행 2번 좌석 예매 (결제 대기)
INSERT INTO reservations (user_id, seat_id, price, reservation_status, reserved_at, expires_at)
VALUES (3, 2, 220000, 'PENDING', CURRENT_TIMESTAMP, DATEADD('MINUTE', 15, CURRENT_TIMESTAMP));

-- 예매한 좌석 상태 업데이트
UPDATE concert_seats
SET seat_status = 'SOLD'
WHERE id = 1;
UPDATE concert_seats
SET seat_status = 'RESERVED'
WHERE id = 2;

-- ------------------------------------------------
-- 9. 결제 (payments) - 샘플
-- ------------------------------------------------
INSERT INTO payments (reservation_id, order_id, amount, payment_method, payment_status,
                      pg_transaction_id, pg_response_code, initiated_at, confirmed_at)
VALUES (1, 'ORDER-2025-001', 220000, 'CARD', 'CONFIRMED',
        'PG-TXN-12345', 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);