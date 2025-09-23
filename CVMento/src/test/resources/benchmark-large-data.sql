-- 벤치마크용 대량 데이터 생성 (H2 동적 생성)
-- ENUM 값들과 정확히 일치하도록 수정
-- 자소서: 5천개, 이력서: 2500개

-- 1. 자소서 데이터 5천개 생성 (member_id 1~5000 기준)
-- CoverLetterStatus: ACTIVE, DELETED
INSERT INTO cover_letter (title, content, job_field, experience_years, member_id, status, created_at, updated_at)
SELECT
    CONCAT('자기소개서 ', n) as title,
    CONCAT('안녕하세요. ',
           CASE WHEN MOD(n, 5) = 0 THEN '백엔드'
                WHEN MOD(n, 5) = 1 THEN '프론트엔드'
                WHEN MOD(n, 5) = 2 THEN '풀스택'
                WHEN MOD(n, 5) = 3 THEN 'DevOps'
                ELSE 'AI' END,
           ' 개발자를 꿈꾸는 지원자입니다. 열정적으로 개발에 임하고 있으며, 지속적인 학습을 통해 성장하고 있습니다. ',
           '다양한 프로젝트 경험을 바탕으로 실무에 바로 적용할 수 있는 역량을 갖추었습니다. ',
           '팀워크를 중시하며, 사용자 중심의 서비스 개발에 관심이 많습니다. ',
           '새로운 기술에 대한 호기심과 도전 정신으로 더 나은 개발자가 되고 싶습니다. 자소서 번호: ', n) as content,
    'IT' as job_field,
    (MOD(n, 5) + 1) as experience_years,
    CASE
        WHEN n <= 5000 THEN n
        ELSE 1
        END as member_id,
    CASE WHEN MOD(n, 10) = 0 THEN 'DELETED' ELSE 'ACTIVE' END as status,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 30), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 5000) as t(n);

-- 2. 이력서 데이터 2500개 생성 (member_id 1~2500 기준)
-- ResumeType: DEFAULT, MODERN
-- CareerType: FRESHMAN, EXPERIENCED (NEW_GRADUATE, CAREER_CHANGE 제거)
-- ResumeStatus: ACTIVE, DELETED
INSERT INTO resume (title, type, name, email, birth_year, phone, career_type, desired_position, member_id, status, created_at, updated_at)
SELECT
    CONCAT('이력서 ', n) as title,
    CASE WHEN MOD(n, 2) = 0 THEN 'DEFAULT' ELSE 'MODERN' END as type,
    CONCAT('개발자', n) as name,
    CONCAT('dev', n, '@example.com') as email,
    (1990 + MOD(n, 15)) as birth_year,
    CONCAT('010-', LPAD(MOD(n, 9999), 4, '0'), '-', LPAD(MOD(n*7, 9999), 4, '0')) as phone,
    CASE WHEN MOD(n, 2) = 0 THEN 'FRESHMAN' ELSE 'EXPERIENCED' END as career_type,
    CASE WHEN MOD(n, 4) = 0 THEN '백엔드 개발자'
         WHEN MOD(n, 4) = 1 THEN '프론트엔드 개발자'
         WHEN MOD(n, 4) = 2 THEN '풀스택 개발자'
         ELSE 'DevOps 엔지니어' END as desired_position,
    n as member_id,
    CASE WHEN MOD(n, 8) = 0 THEN 'DELETED' ELSE 'ACTIVE' END as status,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 60), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 2500) as t(n);

-- 데이터 확인용 쿼리 (주석)
-- SELECT COUNT(*) as total_coverletters FROM cover_letter;
-- SELECT COUNT(*) as total_resumes FROM resume;
-- SELECT status, COUNT(*) FROM cover_letter GROUP BY status;
-- SELECT status, COUNT(*) FROM resume GROUP BY status;
-- SELECT career_type, COUNT(*) FROM resume GROUP BY career_type;
-- SELECT resume_type, COUNT(*) FROM resume GROUP BY resume_type;