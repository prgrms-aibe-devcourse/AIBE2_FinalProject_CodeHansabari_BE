package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.resume.entity.TechStack;
import com.cvmento.domain.resume.repository.TechStackRepository;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * TechStack 엔티티 전용 벤치마크
 * 실행시간: 약 30초
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)  // 더 짧게
@Fork(0)
public class TechStackBenchmark {

    private static ConfigurableApplicationContext context;
    private TechStackRepository techStackRepository;

    @Setup(Level.Trial)
    public void setupSpring() {
        log.info("=== TechStack 벤치마크 시작 ===");
        context = SpringApplication.run(CvMentoApplication.class,
                "--spring.profiles.active=test",
                "--logging.level.org.springframework=WARN"
        );
        techStackRepository = context.getBean(TechStackRepository.class);
    }

    @TearDown(Level.Trial)
    public void tearDownSpring() {
        if (context != null) context.close();
    }

    @Benchmark
    public List<TechStack> 이름순_전체조회() {
        return techStackRepository.findAllByOrderByNameAsc();
    }

    @Benchmark
    public Optional<TechStack> 이름으로_검색() {
        return techStackRepository.findByName("Java");
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(TechStackBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}