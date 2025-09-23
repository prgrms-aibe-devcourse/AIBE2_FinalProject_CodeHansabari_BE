package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterDetailResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterListResponse;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.concurrent.TimeUnit;

/**
 * CoverLetterService 성능 벤치마크
 *
 * 실행 방법:
 * 1. IDE에서 main 메서드 실행
 * 2. 또는 터미널에서: ./gradlew test --tests CoverLetterBenchmark
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(0) // 같은 JVM에서 실행 (스프링 컨텍스트 공유)
public class CoverLetterBenchmark {

    private static ConfigurableApplicationContext context;
    private CoverLetterService coverLetterService;

    // 테스트 데이터 (data-test.sql에서 생성됨)
    private final String testUserEmail = "user100@example.com";
    private final Long existingCoverLetterId = 1L;

    @Setup(Level.Trial)
    public void setupSpring() {
        log.info("=== Spring Boot 앱 시작 중... ===");

        context = SpringApplication.run(CvMentoApplication.class,
                "--spring.profiles.active=test",
                "--logging.level.org.springframework=WARN",
                "--logging.level.com.cvmento=INFO"
        );

        coverLetterService = context.getBean(CoverLetterService.class);
        log.info("=== Spring Boot 앱 시작 완료! ===");
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
     * 자기소개서 목록 조회 성능 측정
     * - 가장 많이 사용되는 기능
     * - 페이징, DB 조회, DTO 변환 포함
     */
    @Benchmark
    public Page<CoverLetterListResponse> 자기소개서목록조회() {
        return coverLetterService.getCoverLetters(
                testUserEmail,
                PageRequest.of(0, 5),
                "thumbnail"
        );
    }

    /**
     * 자기소개서 상세 조회 성능 측정
     * - 단건 조회 성능
     * - DB 조회, 권한 확인, DTO 변환 포함
     */
    @Benchmark
    public CoverLetterDetailResponse 자기소개서상세조회() {
        return coverLetterService.getCoverLetter(existingCoverLetterId, testUserEmail);
    }

    /**
     * 자기소개서 저장 성능 측정 (원본)
     * - 트랜잭션, DB INSERT 성능
     * - 비즈니스 로직 처리 시간 포함
     */
    @Benchmark
    public void 자기소개서저장_원본() {
        CoverLetterSaveRequest request = new CoverLetterSaveRequest(
                "벤치마크 테스트 자기소개서 " + System.currentTimeMillis(),
                "벤치마크를 위한 테스트용 자기소개서 내용입니다. ".repeat(10), // 100자 이상으로 만들기
                "IT",
                3,
                false
        );

        coverLetterService.saveCoverLetter(request, testUserEmail);
    }

    /**
     * 벤치마크 실행 메인 메서드
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== 자기소개서 서비스 성능 벤치마크 시작 ===");

        Options opt = new OptionsBuilder()
                .include(CoverLetterBenchmark.class.getSimpleName())
                .shouldFailOnError(true)
                .jvmArgs("-Xmx2g") // 메모리 충분히 할당
                .build();

        new Runner(opt).run();

        System.out.println("=== 벤치마크 완료! ===");
    }
}