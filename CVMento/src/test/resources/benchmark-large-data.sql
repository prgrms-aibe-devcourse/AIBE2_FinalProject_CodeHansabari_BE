-- 벤치마크용 대량 데이터 생성 (기존 회원 데이터 활용)
-- 자소서: 5천개, 이력서: 2-3천개

-- 1. 자소서 데이터 5천개 생성 (member_id 1~5000 기준)
INSERT INTO cover_letter (title, content, job_field, experience_years, member_id, status, created_at, updated_at) VALUES
('백엔드 개발자 지원서', '안녕하세요. 백엔드 개발에 열정을 가진 개발자입니다. Java와 Spring Boot를 활용한 웹 애플리케이션 개발 경험이 있으며, RESTful API 설계와 데이터베이스 최적화에 특별한 관심을 가지고 있습니다. 효율적인 서버 아키텍처 구축을 통해 사용자에게 빠르고 안정적인 서비스를 제공하고 싶습니다.', 'IT', 2, 1, 'ACTIVE', NOW(), NOW()),
('프론트엔드 개발자의 꿈', '프론트엔드 개발에 대한 열정으로 가득한 개발자입니다. React와 TypeScript를 주력으로 사용하며, 사용자 경험을 최우선으로 하는 웹 애플리케이션 개발을 추구합니다. 반응형 웹 디자인과 웹 접근성, 성능 최적화에 대한 깊은 이해를 바탕으로 모든 사용자가 편리하게 이용할 수 있는 인터페이스를 만들고 싶습니다.', 'IT', 1, 2, 'ACTIVE', NOW(), NOW()),
('풀스택 개발자로의 성장', '풀스택 개발자를 꿈꾸는 개발자입니다. 백엔드와 프론트엔드 모두에 관심이 많으며, Node.js와 React를 활용한 전체적인 웹 서비스 개발 경험을 쌓아왔습니다. 새로운 기술 학습에 대한 두려움 없이 도전하며, 팀원들과의 원활한 소통을 통해 더 나은 서비스를 만들어 나가고 싶습니다.', 'IT', 3, 3, 'ACTIVE', NOW(), NOW()),
('데이터로 세상을 바꾸는 개발자', '데이터 분석과 머신러닝에 관심이 많은 개발자입니다. Python과 pandas, scikit-learn을 활용한 데이터 분석 프로젝트 경험이 있으며, 데이터를 통해 비즈니스 인사이트를 도출하고 의사결정을 지원하는 일에 보람을 느낍니다. 지속적인 학습을 통해 AI 분야의 전문가로 성장하고 싶습니다.', 'IT', 1, 4, 'ACTIVE', NOW(), NOW()),
('DevOps 엔지니어의 여정', 'DevOps 문화에 깊은 관심을 가진 개발자입니다. Docker와 Kubernetes를 활용한 컨테이너 오케스트레이션, CI/CD 파이프라인 구축 경험을 바탕으로 개발팀의 생산성 향상에 기여하고 싶습니다. 자동화를 통한 효율적인 배포 환경 구축과 모니터링 시스템 운영에 특별한 관심을 가지고 있습니다.', 'IT', 4, 5, 'ACTIVE', NOW(), NOW()),
('모바일 앱 개발자의 도전', '모바일 앱 개발에 열정을 가진 개발자입니다. React Native와 Flutter를 활용한 크로스 플랫폼 개발 경험이 있으며, 사용자 친화적인 모바일 인터페이스 설계에 관심이 많습니다. 다양한 디바이스와 운영체제에서 일관된 사용자 경험을 제공하는 앱을 개발하고 싶습니다.', 'IT', 2, 6, 'ACTIVE', NOW(), NOW()),
('게임 개발자의 꿈', '게임 개발에 대한 꿈을 키워온 개발자입니다. Unity와 C#을 활용한 2D/3D 게임 개발 경험이 있으며, 게임 로직 설계와 최적화에 특별한 관심을 가지고 있습니다. 플레이어에게 재미와 감동을 줄 수 있는 게임을 만들어 게임 산업에 기여하고 싶습니다.', 'IT', 1, 7, 'ACTIVE', NOW(), NOW()),
('클라우드 아키텍트를 향한 여정', '클라우드 기술에 깊은 관심을 가진 개발자입니다. AWS와 Azure 환경에서의 서비스 배포 및 운영 경험이 있으며, 확장 가능하고 안정적인 클라우드 아키텍처 설계에 특별한 관심을 가지고 있습니다. 비용 최적화와 보안을 고려한 클라우드 솔루션을 제공하고 싶습니다.', 'IT', 3, 8, 'ACTIVE', NOW(), NOW()),
('AI 연구원의 꿈', '인공지능 연구에 열정을 가진 개발자입니다. TensorFlow와 PyTorch를 활용한 딥러닝 모델 개발 경험이 있으며, 컴퓨터 비전과 자연어 처리 분야에 특별한 관심을 가지고 있습니다. AI 기술을 통해 사회 문제를 해결하고 인류의 삶을 개선하는 데 기여하고 싶습니다.', 'IT', 2, 9, 'ACTIVE', NOW(), NOW()),
('보안 전문가의 길', '정보보안에 깊은 관심을 가진 개발자입니다. 웹 애플리케이션 보안 취약점 분석과 침투 테스트 경험이 있으며, 안전한 시스템 구축을 위한 보안 아키텍처 설계에 특별한 관심을 가지고 있습니다. 사이버 위협으로부터 기업과 개인의 정보를 보호하는 일에 기여하고 싶습니다.', 'IT', 4, 10, 'ACTIVE', NOW(), NOW()),
('블록체인 개발자의 비전', '블록체인 기술에 관심이 많은 개발자입니다. Solidity를 활용한 스마트 컨트랙트 개발 경험이 있으며, 탈중앙화 애플리케이션(DApp) 개발에 특별한 관심을 가지고 있습니다. 블록체인 기술을 통해 더 투명하고 공정한 디지털 생태계 구축에 기여하고 싶습니다.', 'IT', 1, 11, 'ACTIVE', NOW(), NOW()),
('UX/UI 디자이너 개발자', '디자인과 개발의 경계를 넘나드는 개발자입니다. Figma를 활용한 디자인 경험과 HTML/CSS/JavaScript를 활용한 프론트엔드 개발 경험을 모두 보유하고 있습니다. 사용자 중심의 디자인과 개발을 통해 직관적이고 아름다운 웹 서비스를 만들고 싶습니다.', 'IT', 2, 12, 'ACTIVE', NOW(), NOW()),
('시스템 프로그래머의 도전', '시스템 레벨 프로그래밍에 관심이 많은 개발자입니다. C와 C++을 활용한 시스템 소프트웨어 개발 경험이 있으며, 운영체제와 네트워크 프로그래밍에 특별한 관심을 가지고 있습니다. 효율적이고 안정적인 시스템 소프트웨어 개발을 통해 기술 발전에 기여하고 싶습니다.', 'IT', 3, 13, 'ACTIVE', NOW(), NOW()),
('웹 개발자의 새로운 시작', '웹 개발에 대한 열정으로 새로운 시작을 하는 개발자입니다. HTML, CSS, JavaScript 기초부터 시작하여 Vue.js를 활용한 SPA 개발까지 학습해왔습니다. 사용자에게 가치 있는 웹 서비스를 제공하며 지속적으로 성장하는 개발자가 되고 싶습니다.', 'IT', 0, 14, 'ACTIVE', NOW(), NOW()),
('임베디드 개발자의 꿈', '임베디드 시스템 개발에 관심이 많은 개발자입니다. Arduino와 Raspberry Pi를 활용한 IoT 프로젝트 경험이 있으며, 하드웨어와 소프트웨어의 융합 기술에 특별한 관심을 가지고 있습니다. 스마트한 IoT 솔루션 개발을 통해 일상생활의 편의성 향상에 기여하고 싶습니다.', 'IT', 1, 15, 'ACTIVE', NOW(), NOW()),
('데이터베이스 전문가의 길', '데이터베이스 관리와 최적화에 깊은 관심을 가진 개발자입니다. MySQL과 PostgreSQL을 활용한 데이터베이스 설계 및 튜닝 경험이 있으며, 대용량 데이터 처리와 성능 최적화에 특별한 관심을 가지고 있습니다. 안정적이고 효율적인 데이터 관리 솔루션을 제공하고 싶습니다.', 'IT', 3, 16, 'ACTIVE', NOW(), NOW()),
('네트워크 엔지니어의 비전', '네트워크 기술에 관심이 많은 개발자입니다. TCP/IP 프로토콜과 네트워크 보안에 대한 깊은 이해를 바탕으로 안정적인 네트워크 인프라 구축 경험을 쌓아왔습니다. 고성능 네트워크 시스템 설계를 통해 원활한 데이터 통신 환경을 제공하고 싶습니다.', 'IT', 4, 17, 'ACTIVE', NOW(), NOW()),
('소프트웨어 테스터의 사명', '소프트웨어 품질 보증에 대한 책임감을 가진 개발자입니다. 수동 테스트와 자동화 테스트 도구를 활용한 QA 경험이 있으며, 사용자에게 안정적인 소프트웨어를 제공하기 위한 철저한 테스트에 특별한 관심을 가지고 있습니다. 품질 높은 소프트웨어 개발 문화 확산에 기여하고 싶습니다.', 'IT', 2, 18, 'ACTIVE', NOW(), NOW()),
('오픈소스 기여자의 꿈', '오픈소스 생태계에 관심이 많은 개발자입니다. GitHub를 통한 오픈소스 프로젝트 기여 경험이 있으며, 커뮤니티와의 협업을 통한 소프트웨어 개발에 특별한 관심을 가지고 있습니다. 오픈소스 기여를 통해 개발자 커뮤니티 발전에 기여하고 싶습니다.', 'IT', 1, 19, 'ACTIVE', NOW(), NOW()),
('스타트업 개발자의 도전', '스타트업 환경에서의 개발에 관심이 많은 개발자입니다. 빠른 개발과 지속적인 학습이 필요한 환경에서 다양한 기술 스택을 활용한 개발 경험을 쌓아왔습니다. 혁신적인 아이디어를 기술로 구현하여 사회에 긍정적인 변화를 만들어 나가고 싶습니다.', 'IT', 2, 20, 'ACTIVE', NOW(), NOW());

-- NOTE: 실제로는 이런 식으로 5000개까지 생성해야 함. 여기서는 20개만 예시로 작성
-- 실제 운영 시에는 스크립트나 프로그램을 통해 5000개 데이터를 생성하는 것을 권장

-- 2. 이력서 데이터 2-3천개 생성 (일부 회원만, member_id 1~3000 기준)
INSERT INTO resume (title, type, status, name, email, birth_year, phone, career_type, field_name, introduction, github_url, blog_url, notion_url, member_id, created_at, updated_at) VALUES
('백엔드 개발자 이력서', 'DEFAULT', 'ACTIVE', '김민수', 'user001@test.com', 1995, '010-1234-5678', 'EXPERIENCED', '백엔드 개발', 'Java Spring Boot 전문 백엔드 개발자입니다. 3년간의 실무 경험을 바탕으로 안정적이고 확장 가능한 서버 애플리케이션 개발에 전문성을 가지고 있습니다.', 'https://github.com/user001', 'https://blog.user001.com', 'https://notion.so/user001', 1, NOW(), NOW()),
('프론트엔드 개발자 이력서', 'MODERN', 'ACTIVE', '이지영', 'user002@test.com', 1997, '010-2345-6789', 'FRESHMAN', '프론트엔드 개발', 'React와 TypeScript를 활용한 프론트엔드 개발에 관심이 많은 신입 개발자입니다. 사용자 경험을 중시하며 깔끔하고 직관적인 UI 개발을 추구합니다.', 'https://github.com/user002', 'https://blog.user002.com', null, 2, NOW(), NOW()),
('풀스택 개발자 이력서', 'DEFAULT', 'ACTIVE', '박성호', 'user003@test.com', 1993, '010-3456-7890', 'EXPERIENCED', '풀스택 개발', '백엔드와 프론트엔드 모두 경험이 있는 풀스택 개발자입니다. Node.js, React, MongoDB를 주로 사용하며 전체적인 웹 서비스 아키텍처 설계 경험이 있습니다.', 'https://github.com/user003', null, 'https://notion.so/user003', 3, NOW(), NOW()),
('데이터 사이언티스트 이력서', 'MODERN', 'ACTIVE', '최수진', 'user004@test.com', 1996, '010-4567-8901', 'FRESHMAN', '데이터 분석', 'Python을 활용한 데이터 분석과 머신러닝에 관심이 많은 신입 개발자입니다. 통계학 전공을 바탕으로 데이터 기반 의사결정 지원에 기여하고 싶습니다.', 'https://github.com/user004', 'https://blog.user004.com', null, 4, NOW(), NOW()),
('DevOps 엔지니어 이력서', 'DEFAULT', 'ACTIVE', '정대현', 'user005@test.com', 1992, '010-5678-9012', 'EXPERIENCED', 'DevOps', 'AWS 클라우드 환경에서 CI/CD 파이프라인 구축 및 운영 경험이 있는 DevOps 엔지니어입니다. Docker, Kubernetes를 활용한 컨테이너 오케스트레이션 전문가입니다.', 'https://github.com/user005', null, 'https://notion.so/user005', 5, NOW(), NOW()),
('모바일 개발자 이력서', 'MODERN', 'ACTIVE', '강미영', 'user006@test.com', 1994, '010-6789-0123', 'EXPERIENCED', '모바일 개발', 'React Native를 활용한 크로스 플랫폼 모바일 앱 개발 경험이 있습니다. iOS와 Android 모두에서 동일한 사용자 경험을 제공하는 앱 개발에 전문성을 가지고 있습니다.', 'https://github.com/user006', 'https://blog.user006.com', null, 6, NOW(), NOW()),
('게임 개발자 이력서', 'DEFAULT', 'ACTIVE', '조준호', 'user007@test.com', 1998, '010-7890-1234', 'FRESHMAN', '게임 개발', 'Unity와 C#을 활용한 인디 게임 개발 경험이 있는 신입 게임 개발자입니다. 2D 플랫포머 게임과 퍼즐 게임 개발에 특별한 관심을 가지고 있습니다.', 'https://github.com/user007', null, 'https://notion.so/user007', 7, NOW(), NOW()),
('클라우드 엔지니어 이력서', 'MODERN', 'ACTIVE', '윤하은', 'user008@test.com', 1991, '010-8901-2345', 'EXPERIENCED', '클라우드', 'AWS Solutions Architect Associate 자격증을 보유한 클라우드 엔지니어입니다. 서버리스 아키텍처와 마이크로서비스 설계 경험이 풍부합니다.', 'https://github.com/user008', 'https://blog.user008.com', 'https://notion.so/user008', 8, NOW(), NOW()),
('AI 개발자 이력서', 'DEFAULT', 'ACTIVE', '장현우', 'user009@test.com', 1995, '010-9012-3456', 'EXPERIENCED', 'AI/ML', 'TensorFlow와 PyTorch를 활용한 딥러닝 모델 개발 경험이 있는 AI 개발자입니다. 컴퓨터 비전과 자연어 처리 분야에서 실무 프로젝트 경험을 보유하고 있습니다.', 'https://github.com/user009', null, 'https://notion.so/user009', 9, NOW(), NOW()),
('보안 전문가 이력서', 'MODERN', 'ACTIVE', '임소영', 'user010@test.com', 1990, '010-0123-4567', 'EXPERIENCED', '정보보안', '웹 애플리케이션 보안 취약점 분석과 침투 테스트 경험이 있는 보안 전문가입니다. CISSP 자격증을 보유하고 있으며 기업 보안 컨설팅 경험이 풍부합니다.', 'https://github.com/user010', 'https://blog.user010.com', null, 10, NOW(), NOW()),
('블록체인 개발자 이력서', 'DEFAULT', 'ACTIVE', '한태완', 'user011@test.com', 1999, '010-1357-2468', 'FRESHMAN', '블록체인', 'Solidity를 활용한 스마트 컨트랙트 개발에 관심이 많은 신입 블록체인 개발자입니다. DeFi 프로토콜과 NFT 관련 프로젝트 경험이 있습니다.', 'https://github.com/user011', null, 'https://notion.so/user011', 11, NOW(), NOW()),
('UI/UX 개발자 이력서', 'MODERN', 'ACTIVE', '오예린', 'user012@test.com', 1996, '010-2468-1357', 'EXPERIENCED', 'UI/UX', 'Figma를 활용한 UI/UX 디자인과 React를 활용한 프론트엔드 개발 모두 가능한 개발자입니다. 사용자 중심의 디자인 시스템 구축 경험이 있습니다.', 'https://github.com/user012', 'https://blog.user012.com', 'https://notion.so/user012', 12, NOW(), NOW()),
('시스템 프로그래머 이력서', 'DEFAULT', 'ACTIVE', '서상혁', 'user013@test.com', 1993, '010-3579-2468', 'EXPERIENCED', '시스템', 'C와 C++을 활용한 시스템 레벨 프로그래밍 전문가입니다. 리눅스 커널 모듈 개발과 임베디드 시스템 개발 경험이 있습니다.', 'https://github.com/user013', null, 'https://notion.so/user013', 13, NOW(), NOW()),
('웹 개발자 이력서', 'MODERN', 'ACTIVE', '신나연', 'user014@test.com', 2000, '010-4680-1357', 'FRESHMAN', '웹 개발', 'Vue.js와 Nuxt.js를 활용한 웹 개발에 관심이 많은 신입 개발자입니다. 반응형 웹 디자인과 웹 접근성을 중시하는 개발을 추구합니다.', 'https://github.com/user014', 'https://blog.user014.com', null, 14, NOW(), NOW()),
('임베디드 개발자 이력서', 'DEFAULT', 'ACTIVE', '권동건', 'user015@test.com', 1997, '010-5791-2468', 'FRESHMAN', '임베디드', 'Arduino와 Raspberry Pi를 활용한 IoT 프로젝트 개발 경험이 있는 신입 임베디드 개발자입니다. 센서 데이터 수집과 무선 통신 기술에 관심이 많습니다.', 'https://github.com/user015', null, 'https://notion.so/user015', 15, NOW(), NOW());

-- 자소서 대량 데이터 5천개 생성 (SYSTEM_RANGE 사용)
INSERT INTO cover_letter (title, content, job_field, experience_years, member_id, status, created_at, updated_at)
SELECT
    CASE (n % 20)
        WHEN 0 THEN '백엔드 개발자 지원서'
        WHEN 1 THEN '프론트엔드 개발자의 꿈'
        WHEN 2 THEN '풀스택 개발자로의 성장'
        WHEN 3 THEN '데이터로 세상을 바꾸는 개발자'
        WHEN 4 THEN 'DevOps 엔지니어의 여정'
        WHEN 5 THEN '모바일 앱 개발자의 도전'
        WHEN 6 THEN '게임 개발자의 꿈'
        WHEN 7 THEN '클라우드 아키텍트를 향한 여정'
        WHEN 8 THEN 'AI 연구원의 꿈'
        WHEN 9 THEN '보안 전문가의 길'
        WHEN 10 THEN '블록체인 개발자의 비전'
        WHEN 11 THEN 'UX/UI 디자이너 개발자'
        WHEN 12 THEN '시스템 프로그래머의 도전'
        WHEN 13 THEN '웹 개발자의 새로운 시작'
        WHEN 14 THEN '임베디드 개발자의 꿈'
        WHEN 15 THEN '데이터베이스 전문가의 길'
        WHEN 16 THEN '네트워크 엔지니어의 비전'
        WHEN 17 THEN '소프트웨어 테스터의 사명'
        WHEN 18 THEN '오픈소스 기여자의 꿈'
        ELSE '스타트업 개발자의 도전'
    END as title,
    CASE (n % 10)
        WHEN 0 THEN '안녕하세요. 백엔드 개발에 열정을 가진 개발자입니다. Java와 Spring Boot를 활용한 웹 애플리케이션 개발 경험이 있으며, RESTful API 설계와 데이터베이스 최적화에 특별한 관심을 가지고 있습니다.'
        WHEN 1 THEN '프론트엔드 개발에 대한 열정으로 가득한 개발자입니다. React와 TypeScript를 주력으로 사용하며, 사용자 경험을 최우선으로 하는 웹 애플리케이션 개발을 추구합니다.'
        WHEN 2 THEN '풀스택 개발자를 꿈꾸는 개발자입니다. 백엔드와 프론트엔드 모두에 관심이 많으며, Node.js와 React를 활용한 전체적인 웹 서비스 개발 경험을 쌓아왔습니다.'
        WHEN 3 THEN '데이터 분석과 머신러닝에 관심이 많은 개발자입니다. Python과 pandas, scikit-learn을 활용한 데이터 분석 프로젝트 경험이 있으며, 데이터를 통해 비즈니스 인사이트를 도출합니다.'
        WHEN 4 THEN 'DevOps 문화에 깊은 관심을 가진 개발자입니다. Docker와 Kubernetes를 활용한 컨테이너 오케스트레이션, CI/CD 파이프라인 구축 경험을 바탕으로 개발팀 생산성 향상에 기여합니다.'
        WHEN 5 THEN '모바일 앱 개발에 열정을 가진 개발자입니다. React Native와 Flutter를 활용한 크로스 플랫폼 개발 경험이 있으며, 사용자 친화적인 모바일 인터페이스 설계에 관심이 많습니다.'
        WHEN 6 THEN '게임 개발에 대한 꿈을 키워온 개발자입니다. Unity와 C#을 활용한 2D/3D 게임 개발 경험이 있으며, 게임 로직 설계와 최적화에 특별한 관심을 가지고 있습니다.'
        WHEN 7 THEN '클라우드 기술에 깊은 관심을 가진 개발자입니다. AWS와 Azure 환경에서의 서비스 배포 및 운영 경험이 있으며, 확장 가능하고 안정적인 클라우드 아키텍처 설계를 추구합니다.'
        WHEN 8 THEN '인공지능 연구에 열정을 가진 개발자입니다. TensorFlow와 PyTorch를 활용한 딥러닝 모델 개발 경험이 있으며, 컴퓨터 비전과 자연어 처리 분야에 특별한 관심을 가지고 있습니다.'
        ELSE '정보보안에 깊은 관심을 가진 개발자입니다. 웹 애플리케이션 보안 취약점 분석과 침투 테스트 경험이 있으며, 안전한 시스템 구축을 위한 보안 아키텍처 설계에 관심이 많습니다.'
    END as content,
    'IT' as job_field,
    FLOOR(RAND() * 6) as experience_years,
    n as member_id,
    'ACTIVE' as status,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 30), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 5000) as t(n);

-- 이력서 대량 데이터 2500개 생성 (SYSTEM_RANGE 사용)
INSERT INTO resume (title, type, status, name, email, birth_year, phone, career_type, field_name, introduction, github_url, blog_url, notion_url, member_id, created_at, updated_at)
SELECT
    CASE (n % 15)
        WHEN 0 THEN '백엔드 개발자 이력서'
        WHEN 1 THEN '프론트엔드 개발자 이력서'
        WHEN 2 THEN '풀스택 개발자 이력서'
        WHEN 3 THEN '데이터 사이언티스트 이력서'
        WHEN 4 THEN 'DevOps 엔지니어 이력서'
        WHEN 5 THEN '모바일 개발자 이력서'
        WHEN 6 THEN '게임 개발자 이력서'
        WHEN 7 THEN '클라우드 엔지니어 이력서'
        WHEN 8 THEN 'AI 개발자 이력서'
        WHEN 9 THEN '보안 전문가 이력서'
        WHEN 10 THEN '블록체인 개발자 이력서'
        WHEN 11 THEN 'UI/UX 개발자 이력서'
        WHEN 12 THEN '시스템 프로그래머 이력서'
        WHEN 13 THEN '웹 개발자 이력서'
        ELSE '임베디드 개발자 이력서'
    END as title,
    CASE (n % 2) WHEN 0 THEN 'DEFAULT' ELSE 'MODERN' END as type,
    'ACTIVE' as status,
    CONCAT('사용자', n) as name,
    CONCAT('bench', LPAD(n, 4, '0'), '@test.com') as email,
    1990 + FLOOR(RAND() * 15) as birth_year,
    CONCAT('010-', LPAD(FLOOR(1000 + RAND() * 9000), 4, '0'), '-', LPAD(FLOOR(1000 + RAND() * 9000), 4, '0')) as phone,
    CASE (n % 3) WHEN 0 THEN 'FRESHMAN' ELSE 'EXPERIENCED' END as career_type,
    CASE (n % 12)
        WHEN 0 THEN '백엔드 개발'
        WHEN 1 THEN '프론트엔드 개발'
        WHEN 2 THEN '풀스택 개발'
        WHEN 3 THEN '데이터 분석'
        WHEN 4 THEN 'DevOps'
        WHEN 5 THEN '모바일 개발'
        WHEN 6 THEN '게임 개발'
        WHEN 7 THEN '클라우드'
        WHEN 8 THEN 'AI/ML'
        WHEN 9 THEN '정보보안'
        WHEN 10 THEN '블록체인'
        ELSE 'UI/UX'
    END as field_name,
    CASE (n % 5)
        WHEN 0 THEN 'Java Spring Boot 전문 개발자입니다. 실무 경험을 바탕으로 안정적이고 확장 가능한 서버 애플리케이션 개발에 전문성을 가지고 있습니다.'
        WHEN 1 THEN 'React와 TypeScript를 활용한 개발에 관심이 많은 개발자입니다. 사용자 경험을 중시하며 깔끔하고 직관적인 UI 개발을 추구합니다.'
        WHEN 2 THEN '백엔드와 프론트엔드 모두 경험이 있는 풀스택 개발자입니다. 전체적인 웹 서비스 아키텍처 설계 경험이 있습니다.'
        WHEN 3 THEN 'Python을 활용한 데이터 분석과 머신러닝에 관심이 많은 개발자입니다. 데이터 기반 의사결정 지원에 기여하고 싶습니다.'
        ELSE 'AWS 클라우드 환경에서 CI/CD 파이프라인 구축 및 운영 경험이 있는 개발자입니다. 컨테이너 오케스트레이션 전문가입니다.'
    END as introduction,
    CASE (n % 3) WHEN 0 THEN CONCAT('https://github.com/bench', LPAD(n, 4, '0')) ELSE NULL END as github_url,
    CASE (n % 4) WHEN 0 THEN CONCAT('https://blog.bench', LPAD(n, 4, '0'), '.com') ELSE NULL END as blog_url,
    CASE (n % 5) WHEN 0 THEN CONCAT('https://notion.so/bench', LPAD(n, 4, '0')) ELSE NULL END as notion_url,
    n as member_id,
    DATEADD('DAY', -FLOOR(RAND() * 365), NOW()) as created_at,
    DATEADD('DAY', -FLOOR(RAND() * 30), NOW()) as updated_at
FROM SYSTEM_RANGE(1, 2500) as t(n);

-- 3. 기술스택은 이미 TechStackSeeder로 생성되므로 별도 추가 불필요 (50-100개는 이미 충족)

-- 데이터 확인용 쿼리 (주석)
-- SELECT COUNT(*) as total_cover_letters FROM cover_letter;
-- SELECT COUNT(*) as total_resumes FROM resume;
-- SELECT COUNT(*) as total_tech_stacks FROM tech_stack;