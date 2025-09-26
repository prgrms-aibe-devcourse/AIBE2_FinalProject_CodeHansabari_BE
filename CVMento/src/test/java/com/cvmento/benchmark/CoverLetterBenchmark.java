package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterDetailResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterListResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.concurrent.TimeUnit;

/**
 * CoverLetterService 빠른 성능 벤치마크
 * - N+1 쿼리 측정에 집중
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class CoverLetterBenchmark {

    private static ConfigurableApplicationContext context;
    private CoverLetterService coverLetterService;

    // 테스트 데이터 설정
    private final String testUserEmail = "test@example.com";
    private final Long existingCoverLetterId = 1L;

    // 최소한의 파라미터 (test 사용자 10개 자소서 기준)
    @Param({"5", "10"})
    private int pageSize;

    @Param({"0", "1"})
    private int pageNumber;

    @Setup(Level.Trial)
    public void setupSpring() {
        log.info("=== 빠른 CoverLetter 벤치마크 시작 ===");

        context = SpringApplication.run(CvMentoApplication.class,
                "--spring.profiles.active=test",
                "--logging.level.org.springframework=WARN",
                "--logging.level.com.cvmento=INFO"
        );

        coverLetterService = context.getBean(CoverLetterService.class);

        log.info("=== Spring Boot 앱 시작 완료! ===");
        log.info("=== 테스트 파라미터: pageSize={}, pageNumber={} ===", pageSize, pageNumber);
    }

    @TearDown(Level.Trial)
    public void tearDownSpring() {
        if (context != null) {
            log.info("=== Spring Boot 앱 종료 중... ===");
            context.close();
            log.info("=== Spring Boot 앱 종료 완료! ===");
        }
    }

    /**
     * 자소서 목록 조회 - 썸네일 뷰 (N+1 쿼리 성능 측정)
     */
    @Benchmark
    public void getCoverLettersListThumbnailView(Blackhole bh) {
        Page<CoverLetterListResponse> result = coverLetterService.getCoverLetters(
                testUserEmail,
                PageRequest.of(pageNumber, pageSize),
                "thumbnail"
        );
        bh.consume(result);
    }

    /**
     * 자소서 목록 조회 - 전체 뷰 (N+1 쿼리 비교용)
     */
    @Benchmark
    public void getCoverLettersListFullView(Blackhole bh) {
        Page<CoverLetterListResponse> result = coverLetterService.getCoverLetters(
                testUserEmail,
                PageRequest.of(pageNumber, pageSize),
                "full"
        );
        bh.consume(result);
    }

    /**
     * 페이징 성능 - 첫 페이지
     */
    @Benchmark
    public void pagingPerformanceFirstPage(Blackhole bh) {
        Page<CoverLetterListResponse> result = coverLetterService.getCoverLetters(
                testUserEmail,
                PageRequest.of(0, pageSize),
                "thumbnail"
        );
        bh.consume(result);
    }

    /**
     * 페이징 성능 - 두 번째 페이지
     */
    @Benchmark
    public void pagingPerformanceSecondPage(Blackhole bh) {
        Page<CoverLetterListResponse> result = coverLetterService.getCoverLetters(
                testUserEmail,
                PageRequest.of(1, pageSize),
                "thumbnail"
        );
        bh.consume(result);
    }

    /**
     * 실제 사용 패턴 - 목록 조회 후 상세 조회
     */
    @Benchmark
    public void realUsagePatternMixedScenario(Blackhole bh) {
        // 1. 목록 조회
        Page<CoverLetterListResponse> list = coverLetterService.getCoverLetters(
                testUserEmail,
                PageRequest.of(0, 5),
                "thumbnail"
        );

        // 2. 상세 조회 (목록에서 첫 번째 항목)
        if (!list.getContent().isEmpty()) {
            Long firstId = list.getContent().get(0).coverLetterId();
            CoverLetterDetailResponse detail = coverLetterService.getCoverLetter(
                    firstId,
                    testUserEmail
            );
            bh.consume(detail);
        }

        bh.consume(list);
    }
    /**
     * 관리자 기능 - 상태별 조회 (QueryDSL 카운트 쿼리 성능)
     */
    @Benchmark
    public void adminGetCoverLettersByStatusNoFilter(Blackhole bh) {
        Page<CoverLetterStatusListResponse> result = coverLetterService.getCoverLettersByStatus(
                CoverLetterStatus.ACTIVE,
                null, // 이메일 필터 없음 - 불필요한 JOIN 발생
                null, // 제목 필터 없음
                PageRequest.of(pageNumber, pageSize),
                "admin@company.com"
        );
        bh.consume(result);
    }

    @Benchmark
    public void adminGetCoverLettersWithEmailFilter(Blackhole bh) {
        Page<CoverLetterStatusListResponse> result = coverLetterService.getCoverLettersByStatus(
                CoverLetterStatus.ACTIVE,
                "user", // 이메일 필터 있음 - JOIN 필요
                null,
                PageRequest.of(pageNumber, pageSize),
                "admin@company.com"
        );
        bh.consume(result);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== CoverLetter 서비스 성능 벤치마크 시작 ===");
        System.out.println("측정 항목:");
        System.out.println("- N+1 쿼리 성능 (핵심)");
        System.out.println("- 페이징 성능 비교");
        System.out.println("- 실제 사용 패턴");

        Options opt = new OptionsBuilder()
                .include("com.cvmento.benchmark.CoverLetterBenchmark") // 정확한 클래스명
                .exclude(".*ResumeBenchmark.*")  // 다른 벤치마크 제외
                .exclude(".*MemberBenchmark.*")
                .shouldFailOnError(true)
                .jvmArgs("-Xmx2g", "-Xms1g") // 메모리 설정 축소
                .result("coverletter-benchmark-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();

        System.out.println("=== 빠른 벤치마크 완료! ===");
        System.out.println("결과 파일: coverletter-benchmark-results.json");
    }
}