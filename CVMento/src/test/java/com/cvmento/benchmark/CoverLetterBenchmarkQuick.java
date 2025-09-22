package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * CoverLetter 엔티티 전용 벤치마크
 * 실행시간: 약 1분
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(0)
public class CoverLetterBenchmarkQuick {

    private static ConfigurableApplicationContext context;
    private CoverLetterRepository coverLetterRepository;
    private MemberRepository memberRepository;

    private final String testEmail = "test@example.com";

    @Setup(Level.Trial)
    public void setupSpring() {
        log.info("=== CoverLetter 벤치마크 시작 ===");
        context = SpringApplication.run(CvMentoApplication.class,
                "--spring.profiles.active=test",
                "--logging.level.org.springframework=WARN"
        );
        coverLetterRepository = context.getBean(CoverLetterRepository.class);
        memberRepository = context.getBean(MemberRepository.class);
    }

    @TearDown(Level.Trial)
    public void tearDownSpring() {
        if (context != null) context.close();
    }

    @Benchmark
    public Page<CoverLetter> 사용자별_자소서목록() {
        Optional<Member> member = memberRepository.findByEmail(testEmail);
        if (member.isPresent()) {
            return coverLetterRepository.findByMemberAndStatusOrderByUpdatedAtDesc(
                    member.get(), CoverLetterStatus.ACTIVE, PageRequest.of(0, 10));
        }
        return Page.empty();
    }

    @Benchmark
    public List<CoverLetter> 전체_자소서목록() {
        return coverLetterRepository.findAllByOrderByUpdatedAtDesc();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(CoverLetterBenchmarkQuick.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}