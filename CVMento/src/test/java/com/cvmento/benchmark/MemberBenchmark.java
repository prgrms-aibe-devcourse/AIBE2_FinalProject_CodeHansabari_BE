package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Member 엔티티 전용 벤치마크
 * 실행시간: 약 1분
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)  // 짧게
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)  // 짧게
@Fork(0)
public class MemberBenchmark {

    private static ConfigurableApplicationContext context;
    private MemberRepository memberRepository;

    private final String testEmail = "user100@example.com";

    @Setup(Level.Trial)
    public void setupSpring() {
        log.info("=== Member 벤치마크 시작 ===");
        context = SpringApplication.run(CvMentoApplication.class,
                "--spring.profiles.active=test",
                "--logging.level.org.springframework=WARN"
        );
        memberRepository = context.getBean(MemberRepository.class);
    }

    @TearDown(Level.Trial)
    public void tearDownSpring() {
        if (context != null) context.close();
    }

    @Benchmark
    public Optional<Member> Email로_회원조회() {
        return memberRepository.findByEmail(testEmail);
    }

    @Benchmark
    public long 상태별_회원수() {
        return memberRepository.countByStatus(UserStatus.ACTIVE);
    }

    @Benchmark
    public long 역할별_회원수() {
        return memberRepository.countByRole(Role.USER);
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(MemberBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}