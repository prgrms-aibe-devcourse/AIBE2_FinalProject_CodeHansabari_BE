package com.cvmento.benchmark;

import com.cvmento.CvMentoApplication;
import com.cvmento.domain.member.dto.response.MemberDetailResponse;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Member 엔티티 전용 벤치마크 (MySQL용)
 * 인덱스 성능 측정에 집중
 */
@Slf4j
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(0)
public class MemberBenchmark {

    private static ConfigurableApplicationContext context;
    private MemberRepository memberRepository;
    private MemberService memberService;
    private Random random = new Random();

    // 고유한 이메일들 - MySQL 데이터에 맞게 수정
    private final String[] testEmails = {
            "test@example.com",           // member_id: 1
            "user100@example.com",        // member_id: 100
            "user1000@example.com",       // member_id: 1000
            "user2000@example.com",       // member_id: 2000
            "user3000@example.com",       // member_id: 3000
            "admin4800@company.com",      // admin 계정
            "root4980@company.com"        // root 계정
    };

    @Setup(Level.Trial)
    public void setupSpring() {
        log.info("=== Member 벤치마크 시작 (MySQL) ===");
        context = SpringApplication.run(CvMentoApplication.class,
                "--spring.profiles.active=test",
                "--logging.level.org.springframework=WARN"
        );

        memberRepository = context.getBean(MemberRepository.class);
        memberService = context.getBean(MemberService.class);

        // 데이터 생성 확인
        long memberCount = memberRepository.count();
        log.info("총 회원 수: {}", memberCount);

        if (memberCount < 1000) {
            log.warn("충분한 데이터가 없습니다. SQL 스크립트가 실행되었는지 확인하세요.");
        }
    }

    @TearDown(Level.Trial)
    public void tearDownSpring() {
        if (context != null) context.close();
    }

    /**
     * 이메일 인덱스 성능 측정 (핵심 벤치마크)
     */
    @Benchmark
    public Optional<Member> Email로_회원조회() {
        String email = testEmails[random.nextInt(testEmails.length)];
        return memberRepository.findByEmail(email);
    }

//    /**
//     * 회원 상세 조회 (COUNT 쿼리 최적화 효과 측정)
//     */
//    @Benchmark
//    public MemberDetailResponse 회원상세조회_최적화버전() {
//        // 랜덤한 회원 ID 선택 (1~5000)
//        Long memberId = (long) (random.nextInt(5000) + 1);
//
//        try {
//            return memberService.getMemberDetail(memberId);
//        } catch (Exception e) {
//            // 존재하지 않는 회원 ID인 경우 1번 회원으로 fallback
//            return memberService.getMemberDetail(1L);
//        }
//    }


    public static void main(String[] args) throws Exception {
        log.info("=== Member 벤치마크 실행 (MySQL) ===");

        Options opt = new OptionsBuilder()
                .include(MemberBenchmark.class.getSimpleName())
                .result("member-benchmark-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();

        log.info("=== Member 벤치마크 완료 ===");
    }
}
