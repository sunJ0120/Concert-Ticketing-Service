-- ================================================
-- TICKETBLITZ - MySQL Production Schema
-- Option B (Template Pattern)
-- ================================================

-- ------------------------------------------------
-- 1. 회원 (users)
-- ------------------------------------------------
CREATE TABLE users
(
    id         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255)        NULL COMMENT 'NULL: 소셜 로그인 전용',
    name       VARCHAR(100)        NOT NULL,
    phone      VARCHAR(20)         NULL,
    role       VARCHAR(20)         NOT NULL DEFAULT 'USER',
    created_at DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='회원 정보';

-- ------------------------------------------------
-- 1-1. 소셜 계정 연동 (social_accounts)
-- ------------------------------------------------
CREATE TABLE social_accounts
(
    id          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT UNSIGNED NOT NULL,
    provider    VARCHAR(20)     NOT NULL COMMENT 'KAKAO, NAVER, GOOGLE',
    provider_id VARCHAR(255)    NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE KEY uk_provider_account (provider, provider_id),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='소셜 로그인 연동';

-- ------------------------------------------------
-- 2. 공연장 템플릿 (section_templates)
-- ------------------------------------------------
CREATE TABLE section_templates
(
    id            BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    venue_name    VARCHAR(100) NOT NULL,
    section_name  VARCHAR(50)  NOT NULL,
    row_count     INT UNSIGNED NOT NULL DEFAULT 0,
    seats_per_row INT UNSIGNED NOT NULL DEFAULT 0,
    color         VARCHAR(7)   NULL COMMENT 'Hex color code',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_venue_section (venue_name, section_name),
    INDEX idx_venue_name (venue_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='공연장 구역 템플릿 (재사용)';

-- ------------------------------------------------
-- 3. 공연 (concerts) - Venue Embedded
-- ------------------------------------------------
CREATE TABLE concerts
(
    id               BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    title            VARCHAR(300) NOT NULL,
    artist           VARCHAR(200) NULL,
    description      TEXT         NULL,
    poster_url       VARCHAR(500) NULL,

    -- Venue (Embeddable)
    venue_name       VARCHAR(100) NOT NULL COMMENT 'Embedded: 공연장 이름',
    venue_address    VARCHAR(300) NOT NULL COMMENT 'Embedded: 공연장 주소',
    venue_capacity   INT UNSIGNED NULL COMMENT 'Embedded: 수용 인원',

    -- 공연 일정
    start_date       DATETIME     NOT NULL,
    end_date         DATETIME     NOT NULL,

    -- 예매 기간
    booking_start_at DATETIME     NOT NULL,
    booking_end_at   DATETIME     NOT NULL,

    -- 공연 상태
    status           VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED, OPEN, BOOKING, CLOSED, CANCELLED',

    -- 메타데이터
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_concert_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_booking_dates CHECK (booking_end_at >= booking_start_at),

    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_venue_name (venue_name),
    INDEX idx_booking_dates (booking_start_at, booking_end_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='공연 정보 (Venue Embedded)';

-- ------------------------------------------------
-- 4. 공연별 구역 (concert_sections)
-- ------------------------------------------------
CREATE TABLE concert_sections
(
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    concert_id   BIGINT UNSIGNED NOT NULL,
    template_id  BIGINT UNSIGNED NOT NULL,
    price        DECIMAL(10, 0)  NOT NULL,
    is_available BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (concert_id) REFERENCES concerts (id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES section_templates (id),
    UNIQUE KEY uk_concert_template (concert_id, template_id),
    CONSTRAINT chk_price_positive CHECK (price >= 0),

    INDEX idx_concert_id (concert_id),
    INDEX idx_template_id (template_id),
    INDEX idx_concert_available (concert_id, is_available)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='공연별 구역 (Concert + Template)';

-- ------------------------------------------------
-- 5. 공연별 좌석 (concert_seats)
-- ------------------------------------------------
CREATE TABLE concert_seats
(
    id         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    section_id BIGINT UNSIGNED NOT NULL,
    row_num    INT UNSIGNED    NOT NULL,
    seat_num   INT UNSIGNED    NOT NULL,
    seat_label VARCHAR(20)     NULL,
    status     VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE, RESERVED, SOLD, UNAVAILABLE',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (section_id) REFERENCES concert_sections (id) ON DELETE CASCADE,
    UNIQUE KEY uk_seat_position (section_id, row_num, seat_num),
    CONSTRAINT chk_row_positive CHECK (row_num > 0),
    CONSTRAINT chk_seat_positive CHECK (seat_num > 0),

    INDEX idx_section_id (section_id),
    INDEX idx_section_status (section_id, status),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='공연별 좌석';

-- ------------------------------------------------
-- 6. 예매 (reservations)
-- ------------------------------------------------
CREATE TABLE reservations
(
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT UNSIGNED NOT NULL,
    seat_id      BIGINT UNSIGNED NOT NULL,
    price        DECIMAL(10, 0)  NOT NULL,
    status       VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, CONFIRMED, CANCELLED, EXPIRED',
    reserved_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at DATETIME        NULL,
    cancelled_at DATETIME        NULL,
    expires_at   DATETIME        NULL,
    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (seat_id) REFERENCES concert_seats (id),
    CONSTRAINT chk_price_positive CHECK (price >= 0),

    INDEX idx_user_id (user_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='좌석 예매';

-- ------------------------------------------------
-- 7. 결제 (payments)
-- ------------------------------------------------
CREATE TABLE payments
(
    id                BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    reservation_id    BIGINT UNSIGNED NOT NULL,
    order_id          VARCHAR(50)     NOT NULL UNIQUE,

    amount            DECIMAL(10, 0)  NOT NULL,
    payment_method    VARCHAR(20)     NOT NULL COMMENT 'CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT',
    status            VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, CONFIRMED, FAILED, REFUNDED',

    -- PG 응답 정보
    pg_transaction_id VARCHAR(100)    NULL COMMENT '다날 transactionId',
    pg_response_code  VARCHAR(20)     NULL COMMENT '다날 code (SUCCESS/FAIL)',

    -- 타임스탬프
    initiated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at      DATETIME        NULL,
    refunded_at       DATETIME        NULL,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE RESTRICT,
    CONSTRAINT chk_amount_positive CHECK (amount > 0),

    INDEX idx_reservation_id (reservation_id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_pg_transaction_id (pg_transaction_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='결제 정보';

-- ------------------------------------------------
-- 7-1. 결제 로그 (payment_logs)
-- ------------------------------------------------
CREATE TABLE payment_logs
(
    id                  BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    payment_id          BIGINT UNSIGNED NOT NULL,
    event_type          VARCHAR(20)     NOT NULL COMMENT 'INITIATED, CONFIRMED, FAILED, REFUNDED',
    old_status          VARCHAR(20)     NULL,
    new_status          VARCHAR(20)     NULL,

    -- PG 상세 응답
    pg_response_code    VARCHAR(20)     NULL,
    pg_response_message TEXT            NULL,
    pg_raw_response     TEXT            NULL COMMENT '원본 JSON',

    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE,

    INDEX idx_payment_id (payment_id),
    INDEX idx_event_type (event_type),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='결제 이벤트 로그 (감사 추적)';

-- ------------------------------------------------
-- 8. 대기열 (waiting_queue)
-- ------------------------------------------------
CREATE TABLE waiting_queue
(
    id           BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT UNSIGNED NOT NULL,
    concert_id   BIGINT UNSIGNED NOT NULL,

    queue_token  VARCHAR(100)    NOT NULL UNIQUE,
    position     INT UNSIGNED    NOT NULL,
    status       VARCHAR(20)     NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING, ACTIVE, EXPIRED',

    entered_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activated_at DATETIME        NULL,
    expires_at   DATETIME        NULL,

    created_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (concert_id) REFERENCES concerts (id) ON DELETE CASCADE,
    CONSTRAINT chk_position_positive CHECK (position > 0),

    INDEX idx_user_id (user_id),
    INDEX idx_concert_id (concert_id),
    INDEX idx_queue_token (queue_token),
    INDEX idx_concert_status (concert_id, status),
    INDEX idx_expires_at (expires_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
    COMMENT ='대기열 (Redis로 이전 예정)';


-- ================================================
-- 프로덕션 초기 데이터 (필수 템플릿만)
-- ================================================

-- 공연장 템플릿 (실제 공연장 데이터)
INSERT INTO section_templates (venue_name, section_name, row_count, seats_per_row, color)
VALUES
    -- 올림픽공원 체조경기장
    ('올림픽공원 체조경기장', 'VIP', 2, 12, '#FFD700'),
    ('올림픽공원 체조경기장', 'R', 8, 16, '#E066FF'),
    ('올림픽공원 체조경기장', 'S', 6, 20, '#00BFFF'),
    ('올림픽공원 체조경기장', 'A', 4, 22, '#7CFC00');

-- 추가 공연장은 어드민 페이지에서 등록