-- 테스트용 멤버 데이터 (테이블명: members, ID: member_id)
INSERT INTO members (member_id, google_id, email, name, profile_img, role, status, created_at, updated_at)
VALUES (1, 'test-google-123', 'test@example.com', '테스트유저', 'https://example.com/profile.jpg', 'USER', 'ACTIVE', NOW(), NOW());

-- 테스트용 자기소개서 데이터 (테이블명: cover_letter, ID: cover_letter_id)
INSERT INTO cover_letter (cover_letter_id, title, content, job_field, experience_years, member_id, status, created_at, updated_at)
VALUES (1, '[원본] 백엔드 개발자 자기소개서',
        '안녕하세요. 백엔드 개발자를 꿈꾸는 지원자입니다. Java와 Spring Boot를 활용한 웹 애플리케이션 개발 경험이 있으며, 특히 REST API 설계와 데이터베이스 최적화에 관심이 많습니다. 이번 기회를 통해 더 나은 개발자로 성장하고 싶습니다.',
        'IT', 3, 1, 'ACTIVE', NOW(), NOW());

INSERT INTO cover_letter (cover_letter_id, title, content, job_field, experience_years, member_id, status, created_at, updated_at)
VALUES (2, '[AI첨삭] 프론트엔드 개발자 자기소개서',
        '프론트엔드 개발에 열정을 가진 지원자입니다. React와 TypeScript를 주로 사용하며, 사용자 경험을 중시하는 개발을 추구합니다. 반응형 웹 디자인과 성능 최적화에 대한 경험이 있습니다.',
        'IT', 2, 1, 'ACTIVE', NOW(), NOW());