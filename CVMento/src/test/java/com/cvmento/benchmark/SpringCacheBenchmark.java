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
 * - 캐시 적용: 동일한 100명 사용자에 대해 @Cacheable 메서드 호출 (메모리 캐시에서 조회)
 * - 캐시 미적용: 동일한 100명 사용자에 대해 캐시 우회 메서드 호출 (DB 직접 조회)
 *
 * 목적: 캐시 사용 유무에 따른 순수한 성능 차이 측정
 * 조건: 동일한 데이터셋, 동일한 사용자 풀을 사용하여 변수 통제
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
                // 🔥 실제 벤치마크에서 사용할 @Cacheable 메서드로 워밍업
                Member user = staticCoverLetterService.findMemberByEmailForBenchmark(userEmail);

                if (user != null) {
                    cachedUsersList.add(userEmail);
                    log.debug("캐시 워밍업 완료: {}", userEmail);
                }
            } catch (Exception e) {
                continue;
            }
        }

        this.cachedUsers = cachedUsersList.toArray(new String[0]);
        log.info("✅ 캐시 워밍업 완료: {}명 캐시됨", cachedUsers.length);
    }

    /**
     * 동일한 100명에 대한 캐시 적용 조회
     */
    @Benchmark
    public Member coverLetter_WithCache() {
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