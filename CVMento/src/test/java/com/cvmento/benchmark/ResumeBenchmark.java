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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Resume 엔티티 전용 벤치마크
 * 실행시간: 약 1분
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

    private final String testEmail = "user100@example.com";

    @Setup(Level.Trial)
    public void setupSpring() {
        log.info("=== Resume 벤치마크 시작 ===");
        context = SpringApplication.run(CvMentoApplication.class,
                "--spring.profiles.active=test",
                "--logging.level.org.springframework=WARN"
        );
        resumeRepository = context.getBean(ResumeRepository.class);
        memberRepository = context.getBean(MemberRepository.class);
    }

    @TearDown(Level.Trial)
    public void tearDownSpring() {
        if (context != null) context.close();
    }

    /**
     * 사용자별 이력서 목록 조회 (페이징)
     * - 가장 중요한 사용자 시나리오
     * - 인덱스: member_email, status, updated_at
     */
    @Benchmark
    public Page<Resume> 사용자별_이력서목록() {
        return resumeRepository.findByMemberEmailAndStatusOrderByUpdatedAtDesc(
                testEmail, ResumeStatus.ACTIVE, PageRequest.of(0, 10));
    }

    /**
     * 사용자별 이력서 상세 조회
     * - 두 번째로 중요한 사용자 시나리오
     * - 인덱스: member_email, status
     */
    @Benchmark
    public Page<Resume> 사용자별_이력서상세조회() {
        return resumeRepository.findByMemberEmailAndStatusOrderByUpdatedAtDesc(
                testEmail, ResumeStatus.ACTIVE, PageRequest.of(0, 1));
    }

    /**
     * 이력서 저장(생성/수정)
     * - 세 번째로 중요한 사용자 시나리오
     * - 인덱스: member_email, status, updated_at (저장 시 인덱스 업데이트)
     */
    @Benchmark
    public Resume 이력서_저장() {
        Optional<Member> member = memberRepository.findByEmail(testEmail);
        if (member.isPresent()) {
            Resume resume = Resume.createResume(
                "벤치마크 테스트 이력서",
                com.cvmento.domain.resume.enums.ResumeType.DEFAULT,
                "홍길동",
                testEmail,
                1990,
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
        System.out.println("=== Resume 인덱스 성능 벤치마크 ===");

        Options opt = new OptionsBuilder()
                .include(ResumeBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();

        System.out.println("=== Resume 벤치마크 완료 ===");
    }
}