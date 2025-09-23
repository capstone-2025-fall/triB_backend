-- 임시 사용자 데이터 삽입 (중복 시 무시)
INSERT INTO users (email, password, nickname, username, user_status, is_alarm, created_at, updated_at)
VALUES ('test1@example.com', 'password123', '김철수', 'kimcs', 'ACTIVE', 'ON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (email, password, nickname, username, user_status, is_alarm, created_at, updated_at)
VALUES ('test2@example.com', 'password456', '이영희', 'leeyh', 'ACTIVE', 'ON', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (email, password, nickname, username, user_status, is_alarm, created_at, updated_at)
VALUES ('test3@example.com', 'password789', '박민수', 'parkms', 'ACTIVE', 'OFF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 임시 룸 데이터 삽입 (중복 시 무시)
INSERT INTO rooms (room_name, destination, start_date, end_date, created_at, updated_at)
VALUES ('도쿄 여행방', '도쿄', '2025-10-01', '2025-10-05', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO rooms (room_name, destination, start_date, end_date, created_at, updated_at)
VALUES ('오사카 여행방', '오사카', '2025-10-10', '2025-10-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 임시 여행 데이터 삽입 (중복 시 무시)
INSERT INTO trips (room_id, destination, trip_status, is_bookmarked, created_at, updated_at)
VALUES (1, '도쿄', 'READY', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO trips (room_id, destination, trip_status, is_bookmarked, created_at, updated_at)
VALUES (2, '오사카', 'ACCEPTED', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
