-- 5천명 회원 대량 테스트 데이터 생성
-- 인덱스 성능 테스트용

-- 기존 테스트 데이터는 그대로 두고 추가
-- member_id 2부터 5001까지 생성

-- ACTIVE USER들 (3,760명: id 2~3761)
INSERT INTO members (member_id, google_id, email, name, profile_img, role, status, created_at, updated_at)
SELECT
    n + 1 as member_id,
    CONCAT('google-user-', n + 1) as google_id,
    CONCAT('user', n + 1, '@example.com') as email,
    CONCAT('사용자', n + 1) as name,
    CONCAT('https://example.com/profile', n + 1, '.jpg') as profile_img,
    'USER' as role,
    'ACTIVE' as status,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 30), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 3760) as t(n);

-- INACTIVE USER들 (752명: id 3762~4513)
INSERT INTO members (member_id, google_id, email, name, profile_img, role, status, created_at, updated_at)
SELECT
    n + 3761 as member_id,
    CONCAT('google-user-', n + 3761) as google_id,
    CONCAT('user', n + 3761, '@example.com') as email,
    CONCAT('비활성사용자', n) as name,
    CONCAT('https://example.com/profile', n + 3761, '.jpg') as profile_img,
    'USER' as role,
    'INACTIVE' as status,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 90), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 752) as t(n);

-- DELETED USER들 (188명: id 4514~4701)
INSERT INTO members (member_id, google_id, email, name, profile_img, role, status, created_at, updated_at)
SELECT
    n + 4513 as member_id,
    CONCAT('google-user-', n + 4513) as google_id,
    CONCAT('deleted', n, '@example.com') as email,
    CONCAT('삭제된사용자', n) as name,
    CONCAT('https://example.com/profile', n + 4513, '.jpg') as profile_img,
    'USER' as role,
    'DELETED' as status,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 180), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 188) as t(n);

-- ACTIVE ADMIN들 (238명: id 4702~4939)
INSERT INTO members (member_id, google_id, email, name, profile_img, role, status, created_at, updated_at)
SELECT
    n + 4701 as member_id,
    CONCAT('google-admin-', n + 4701) as google_id,
    CONCAT('admin', n, '@company.com') as email,
    CONCAT('관리자', n) as name,
    CONCAT('https://example.com/admin', n, '.jpg') as profile_img,
    'ADMIN' as role,
    'ACTIVE' as status,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 7), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 238) as t(n);

-- INACTIVE ADMIN들 (12명: id 4940~4951)
INSERT INTO members (member_id, google_id, email, name, profile_img, role, status, created_at, updated_at)
SELECT
    n + 4939 as member_id,
    CONCAT('google-admin-', n + 4939) as google_id,
    CONCAT('inactive-admin', n, '@company.com') as email,
    CONCAT('비활성관리자', n) as name,
    CONCAT('https://example.com/admin', n + 4939, '.jpg') as profile_img,
    'ADMIN' as role,
    'INACTIVE' as status,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 60), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 12) as t(n);

-- ACTIVE SUPER_ADMIN들 (48명: id 4952~4999)
INSERT INTO members (member_id, google_id, email, name, profile_img, role, status, created_at, updated_at)
SELECT
    n + 4951 as member_id,
    CONCAT('google-super-', n + 4951) as google_id,
    CONCAT('super', n, '@company.com') as email,
    CONCAT('슈퍼관리자', n) as name,
    CONCAT('https://example.com/super', n, '.jpg') as profile_img,
    'SUPER_ADMIN' as role,
    'ACTIVE' as status,
    DATEADD('DAY', -FLOOR(RAND() * 180), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 3), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 48) as t(n);

-- INACTIVE SUPER_ADMIN들 (2명: id 5000~5001)
INSERT INTO members (member_id, google_id, email, name, profile_img, role, status, created_at, updated_at)
SELECT
    n + 4999 as member_id,
    CONCAT('google-super-', n + 4999) as google_id,
    CONCAT('inactive-super', n, '@company.com') as email,
    CONCAT('비활성슈퍼관리자', n) as name,
    CONCAT('https://example.com/super', n + 4999, '.jpg') as profile_img,
    'SUPER_ADMIN' as role,
    'INACTIVE' as status,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 120), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 2) as t(n);

-- 데이터 확인용 쿼리 (주석)
-- SELECT status, COUNT(*) FROM members GROUP BY status;
-- SELECT role, COUNT(*) FROM members GROUP BY role;
-- SELECT COUNT(*) as total_members FROM members;