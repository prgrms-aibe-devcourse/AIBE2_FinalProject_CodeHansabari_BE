package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
import com.cvmento.domain.resume.service.ResumeService;
import com.cvmento.global.usage.service.UsageTokenService;
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
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(0)
public class SpringCacheBenchmark {

    // Global state
    private static volatile ConfigurableApplicationContext context;
    private static CoverLetterService staticCoverLetterService;
    private static ResumeService staticResumeService;
    private static UsageTokenService staticUsageTokenService;
    private static MemberRepository staticMemberRepository;
    private static AuthService staticAuthService;

    // Instance fields
    private CoverLetterService coverLetterService;
    private ResumeService resumeService;
    private UsageTokenService usageTokenService;
    private MemberRepository memberRepository;
    private AuthService authService;

    private final String testEmail = "test@google.com";

    // 동시 이용자 100명 시뮬레이션 (실제 존재하는 사용자들로만 구성)
    private String[] activeUsers;  // 캐시된 사용자들 (100명)
    private String[] nonCachedUsers;  // 캐시되지 않은 나머지 사용자들

    private java.util.Random random = new java.util.Random();

    @Setup(Level.Trial)
    public void setupSpring() {
        initializeGlobalContext();

        // Instance fields 할당
        this.coverLetterService = staticCoverLetterService;
        this.resumeService = staticResumeService;
        this.usageTokenService = staticUsageTokenService;
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
            staticResumeService = context.getBean(ResumeService.class);
            staticUsageTokenService = context.getBean(UsageTokenService.class);
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

            log.info("✅ 테스트 사용자 로그인 완료: {} → 캐시 저장됨", cachedMember.getEmail());

            // 실제 존재하는 사용자들을 캐시된 그룹과 비캐시 그룹으로 분류
            log.info("🚀 실제 존재하는 사용자들 수집 및 분류 시작...");
            java.util.List<String> cachedUsersList = new java.util.ArrayList<>();
            java.util.List<String> nonCachedUsersList = new java.util.ArrayList<>();

            for (int i = 1; i <= 5000; i++) {
                String userEmail = "user" + i + "@example.com";
                Member user = staticMemberRepository.findByEmail(userEmail)
                        .orElse(null);
                if (user != null) {
                    if (cachedUsersList.size() < 100) {
                        // 처음 100명은 캐시된 그룹에 추가
                        staticAuthService.cacheUserOnLogin(user);
                        cachedUsersList.add(userEmail);
                    } else {
                        // 나머지는 비캐시 그룹에 추가
                        nonCachedUsersList.add(userEmail);
                    }
                }
            }

            // 배열 설정
            this.activeUsers = cachedUsersList.toArray(new String[0]);
            this.nonCachedUsers = nonCachedUsersList.toArray(new String[0]);

            log.info("✅ 캐시된 사용자: {}명, 비캐시 사용자: {}명", activeUsers.length, nonCachedUsers.length);
            log.info("=== 이제 API 호출 시 캐시 히트 예상 ===");

        } catch (Exception e) {
            log.error("❌ 로그인 시뮬레이션 실패", e);
            throw new RuntimeException("Login simulation failed", e);
        }
    }

    /**
     * 핵심 비교 - 캐시 적용 (100명 중 선택)
     */
    @Benchmark
    public Member 캐시적용_Member조회() {
        String userEmail = activeUsers[random.nextInt(activeUsers.length)];
        return coverLetterService.findMemberByEmailForBenchmark(userEmail);
    }

    /**
     * 핵심 비교 - 캐시 미적용 (비캐시 사용자들 중 선택)
     */
    @Benchmark
    public Member 캐시미적용_Member조회() {
        if (nonCachedUsers == null || nonCachedUsers.length == 0) {
            // 폴백: 비캐시 사용자가 없으면 마지막 사용자 사용
            String fallbackEmail = "user5000@example.com";
            return coverLetterService.findMemberByEmailNoCache(fallbackEmail);
        }
        String userEmail = nonCachedUsers[random.nextInt(nonCachedUsers.length)];
        return coverLetterService.findMemberByEmailNoCache(userEmail);
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