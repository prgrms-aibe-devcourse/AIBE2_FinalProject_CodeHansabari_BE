package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.entity.TechStack;
import com.cvmento.domain.resume.repository.TechStackRepository;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 인덱스 성능 비교 벤치마크
 *
 * 사용법:
 * 1. 인덱스 없는 브랜치에서 실행 -> 결과 기록
 * 2. 인덱스 추가한 브랜치에서 실행 -> 결과 비교
 *
 * 실행: ./gradlew test --tests IndexPerformanceBenchmark
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(0) // Spring 컨텍스트 공유
public class IndexPerformanceBenchmark {

    private static ConfigurableApplicationContext context;
    private MemberRepository memberRepository;
    private CoverLetterRepository coverLetterRepository;
    private TechStackRepository techStackRepository;

    // 테스트용 데이터 (data.sql에서 생성됨)
    private final String testEmail = "test@example.com";
    private final Pageable pageable = PageRequest.of(0, 10);

    @Setup(Level.Trial)
    public void setupSpring() {
        log.info("=== 인덱스 성능 벤치마크 Spring Boot 시작 ===");

        context = SpringApplication.run(CvMentoApplication.class,
                "--spring.profiles.active=test",
                "--logging.level.org.springframework=WARN",
                "--logging.level.com.cvmento=INFO"
        );

        memberRepository = context.getBean(MemberRepository.class);
        coverLetterRepository = context.getBean(CoverLetterRepository.class);
        techStackRepository = context.getBean(TechStackRepository.class);

        log.info("=== Spring Boot 컨텍스트 로딩 완료 ===");

        // 데이터 존재 확인
        long memberCount = memberRepository.count();
        long coverLetterCount = coverLetterRepository.count();
        long techStackCount = techStackRepository.count();

        log.info("테스트 데이터 확인 - 회원: {}명, 자소서: {}개, 기술스택: {}개",
                memberCount, coverLetterCount, techStackCount);
    }

    @TearDown(Level.Trial)
    public void tearDownSpring() {
        if (context != null) {
            log.info("=== Spring Boot 컨텍스트 종료 ===");
            context.close();
        }
    }

    // ========== 회원 조회 테스트 ==========

    /**
     * 회원 Email 조회 성능
     * 인덱스 필요: email
     */
    @Benchmark
    public Optional<Member> 회원_Email로_조회() {
        return memberRepository.findByEmail(testEmail);
    }

    /**
     * 회원 상태별 집계 성능
     * 인덱스 필요: status
     */
    @Benchmark
    public long 회원_상태별_카운트() {
        return memberRepository.countByStatus(UserStatus.ACTIVE);
    }

    /**
     * 회원 역할별 집계 성능
     * 인덱스 필요: role
     */
    @Benchmark
    public long 회원_역할별_카운트() {
        return memberRepository.countByRole(Role.USER);
    }

    // ========== 자소서 조회 테스트 ==========

    /**
     * 사용자별 자소서 목록 조회 (페이징)
     * 인덱스 필요: member_id, status, updated_at (복합 인덱스)
     */
    @Benchmark
    public Page<CoverLetter> 자소서_사용자별_목록조회() {
        Optional<Member> member = memberRepository.findByEmail(testEmail);
        if (member.isPresent()) {
            return coverLetterRepository.findByMemberAndStatusOrderByUpdatedAtDesc(
                    member.get(), CoverLetterStatus.ACTIVE, pageable);
        }
        return Page.empty();
    }

    /**
     * 자소서 전체 목록 조회 (최신순)
     * 인덱스 필요: updated_at
     */
    @Benchmark
    public List<CoverLetter> 자소서_전체목록_최신순() {
        return coverLetterRepository.findAllByOrderByUpdatedAtDesc();
    }

    // ========== 기술스택 조회 테스트 ==========

    /**
     * 기술스택 이름순 전체 조회 (메타데이터용)
     * 인덱스 필요: name
     */
    @Benchmark
    public List<TechStack> 기술스택_이름순_전체조회() {
        return techStackRepository.findAllByOrderByNameAsc();
    }

    /**
     * 기술스택 이름으로 검색
     * 인덱스 필요: name
     */
    @Benchmark
    public Optional<TechStack> 기술스택_이름으로_검색() {
        return techStackRepository.findByName("Java");
    }

    // ========== 복합 쿼리 테스트 ==========

    /**
     * 전체 통계 조회 (여러 테이블 집계)
     * 각 테이블별 인덱스 필요
     */
    @Benchmark
    public String 전체_통계_조회() {
        long totalMembers = memberRepository.count();
        long activeMembers = memberRepository.countByStatus(UserStatus.ACTIVE);
        long totalCoverLetters = coverLetterRepository.count();
        long totalTechStacks = techStackRepository.count();

        return String.format("회원:%d(활성:%d), 자소서:%d, 기술스택:%d",
                totalMembers, activeMembers, totalCoverLetters, totalTechStacks);
    }

    /**
     * 벤치마크 실행 메인 메서드
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== 인덱스 성능 비교 벤치마크 시작 ===");
        System.out.println("현재 브랜치의 인덱스 상태를 확인하세요!");
        System.out.println("- 인덱스 없음: develop 브랜치");
        System.out.println("- 인덱스 있음: feature/add-indexes 브랜치");
        System.out.println("==========================================");

        Options opt = new OptionsBuilder()
                .include(IndexPerformanceBenchmark.class.getSimpleName())
                .shouldFailOnError(true)
                .jvmArgs("-Xmx2g", "-XX:+UseG1GC")
                .build();

        new Runner(opt).run();

        System.out.println("=== 벤치마크 완료! 결과를 기록하세요 ===");
    }
}