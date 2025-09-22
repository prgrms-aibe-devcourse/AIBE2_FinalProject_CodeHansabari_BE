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

    private final String testEmail = "test@example.com";

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
     * 인덱스: member_id, status, updated_at
     */
    @Benchmark
    public Page<Resume> 사용자별_이력서목록() {
        return resumeRepository.findByMemberEmailAndStatusOrderByUpdatedAtDesc(
                testEmail, ResumeStatus.ACTIVE, PageRequest.of(0, 10));
    }

    /**
     * 사용자와 ID로 이력서 조회
     * 인덱스: member_id
     */
    @Benchmark
    public Optional<Resume> 사용자별_이력서조회() {
        Optional<Member> member = memberRepository.findByEmail(testEmail);
        if (member.isPresent()) {
            return resumeRepository.findByIdAndMember(1L, member.get());
        }
        return Optional.empty();
    }

    /**
     * 사용자+상태+ID로 이력서 조회
     * 인덱스: member_id, status 복합
     */
    @Benchmark
    public Optional<Resume> 사용자별_활성이력서조회() {
        Optional<Member> member = memberRepository.findByEmail(testEmail);
        if (member.isPresent()) {
            return resumeRepository.findByIdAndMemberAndStatus(1L, member.get(), ResumeStatus.ACTIVE);
        }
        return Optional.empty();
    }

    /**
     * 상태와 ID로 이력서 조회
     * 인덱스: status
     */
    @Benchmark
    public Optional<Resume> 상태별_이력서조회() {
        return resumeRepository.findByIdAndStatus(1L, ResumeStatus.ACTIVE);
    }

    /**
     * 상태와 수정시간으로 이력서 조회 (정리용)
     * 인덱스: status, updated_at 복합
     */
    @Benchmark
    public List<Resume> 오래된_삭제된이력서조회() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        return resumeRepository.findByStatusAndUpdatedAtBefore(ResumeStatus.DELETED, cutoff);
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