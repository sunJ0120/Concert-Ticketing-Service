-- ================================================
-- concertRepoistory test data
-- ================================================

-- 1. Building (필수)
INSERT INTO buildings (id, name, address, latitude, longitude)
VALUES (1, '테스트 공연장', '서울시 강남구', 37.5, 127.0);

-- 2. HallTemplate (필수)
INSERT INTO hall_templates (id, building_id, hall_name, total_seats, total_rows)
VALUES (1, 1, '테스트홀', 100, 10);

-- 3. Concert (테스트 대상)
INSERT INTO concerts (id, hall_template_id, title, artist,
                      start_date, end_date, booking_start_at, booking_end_at,
                      concert_status, genre, view_count)
VALUES
-- BOOKING_OPEN 1개
(1, 1, 'IU Concert', 'IU',
 '2025-03-15 19:00:00', '2025-03-15 22:00:00',
 '2025-01-15 20:00:00', '2025-03-15 18:00:00',
 'BOOKING_OPEN', 'KPOP', 0),

-- 다른 상태들 (카운트 검증용)
(2, 1, 'BTS Concert', 'BTS',
 '2025-04-20 18:00:00', '2025-04-20 22:00:00',
 '2025-02-20 20:00:00', '2025-04-20 17:00:00',
 'SCHEDULED', 'KPOP', 1),

(3, 1, '종료된 공연', 'Artist',
 '2024-12-01 18:00:00', '2024-12-01 22:00:00',
 '2024-11-01 20:00:00', '2024-12-01 17:00:00',
 'BOOKING_CLOSED', 'CONCERT', 2);

-- 4. ConcertSections (fetch join 검증용)
INSERT INTO concert_sections (concert_id, section_name, row_start, row_end, price, color)
VALUES (1, 'VIP', 1, 5, 220000, '#FFD700'),
       (1, 'R', 6, 10, 154000, '#FF69B4'),
       (2, 'VIP', 1, 5, 200000, '#FFD700'),
       (2, 'S', 6, 10, 100000, '#87CEEB');