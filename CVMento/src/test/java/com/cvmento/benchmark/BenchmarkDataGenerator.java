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
 * 현실적인 벤치마크용 테스트 데이터 생성기
 * 5000명 회원, test 사용자 10개 자소서, 일반 사용자 3-5개 자소서
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
        log.info("=== 빠른 벤치마크 데이터 생성 시작 ===");

        createTestMember();
        createBulkMembers();
        createBulkCoverLetters();
        createBulkResumes();

        log.info("=== 빠른 벤치마크 데이터 생성 완료 ===");
        logDataSummary();
    }

    /**
     * 테스트 멤버 생성 (10개 자소서 포함)
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

        // 테스트 사용자용 자소서 10개 생성
        List<CoverLetter> testCoverLetters = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String jobType = switch (i % 5) {
                case 0 -> "백엔드";
                case 1 -> "프론트엔드";
                case 2 -> "풀스택";
                case 3 -> "DevOps";
                default -> "AI";
            };

            CoverLetter coverLetter = new CoverLetter(
                    String.format("[테스트] %s 개발자 자소서 %d", jobType, i),
                    String.format("안녕하세요. %s 개발자를 꿈꾸는 지원자입니다. " +
                            "Java와 Spring Boot를 활용한 웹 애플리케이션 개발 경험이 있으며, " +
                            "특히 REST API 설계와 데이터베이스 최적화에 관심이 많습니다. " +
                            "팀워크를 중시하며, 사용자 중심의 서비스 개발에 관심이 많습니다. " +
                            "새로운 기술에 대한 호기심과 도전 정신으로 더 나은 개발자가 되고 싶습니다. " +
                            "자소서 번호: %d", jobType, i),
                    "IT",
                    (i % 5) + 1, // 1-5년 경력
                    testMember
            );

            // 10%는 DELETED 상태로 설정
            if (i % 10 == 0) {
                coverLetter.delete();
            }

            testCoverLetters.add(coverLetter);
        }

        coverLetterRepository.saveAll(testCoverLetters);
        log.info("테스트 멤버와 10개 자소서 생성 완료");
    }

    /**
     * 대규모 멤버 데이터 생성 (5000명 총)
     */
    @Transactional
    public void createBulkMembers() {
        log.info("대규모 멤버 데이터 생성 시작... (5000명)");

        List<Member> members = new ArrayList<>();
        int batchSize = 100;

        // ACTIVE USER들 (4500명: id 2~4501)
        for (int i = 2; i <= 4501; i++) {
            Member member = new Member("google-user-" + i, "user" + i + "@example.com",
                    "사용자" + i, "https://example.com/profile" + i + ".jpg");
            member.activate();
            members.add(member);

            if (members.size() >= batchSize) {
                memberRepository.saveAll(members);
                members.clear();
            }
        }

        // INACTIVE USER들 (400명: id 4502~4901)
        for (int i = 4502; i <= 4901; i++) {
            Member member = new Member("google-user-" + i, "user" + i + "@example.com",
                    "비활성사용자" + i, "https://example.com/profile" + i + ".jpg");
            member.deactivate();
            members.add(member);
        }

        // SUSPENDED USER들 (80명: id 4902~4981)
        for (int i = 4902; i <= 4981; i++) {
            Member member = new Member("google-user-" + i, "suspended" + i + "@example.com",
                    "정지된사용자" + i, "https://example.com/profile" + i + ".jpg");
            member.deactivate();
            members.add(member);
        }

        // ACTIVE ADMIN들 (18명: id 4982~4999)
        for (int i = 4982; i <= 4999; i++) {
            Member member = new Member("google-admin-" + i, "admin" + i + "@company.com",
                    "관리자" + i, "https://example.com/admin" + i + ".jpg");
            member.changeRole(Role.ADMIN);
            members.add(member);
        }

        // ACTIVE ROOT (1명: id 5000)
        Member rootMember = new Member("google-root-5000", "root5000@company.com",
                "루트관리자5000", "https://example.com/root5000.jpg");
        rootMember.changeRole(Role.ROOT);
        members.add(rootMember);

        // 남은 데이터 저장
        if (!members.isEmpty()) {
            memberRepository.saveAll(members);
        }

        log.info("멤버 데이터 생성 완료: {}명", memberRepository.count());
    }

    /**
     * 대규모 자소서 데이터 생성 (약 15000개: 일반 사용자당 3-5개)
     */
    @Transactional
    public void createBulkCoverLetters() {
        log.info("대규모 자소서 데이터 생성 시작...");

        List<CoverLetter> coverLetters = new ArrayList<>();
        int batchSize = 200;
        int totalCreated = 0;

        // 일반 사용자들(id 2~5000)에게 각각 3-5개의 자소서 생성
        for (long memberId = 2; memberId <= 5000; memberId++) {
            Member member = memberRepository.findById(memberId).orElse(null);
            if (member == null) continue;

            // 각 사용자마다 3-5개의 자소서 (랜덤)
            int coverLetterCount = 3 + random.nextInt(3); // 3~5개

            for (int coverLetterIndex = 1; coverLetterIndex <= coverLetterCount; coverLetterIndex++) {
                String jobType = switch (coverLetterIndex % 5) {
                    case 0 -> "백엔드";
                    case 1 -> "프론트엔드";
                    case 2 -> "풀스택";
                    case 3 -> "DevOps";
                    default -> "AI";
                };

                CoverLetter coverLetter = new CoverLetter(
                        String.format("자소서 %d - %s 개발자", coverLetterIndex, jobType),
                        String.format("안녕하세요. %s 개발자를 꿈꾸는 지원자입니다. " +
                                "열정적으로 개발에 임하고 있으며, 지속적인 학습을 통해 성장하고 있습니다. " +
                                "다양한 프로젝트 경험을 바탕으로 실무에 바로 적용할 수 있는 역량을 갖추었습니다. " +
                                "팀워크를 중시하며, 사용자 중심의 서비스 개발에 관심이 많습니다. " +
                                "새로운 기술에 대한 호기심과 도전 정신으로 더 나은 개발자가 되고 싶습니다. " +
                                "자소서 번호: %d, 회원ID: %d", jobType, coverLetterIndex, memberId),
                        "IT",
                        (coverLetterIndex % 5) + 1,
                        member
                );

                // 10%는 DELETED 상태로 설정
                if (coverLetterIndex % 10 == 0) {
                    coverLetter.delete();
                }

                coverLetters.add(coverLetter);
                totalCreated++;

                if (coverLetters.size() >= batchSize) {
                    coverLetterRepository.saveAll(coverLetters);
                    coverLetters.clear();
                }
            }
        }

        // 남은 데이터 저장
        if (!coverLetters.isEmpty()) {
            coverLetterRepository.saveAll(coverLetters);
        }

        log.info("자소서 데이터 생성 완료: {}개", coverLetterRepository.count());
    }

    /**
     * 대규모 이력서 데이터 생성 (2500개: 회원의 50%)
     */
    @Transactional
    public void createBulkResumes() {
        log.info("대규모 이력서 데이터 생성 시작...");

        List<Resume> resumes = new ArrayList<>();
        int batchSize = 100;

        // 첫 2500명의 사용자에게 이력서 생성 (50% 비율)
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
                    "resume" + i + "@example.com",
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
        log.info("=== 빠른 벤치마크 데이터 생성 요약 ===");
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