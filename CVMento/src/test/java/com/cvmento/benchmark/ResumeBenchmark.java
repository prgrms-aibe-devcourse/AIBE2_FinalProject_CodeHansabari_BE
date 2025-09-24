package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.enums.ResumeStatus;
import com.cvmento.domain.resume.repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Resume 엔티티 전용 벤치마크 (MySQL용)
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(0)
public class ResumeBenchmark {

    private static ConfigurableApplicationContext context;
    private ResumeRepository resumeRepository;
    private MemberRepository memberRepository;
    private Random random = new Random();

    // 이력서가 있는 회원들의 이메일 (member_id 1~2500)
    private final String[] testEmails = {
            "test@example.com",           // member_id: 1
            "user100@example.com",        // member_id: 100
            "user500@example.com",        // member_id: 500
            "user1000@example.com",       // member_id: 1000
            "user1500@example.com",       // member_id: 1500
            "user2000@example.com",       // member_id: 2000
            "user2500@example.com"        // member_id: 2500
    };

    @Setup(Level.Trial)
    public void setupSpring() {
        log.info("=== Resume 벤치마크 시작 (MySQL) ===");
        context = SpringApplication.run(CvMentoApplication.class,
                "--spring.profiles.active=test",
                "--logging.level.org.springframework=WARN"
        );
        resumeRepository = context.getBean(ResumeRepository.class);
        memberRepository = context.getBean(MemberRepository.class);

        // 데이터 확인
        long resumeCount = resumeRepository.count();
        log.info("총 이력서 수: {}", resumeCount);
    }

    @TearDown(Level.Trial)
    public void tearDownSpring() {
        if (context != null) context.close();
    }

    /**
     * 사용자별 이력서 목록 조회 (인덱스 성능 핵심 테스트)
     */
    @Benchmark
    public Page<Resume> getUserResumeList() {
        String email = testEmails[random.nextInt(testEmails.length)];
        return resumeRepository.findByMemberEmailAndStatusOrderByUpdatedAtDesc(
                email, ResumeStatus.ACTIVE, PageRequest.of(0, 10));
    }

    /**
     * 이력서 저장 성능 측정
     */
    @Benchmark
    public Resume saveResume() {
        String email = testEmails[random.nextInt(testEmails.length)];
        Optional<Member> member = memberRepository.findByEmail(email);

        if (member.isPresent()) {
            Resume resume = Resume.createResume(
                    "벤치마크 테스트 이력서 " + System.currentTimeMillis(),
                    com.cvmento.domain.resume.enums.ResumeType.DEFAULT,
                    "벤치마크 테스터",
                    "benchmark" + System.currentTimeMillis() + "@test.com",
                    1990 + random.nextInt(20),
                    "010-1234-5678",
                    com.cvmento.domain.resume.enums.CareerType.EXPERIENCED,
                    "백엔드 개발",
                    member.get()
            );
            return resumeRepository.save(resume);
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Resume 인덱스 성능 벤치마크 (MySQL) ===");

        Options opt = new OptionsBuilder()
                .include(ResumeBenchmark.class.getSimpleName())
                .result("resume-benchmark-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();

        System.out.println("=== Resume 벤치마크 완료 ===");
    }
}