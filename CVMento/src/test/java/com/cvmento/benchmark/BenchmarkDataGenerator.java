package com.cvmento.benchmark;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.resume.enums.ResumeStatus;
import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 벤치마크용 테스트 데이터를 프로그래밍 방식으로 생성
 * @Profile("test") 애너테이션으로 테스트 환경에서만 실행
 */
@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class BenchmarkDataGenerator implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final ResumeRepository resumeRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 벤치마크 데이터 생성 시작 ===");

        // 기존 데이터가 있으면 건너뛰기
        if (memberRepository.count() > 1) {
            log.info("이미 데이터가 존재합니다. 데이터 생성을 건너뜁니다.");
            return;
        }

        createTestMember();
        createBulkMembers();
        createBulkCoverLetters();
        createBulkResumes();

        log.info("=== 벤치마크 데이터 생성 완료 ===");
        logDataSummary();
    }

    /**
     * 기본 테스트 멤버 생성 (member_id: 1)
     */
    @Transactional
    public void createTestMember() {
        if (memberRepository.findByEmail("test@example.com").isPresent()) {
            log.info("테스트 멤버가 이미 존재합니다.");
            return;
        }

        Member testMember = new Member("test-google-123", "test@example.com",
                "테스트유저", "https://example.com/profile.jpg");

        memberRepository.save(testMember);

        // 테스트 자소서 생성
        CoverLetter testCoverLetter = new CoverLetter(
                "[원본] 백엔드 개발자 자기소개서",
                "안녕하세요. 백엔드 개발자를 꿈꾸는 지원자입니다. Java와 Spring Boot를 활용한 웹 애플리케이션 개발 경험이 있으며, 특히 REST API 설계와 데이터베이스 최적화에 관심이 많습니다.",
                "IT",
                3,
                testMember
        );
        coverLetterRepository.save(testCoverLetter);

        log.info("기본 테스트 데이터 생성 완료");
    }

    /**
     * 대량 멤버 데이터 생성 (5000명)
     */
    @Transactional
    public void createBulkMembers() {
        log.info("대량 멤버 데이터 생성 시작...");

        List<Member> members = new ArrayList<>();
        int batchSize = 1000;

        // ACTIVE USER들 (3,760명: id 2~3761)
        for (int i = 2; i <= 3761; i++) {
            Member member = new Member("google-user-" + i, "user" + i + "@example.com",
                    "사용자" + i, "https://example.com/profile" + i + ".jpg");
            member.activate(); // 명시적으로 ACTIVE 상태 설정
            members.add(member);

            if (members.size() >= batchSize) {
                memberRepository.saveAll(members);
                members.clear();
                log.info("ACTIVE USER 진행률: {}/{}", i-1, 3760);
            }
        }

        // INACTIVE USER들 (752명: id 3762~4513)
        for (int i = 3762; i <= 4513; i++) {
            Member member = new Member("google-user-" + i, "user" + i + "@example.com",
                    "비활성사용자" + i, "https://example.com/profile" + i + ".jpg");
            member.deactivate(); // INACTIVE 상태로 설정
            members.add(member);

            if (members.size() >= batchSize) {
                memberRepository.saveAll(members);
                members.clear();
            }
        }

        // SUSPENDED USER들 (188명: id 4514~4701)
        for (int i = 4514; i <= 4701; i++) {
            Member member = new Member("google-user-" + i, "suspended" + i + "@example.com",
                    "정지된사용자" + i, "https://example.com/profile" + i + ".jpg");
            member.deactivate(); // SUSPENDED도 비활성화로 처리 (Member 엔티티에 suspend 메소드가 없음)
            members.add(member);
        }

        // ACTIVE ADMIN들 (238명: id 4702~4939)
        for (int i = 4702; i <= 4939; i++) {
            Member member = new Member("google-admin-" + i, "admin" + i + "@company.com",
                    "관리자" + i, "https://example.com/admin" + i + ".jpg");
            member.changeRole(Role.ADMIN);
            members.add(member);
        }

        // INACTIVE ADMIN들 (12명: id 4940~4951)
        for (int i = 4940; i <= 4951; i++) {
            Member member = new Member("google-admin-" + i, "inactive-admin" + i + "@company.com",
                    "비활성관리자" + i, "https://example.com/admin" + i + ".jpg");
            member.changeRole(Role.ADMIN);
            member.deactivate();
            members.add(member);
        }

        // ACTIVE ROOT들 (48명: id 4952~4999)
        for (int i = 4952; i <= 4999; i++) {
            Member member = new Member("google-root-" + i, "root" + i + "@company.com",
                    "루트관리자" + i, "https://example.com/root" + i + ".jpg");
            member.changeRole(Role.ROOT);
            members.add(member);
        }

        // INACTIVE ROOT들 (2명: id 5000~5001)
        for (int i = 5000; i <= 5001; i++) {
            Member member = new Member("google-root-" + i, "inactive-root" + i + "@company.com",
                    "비활성루트관리자" + i, "https://example.com/root" + i + ".jpg");
            member.changeRole(Role.ROOT);
            member.deactivate();
            members.add(member);
        }

        // 남은 데이터 저장
        if (!members.isEmpty()) {
            memberRepository.saveAll(members);
        }

        log.info("멤버 데이터 생성 완료: {}명", memberRepository.count());
    }

    /**
     * 대량 자소서 데이터 생성 (5000개)
     */
    @Transactional
    public void createBulkCoverLetters() {
        log.info("대량 자소서 데이터 생성 시작...");

        List<CoverLetter> coverLetters = new ArrayList<>();
        int batchSize = 1000;

        for (int i = 1; i <= 5000; i++) {
            // member_id는 1~5000 범위에서 순환
            Long memberId = (long) i;
            if (i > 5001) memberId = (long) ((i % 5001) + 1);

            Member member = memberRepository.findById(memberId).orElse(null);
            if (member == null) {
                member = memberRepository.findById(1L).orElseThrow(); // fallback to test user
            }

            String jobType = switch (i % 5) {
                case 0 -> "백엔드";
                case 1 -> "프론트엔드";
                case 2 -> "풀스택";
                case 3 -> "DevOps";
                default -> "AI";
            };

            CoverLetter coverLetter = new CoverLetter(
                    "자기소개서 " + i,
                    String.format("안녕하세요. %s 개발자를 꿈꾸는 지원자입니다. " +
                            "열정적으로 개발에 임하고 있으며, 지속적인 학습을 통해 성장하고 있습니다. " +
                            "다양한 프로젝트 경험을 바탕으로 실무에 바로 적용할 수 있는 역량을 갖추었습니다. " +
                            "팀워크를 중시하며, 사용자 중심의 서비스 개발에 관심이 많습니다. " +
                            "새로운 기술에 대한 호기심과 도전 정신으로 더 나은 개발자가 되고 싶습니다. " +
                            "자소서 번호: %d", jobType, i),
                    "IT",
                    (i % 5) + 1,
                    member
            );

            // 10%는 DELETED 상태로 설정
            if (i % 10 == 0) {
                coverLetter.delete();
            }

            coverLetters.add(coverLetter);

            if (coverLetters.size() >= batchSize) {
                coverLetterRepository.saveAll(coverLetters);
                coverLetters.clear();
                log.info("자소서 생성 진행률: {}/{}", i, 5000);
            }
        }

        // 남은 데이터 저장
        if (!coverLetters.isEmpty()) {
            coverLetterRepository.saveAll(coverLetters);
        }

        log.info("자소서 데이터 생성 완료: {}개", coverLetterRepository.count());
    }

    /**
     * 대량 이력서 데이터 생성 (2500개)
     */
    @Transactional
    public void createBulkResumes() {
        log.info("대량 이력서 데이터 생성 시작...");

        List<Resume> resumes = new ArrayList<>();
        int batchSize = 1000;

        for (int i = 1; i <= 2500; i++) {
            Member member = memberRepository.findById((long) i).orElse(null);
            if (member == null) {
                member = memberRepository.findById(1L).orElseThrow(); // fallback to test user
            }

            String fieldName = switch (i % 4) {
                case 0 -> "백엔드 개발자";
                case 1 -> "프론트엔드 개발자";
                case 2 -> "풀스택 개발자";
                default -> "DevOps 엔지니어";
            };

            Resume resume = Resume.createResume(
                    "이력서 " + i,
                    i % 2 == 0 ? ResumeType.DEFAULT : ResumeType.MODERN,
                    "개발자" + i,
                    "resume" + i + "@example.com", // 고유 이메일
                    1990 + (i % 15),
                    String.format("010-%04d-%04d", i % 9999, (i * 7) % 9999),
                    i % 2 == 0 ? CareerType.FRESHMAN : CareerType.EXPERIENCED,
                    fieldName,
                    member
            );

            // 12.5%는 DELETED 상태로 설정 (8개 중 1개)
            if (i % 8 == 0) {
                resume.updateStatus(ResumeStatus.DELETED);
            }

            resumes.add(resume);

            if (resumes.size() >= batchSize) {
                resumeRepository.saveAll(resumes);
                resumes.clear();
                log.info("이력서 생성 진행률: {}/{}", i, 2500);
            }
        }

        // 남은 데이터 저장
        if (!resumes.isEmpty()) {
            resumeRepository.saveAll(resumes);
        }

        log.info("이력서 데이터 생성 완료: {}개", resumeRepository.count());
    }

    /**
     * 생성된 데이터 요약 로깅
     */
    private void logDataSummary() {
        log.info("=== 데이터 생성 요약 ===");
        log.info("총 회원 수: {}", memberRepository.count());
        log.info("총 자소서 수: {}", coverLetterRepository.count());
        log.info("총 이력서 수: {}", resumeRepository.count());

        // 상태별 통계
        log.info("ACTIVE 회원: {}", memberRepository.countByStatus(UserStatus.ACTIVE));
        log.info("INACTIVE 회원: {}", memberRepository.countByStatus(UserStatus.INACTIVE));
        log.info("SUSPENDED 회원: {}", memberRepository.countByStatus(UserStatus.SUSPENDED));

        log.info("USER 역할: {}", memberRepository.countByRole(Role.USER));
        log.info("ADMIN 역할: {}", memberRepository.countByRole(Role.ADMIN));
        log.info("ROOT 역할: {}", memberRepository.countByRole(Role.ROOT));
    }
}