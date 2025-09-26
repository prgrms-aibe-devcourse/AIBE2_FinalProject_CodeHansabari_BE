package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
import com.cvmento.domain.resume.service.ResumeService;
import com.cvmento.global.exception.customException.MemberNotFoundException;
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
 * Spring Cache 성능 벤치마크 테스트
 * 비교 항목:
 * - 캐시 적용: 로그인한 100명 사용자에 대해 @Cacheable 메서드 호출 (Redis 캐시에서 조회)
 * - 캐시 미적용: 전체 5000명 사용자 중 랜덤 선택하여 캐시 우회 메서드 호출 (DB 직접 조회)
 *
 * 시나리오: 실제 서비스 운영 패턴 시뮬레이션
 * - 5000명 전체 회원 중 100명이 로그인 상태
 * - 로그인한 사용자는 AuthService를 통해 Redis 캐시에 저장됨
 * - 활성 사용자 vs 전체 사용자 풀에 대한 조회 성능 비교
 *
 * 목적: 로그인 기반 캐시 전략의 실제 성능 효과 측정
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1)
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

    // 캐시된 100명의 활성 사용자들
    private String[] cachedUsers;
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

        // 캐시 워밍업: 100명 로그인 시뮬레이션
        warmupCache();

        log.info("=== Spring Cache 벤치마크 환경 설정 완료 ===");
    }
    @Setup(Level.Iteration)
    public void checkCacheStatus() {
        // 배열이 비어있는지 확인
        if (cachedUsers == null || cachedUsers.length == 0) {
            log.warn("캐시된 사용자가 없습니다!");
            return;
        }

        log.info("캐시된 사용자 수: {}, 샘플 사용자 캐시 확인: {}",
                cachedUsers.length,
                staticCoverLetterService.findMemberByEmailForBenchmark(cachedUsers[0]).getName());
    }

    private static synchronized void initializeGlobalContext() {
        if (context == null || !context.isActive()) {
            log.info("=== Spring Cache 벤치마크 시작 ===");

            context = SpringApplication.run(CvMentoApplication.class,
                    "--spring.profiles.active=test",
                    "--spring.main.web-application-type=none",
                    "--server.port=0",
                    "--logging.level.org.springframework=WARN",
                    "--logging.level.com.cvmento=INFO",
                    "--logging.level.com.cvmento.domain=INFO",
                    "--logging.level.com.cvmento.domain.auth=INFO"
            );

            staticCoverLetterService = context.getBean(CoverLetterService.class);
            staticResumeService = context.getBean(ResumeService.class);
            staticUsageTokenService = context.getBean(UsageTokenService.class);
            staticMemberRepository = context.getBean(MemberRepository.class);
            staticAuthService = context.getBean(AuthService.class);

            log.info("=== Global Spring 컨텍스트 초기화 완료 ===");
        }
    }

    /**
     * 캐시 워밍업: 실제 존재하는 처음 100명 사용자를 캐시에 저장
     */
    private void warmupCache() {
        log.info("🚀 캐시 워밍업 시작: 100명 로그인 시뮬레이션");

        java.util.List<String> cachedUsersList = new java.util.ArrayList<>();

        for (int i = 1; i <= 5000 && cachedUsersList.size() < 100; i++) {
            String userEmail = "user" + i + "@example.com";

            try {
                Member user = staticMemberRepository.findByEmail(userEmail).orElse(null);

                if (user != null) {
                    log.info("사용자 찾음: {}", userEmail); // 디버깅 로그 추가
                    staticAuthService.cacheUserOnLogin(user);
                    Member cachedUser = staticCoverLetterService.findMemberByEmailForBenchmark(userEmail);

                    if (cachedUser != null) {
                        cachedUsersList.add(userEmail);
                        log.debug("✅ 캐시 워밍업 성공: {}", userEmail);
                    }
                } else {
                    log.debug("사용자 없음: {}", userEmail); // 사용자가 없을 때 로그
                }
            } catch (Exception e) {
                log.debug("캐시 워밍업 중 오류: {} - {}", userEmail, e.getMessage());
            }
        }

        this.cachedUsers = cachedUsersList.toArray(new String[0]);
        log.info("✅ 캐시 워밍업 완료: {}명 캐시됨", cachedUsers.length);

        // 사용자가 하나도 없으면 경고
        if (cachedUsers.length == 0) {
            log.error("❌ 캐시할 사용자를 찾을 수 없습니다! DB에 테스트 데이터가 있는지 확인하세요.");
        }
    }

    /**
     * 캐시가 실제로 히트되는지 검증
     */
    private void validateCacheHit(String email) {
        log.info("🔍 캐시 히트 검증 시작: {}", email);

        // 첫 번째 호출 - 캐시에서 조회되어야 함
        long startTime1 = System.nanoTime();
        Member member1 = staticCoverLetterService.findMemberByEmailForBenchmark(email);
        long time1 = System.nanoTime() - startTime1;

        // 두 번째 호출 - 캐시에서 조회되어야 함
        long startTime2 = System.nanoTime();
        Member member2 = staticCoverLetterService.findMemberByEmailForBenchmark(email);
        long time2 = System.nanoTime() - startTime2;

        log.info("🔍 캐시 검증 결과: 1차={}ns, 2차={}ns", time1, time2);

        // DB 직접 조회와 비교
        long startTime3 = System.nanoTime();
        Member member3 = staticCoverLetterService.findMemberByEmailNoCache(email);
        long time3 = System.nanoTime() - startTime3;

        log.info("🔍 DB 직접 조회: {}ns", time3);
        log.info("🔍 캐시 효과: {}x 빠름", (double)time3 / time2);
    }

    /**
     * 동일한 100명에 대한 캐시 적용 조회
     */
    @Benchmark
    public Member coverLetter_WithCache() {
        if (cachedUsers.length == 0) {
            throw new RuntimeException("캐시된 사용자가 없습니다!");
        }
        String userEmail = cachedUsers[random.nextInt(cachedUsers.length)];
        return coverLetterService.findMemberByEmailForBenchmark(userEmail); // @Cacheable 적용
    }

    /**
     * 동일한 100명에 대한 캐시 미적용 조회 (DB 직접)
     */
    @Benchmark
    public Member coverLetter_WithoutCache() {
        String userEmail = cachedUsers[random.nextInt(cachedUsers.length)];
        return coverLetterService.findMemberByEmailNoCache(userEmail); // 캐시 우회, DB 직접
    }

    /**
     * 이력서 서비스 - 캐시 적용
     */
    @Benchmark
    public Member resume_WithCache() {
        if (cachedUsers.length == 0) {
            throw new RuntimeException("캐시된 사용자가 없습니다!");
        }
        String userEmail = cachedUsers[random.nextInt(cachedUsers.length)];
        return resumeService.findMemberByEmailForBenchmark(userEmail);
    }

    /**
     * 이력서 서비스 - 캐시 미적용
     */
    @Benchmark
    public Member resume_WithoutCache() {
        String userEmail = cachedUsers[random.nextInt(cachedUsers.length)];
        return resumeService.findMemberByEmailNoCache(userEmail);
    }

    /**
     * 사용량토큰 서비스 - 캐시 적용
     */
    @Benchmark
    public Member usageToken_WithCache() {
        if (cachedUsers.length == 0) {
            throw new RuntimeException("캐시된 사용자가 없습니다!");
        }
        String userEmail = cachedUsers[random.nextInt(cachedUsers.length)];
        return usageTokenService.findMemberByEmailForBenchmark(userEmail);
    }

    /**
     * 사용량토큰 서비스 - 캐시 미적용
     */
    @Benchmark
    public Member usageToken_WithoutCache() {
        String userEmail = cachedUsers[random.nextInt(cachedUsers.length)];
        return usageTokenService.findMemberByEmailNoCache(userEmail);
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
        // 간단한 캐시 테스트 먼저 실행
        testCacheWorking();

        // 기존 벤치마크 코드
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
    private static void testCacheWorking() {
        // Spring 초기화 (기존 메서드 재사용)
        initializeGlobalContext();

        System.out.println("=== 캐시 작동 테스트 ===");

        // Redis에 있는 사용자로 직접 테스트
        String testEmail = "user10@example.com"; // Redis에 있다고 확인한 이메일

        // 5번 연속 호출
        for (int i = 1; i <= 5; i++) {
            long start = System.nanoTime();
            try {
                Member member = staticCoverLetterService.findMemberByEmailForBenchmark(testEmail);
                long end = System.nanoTime();
                System.out.println("호출 " + i + ": " + (end-start)/1000000.0 + "ms - " + member.getName());
            } catch (Exception e) {
                System.out.println("호출 " + i + ": 실패 - " + e.getMessage());
            }
        }
    }
}