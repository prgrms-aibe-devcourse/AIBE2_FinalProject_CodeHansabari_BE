package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
import com.cvmento.domain.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * Spring Cache 어노테이션 기반 벤치마크
 *
 * 비교 항목:
 * - 캐시 적용: @Cacheable 어노테이션 사용
 * - 캐시 미적용: DB 직접 조회
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(0)
public class SpringCacheBenchmark {

    // Global state
    private static volatile ConfigurableApplicationContext context;
    private static CoverLetterService staticCoverLetterService;
    private static MemberRepository staticMemberRepository;
    private static AuthService staticAuthService;

    // Instance fields
    private CoverLetterService coverLetterService;
    private MemberRepository memberRepository;
    private AuthService authService;

    private final String testEmail = "test@google.com";

    @Setup(Level.Trial)
    public void setupSpring() {
        initializeGlobalContext();

        // Instance fields 할당
        this.coverLetterService = staticCoverLetterService;
        this.memberRepository = staticMemberRepository;
        this.authService = staticAuthService;

        // 🔥 로그인 시뮬레이션: 실제 시나리오 반영
        simulateUserLogin();

        log.info("=== Spring Cache 벤치마크 환경 설정 완료 ===");
    }

    private static synchronized void initializeGlobalContext() {
        if (context == null || !context.isActive()) {
            log.info("=== Spring Cache 벤치마크 시작 ===");

            context = SpringApplication.run(CvMentoApplication.class,
                    "--spring.profiles.active=test",
                    "--spring.main.web-application-type=none",
                    "--server.port=0",
                    "--logging.level.org.springframework=WARN"
            );

            staticCoverLetterService = context.getBean(CoverLetterService.class);
            staticMemberRepository = context.getBean(MemberRepository.class);
            staticAuthService = context.getBean(AuthService.class);

            // 테스트 데이터 확인 및 생성
            Member testMember = staticMemberRepository.findByEmail("test@google.com")
                    .orElseGet(() -> {
                        log.info("테스트 사용자 생성: test@google.com");
                        Member newMember = new Member("test-google-id-" + System.currentTimeMillis(),
                                                    "test@google.com", "Spring Cache 테스트 유저",
                                                    "https://via.placeholder.com/150");
                        return staticMemberRepository.save(newMember);
                    });
            log.info("✅ 테스트 멤버 준비: {} (ID: {})", testMember.getEmail(), testMember.getMemberId());

            log.info("=== Global Spring 컨텍스트 초기화 완료 ===");
        }
    }

    /**
     * 로그인 시뮬레이션: 실제 사용자 플로우 반영
     * 1. 사용자 로그인 → AuthService.cacheUserOnLogin() 호출
     * 2. 캐시에 Member 정보 저장
     * 3. 이후 API 호출 시 캐시 히트
     */
    private void simulateUserLogin() {
        log.info("=== 🚀 로그인 시뮬레이션 시작 ===");

        try {
            // 테스트 사용자 조회
            Member testMember = staticMemberRepository.findByEmail(testEmail)
                    .orElseThrow(() -> new RuntimeException("Test member not found: " + testEmail));

            // 🔥 실제 로그인 시나리오: AuthService를 통한 캐시 저장
            Member cachedMember = staticAuthService.cacheUserOnLogin(testMember);

            log.info("✅ 로그인 시뮬레이션 완료: {} → 캐시 저장됨", cachedMember.getEmail());
            log.info("=== 이제 자소서 API 호출 시 캐시 히트 예상 ===");

        } catch (Exception e) {
            log.error("❌ 로그인 시뮬레이션 실패", e);
            throw new RuntimeException("Login simulation failed", e);
        }
    }

    /**
     * 캐시 적용 버전 - @Cacheable 사용
     * 첫 호출: DB 조회 + 캐시 저장
     * 이후 호출: 캐시에서 바로 반환
     */
    @Benchmark
    public Member 캐시적용_Member조회() {
        return coverLetterService.findMemberByEmailForBenchmark(testEmail);
    }

    /**
     * 캐시 미적용 버전 - DB 직접 조회
     * 매번 DB 쿼리 실행
     */
    @Benchmark
    public Member 캐시미적용_Member조회() {
        return coverLetterService.findMemberByEmailNoCache(testEmail);
    }

    /**
     * 캐시 적용 - 연속 API 호출 시나리오
     * 실제 사용 패턴: 자소서 목록 → 저장 → 수정 → 목록 조회
     */
    @Benchmark
    @OperationsPerInvocation(5)
    public void 캐시적용_연속API호출() {
        for (int i = 0; i < 5; i++) {
            coverLetterService.findMemberByEmailForBenchmark(testEmail);
        }
    }

    /**
     * 캐시 미적용 - 연속 API 호출 시나리오
     */
    @Benchmark
    @OperationsPerInvocation(5)
    public void 캐시미적용_연속API호출() {
        for (int i = 0; i < 5; i++) {
            coverLetterService.findMemberByEmailNoCache(testEmail);
        }
    }

    /**
     * 캐시 적용 - 하루 사용 패턴
     * 한 사용자가 하루에 자소서 기능을 사용하는 패턴
     */
    @Benchmark
    @OperationsPerInvocation(10)
    public void 캐시적용_일일사용패턴() {
        for (int i = 0; i < 10; i++) {
            coverLetterService.findMemberByEmailForBenchmark(testEmail);
        }
    }

    /**
     * 캐시 미적용 - 하루 사용 패턴
     */
    @Benchmark
    @OperationsPerInvocation(10)
    public void 캐시미적용_일일사용패턴() {
        for (int i = 0; i < 10; i++) {
            coverLetterService.findMemberByEmailNoCache(testEmail);
        }
    }

    // Cleanup
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (context != null && context.isActive()) {
                log.info("=== JVM 종료 - Spring 컨텍스트 정리 ===");
                context.close();
            }
        }));
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(SpringCacheBenchmark.class.getSimpleName())
                .result("spring-cache-benchmark-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        try {
            new Runner(opt).run();
        } finally {
            if (context != null && context.isActive()) {
                log.info("=== 벤치마크 완료 - Spring 컨텍스트 강제 종료 ===");
                context.close();
            }
            System.exit(0);
        }
    }
}