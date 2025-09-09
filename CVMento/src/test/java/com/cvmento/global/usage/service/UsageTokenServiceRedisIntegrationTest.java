package com.cvmento.global.usage.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.usage.dto.TokenUsageInfo;
import com.cvmento.global.usage.enums.UsageType;
import com.cvmento.global.exception.customException.UsageLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * UsageTokenService Redis 통합 테스트
 *
 * 목적: 실제 Redis와의 연동 안정성 및 데이터 일관성 검증
 * 방식: 실제 Redis 인스턴스 사용, 진짜 데이터 저장/조회
 *
 * 검증 내용:
 * 1. 기본 Redis 연동 - 실제 저장, 조회, 소모 동작 확인
 * 2. 동시성 테스트 - 멀티스레드 환경에서 Redis 원자적 연산(DECRBY) 검증
 * 3. 사용자 격리 - 여러 사용자가 독립적으로 토큰 관리되는지 확인
 * 4. 전체 사용자 충전 - 실제 Redis keys 패턴 검색 및 배치 업데이트
 * 5. 경계값 처리 - 0토큰, 정확한 토큰량에서의 동작
 * 6. 실제 사용자 여정 - 가입 → 사용 → 충전 → 재사용 전체 플로우
 *
 * 특징:
 * - @SpringBootTest로 실제 컨텍스트 로딩
 * - 개발용 Redis 서버 직접 사용
 * - @AfterEach에서 테스트 키 정리 (user:*, global:*)
 * - 동시성 안전성 검증 (Race Condition 방지)
 * - 실제 운영 환경과 동일한 조건에서 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Slf4j
class UsageTokenServiceRedisIntegrationTest {

    @Autowired
    private UsageTokenService usageTokenService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private Member testMember;
    private final String TEST_EMAIL = "redis-test@example.com";

    @BeforeEach
    void setUp() throws Exception {
        log.info("=== Redis 통합 테스트 설정 시작 ===");

        // 테스트 사용자 생성
        testMember = new Member("google-redis-test", TEST_EMAIL, "Redis통합테스트사용자", "profile.jpg");
        testMember = memberRepository.save(testMember);
        log.info("테스트 Member 생성: email={}, memberId={}", TEST_EMAIL, testMember.getMemberId());

        log.info("=== Redis 통합 테스트 설정 완료 ===\n");
    }

    @AfterEach
    void tearDown() {
        log.info("=== Redis 테스트 데이터 정리 시작 ===");

        // user:로 시작하는 모든 키 삭제
        Set<String> userKeys = redisTemplate.keys("user:*");
        Set<String> globalKeys = redisTemplate.keys("global:*");

        if (userKeys != null && !userKeys.isEmpty()) {
            redisTemplate.delete(userKeys);
            log.info("사용자 키 정리 완료: {}개", userKeys.size());
        }

        if (globalKeys != null && !globalKeys.isEmpty()) {
            redisTemplate.delete(globalKeys);
            log.info("전역 키 정리 완료: {}개", globalKeys.size());
        }

        log.info("=== Redis 테스트 데이터 정리 완료 ===\n");
    }

    @Nested
    @DisplayName("실제 Redis 기본 연동 테스트")
    class BasicRedisOperationTest {

        @Test
        @DisplayName("실제 Redis에 토큰 초기화가 정상 작동한다")
        void success_initializeTokensInRealRedis() {
            log.info("=== 테스트 시작: 실제 Redis 토큰 초기화 ===");

            // when
            usageTokenService.initializeUserTokens(testMember.getMemberId());
            log.info("토큰 초기화 실행 완료");

            // then
            String tokenKey = UsageType.getTokenKey(testMember.getMemberId());
            Integer storedTokens = (Integer) redisTemplate.opsForValue().get(tokenKey);

            assertThat(storedTokens).isNotNull();
            assertThat(storedTokens).isEqualTo(UsageType.MAX_TOKENS);
            log.info("✅ Redis 저장 확인: key={}, value={}", tokenKey, storedTokens);
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("실제 Redis에서 토큰 소모가 정상 작동한다")
        void success_consumeTokensInRealRedis() {
            log.info("=== 테스트 시작: 실제 Redis 토큰 소모 ===");

            // given
            usageTokenService.initializeUserTokens(testMember.getMemberId());
            String tokenKey = UsageType.getTokenKey(testMember.getMemberId());
            log.info("초기 토큰 설정 완료: {}개", UsageType.MAX_TOKENS);

            // when
            usageTokenService.tryConsumeTokens(testMember.getMemberId(), UsageType.ESSAY_REVIEW);
            log.info("토큰 소모 실행: ESSAY_REVIEW (5토큰)");

            // then
            Integer remainingTokens = (Integer) redisTemplate.opsForValue().get(tokenKey);
            assertThat(remainingTokens).isEqualTo(35); // 40 - 5 = 35
            log.info("✅ 토큰 소모 확인: 40 -> 35 (5토큰 차감)");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("실제 Redis에서 토큰 조회가 정상 작동한다")
        void success_getTokenUsageFromRealRedis() {
            log.info("=== 테스트 시작: 실제 Redis 토큰 조회 ===");

            // given
            usageTokenService.initializeUserTokens(testMember.getMemberId());
            usageTokenService.tryConsumeTokens(testMember.getMemberId(), UsageType.INTERVIEW_AUTO); // 3토큰 소모
            log.info("토큰 초기화 및 소모 완료: 40 - 3 = 37");

            // when
            TokenUsageInfo result = usageTokenService.getTokenUsage(TEST_EMAIL);

            // then
            assertThat(result.remainingTokens()).isEqualTo(37);
            assertThat(result.maxTokens()).isEqualTo(40);
            assertThat(result.refillAmount()).isEqualTo(10);
            log.info("✅ 조회 결과 확인: remainingTokens={}, maxTokens={}, refillAmount={}",
                    result.remainingTokens(), result.maxTokens(), result.refillAmount());
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("실제 Redis 동시성 테스트")
    class ConcurrencyTest {

        @Test
        @DisplayName("멀티스레드 환경에서 토큰 소모가 정확히 처리된다")
        void success_concurrentTokenConsumption() throws InterruptedException {
            log.info("=== 테스트 시작: 멀티스레드 토큰 소모 ===");

            // given
            usageTokenService.initializeUserTokens(testMember.getMemberId());
            int threadCount = 20;
            UsageType usageType = UsageType.INTERVIEW_CUSTOM; // 1토큰씩 소모

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            log.info("동시성 테스트 설정: {}개 스레드, {}토큰씩 소모", threadCount, usageType.getCost());

            // when
            for (int i = 0; i < threadCount; i++) {
                final int threadNum = i;
                executorService.submit(() -> {
                    try {
                        usageTokenService.tryConsumeTokens(testMember.getMemberId(), usageType);
                        successCount.incrementAndGet();
                        log.debug("스레드 {} 성공", threadNum);
                    } catch (UsageLimitExceededException e) {
                        failCount.incrementAndGet();
                        log.debug("스레드 {} 실패: 토큰 부족", threadNum);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            String tokenKey = UsageType.getTokenKey(testMember.getMemberId());
            Integer finalTokens = (Integer) redisTemplate.opsForValue().get(tokenKey);
            int expectedFinalTokens = UsageType.MAX_TOKENS - (successCount.get() * usageType.getCost());

            assertThat(finalTokens).isEqualTo(expectedFinalTokens);
            assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);

            log.info("✅ 동시성 테스트 결과:");
            log.info("  - 성공한 스레드: {}개", successCount.get());
            log.info("  - 실패한 스레드: {}개", failCount.get());
            log.info("  - 최종 토큰: {}개 (예상: {}개)", finalTokens, expectedFinalTokens);
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("동시에 여러 사용자가 토큰을 사용해도 격리된다")
        void success_multiUserConcurrency() throws Exception {
            log.info("=== 테스트 시작: 다중 사용자 동시 토큰 사용 ===");

            // given
            Member user2 = new Member("google-test-2", "test2@example.com", "사용자2", "profile2.jpg");
            user2 = memberRepository.save(user2);

            usageTokenService.initializeUserTokens(testMember.getMemberId());
            usageTokenService.initializeUserTokens(user2.getMemberId());
            log.info("2명 사용자 토큰 초기화 완료");

            // when
            usageTokenService.tryConsumeTokens(testMember.getMemberId(), UsageType.ESSAY_REVIEW); // 5토큰
            usageTokenService.tryConsumeTokens(user2.getMemberId(), UsageType.INTERVIEW_AUTO); // 3토큰

            // then
            String tokenKey1 = UsageType.getTokenKey(testMember.getMemberId());
            String tokenKey2 = UsageType.getTokenKey(user2.getMemberId());

            Integer user1Tokens = (Integer) redisTemplate.opsForValue().get(tokenKey1);
            Integer user2Tokens = (Integer) redisTemplate.opsForValue().get(tokenKey2);

            assertThat(user1Tokens).isEqualTo(35); // 40 - 5
            assertThat(user2Tokens).isEqualTo(37); // 40 - 3

            log.info("✅ 사용자별 독립적 토큰 관리 확인:");
            log.info("  - 사용자1: {}토큰 (40-5)", user1Tokens);
            log.info("  - 사용자2: {}토큰 (40-3)", user2Tokens);
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("실제 Redis 전체 사용자 충전 테스트")
    class RefillAllUsersTest {

        @Test
        @DisplayName("실제 Redis에서 전체 사용자 토큰 충전이 정상 작동한다")
        void success_refillAllUsersInRealRedis() throws Exception {
            log.info("=== 테스트 시작: 실제 Redis 전체 사용자 충전 ===");

            // given
            Member user2 = new Member("google-refill-test", "refill-test@example.com", "충전테스트", "profile.jpg");
            user2 = memberRepository.save(user2);

            // 초기화 후 일부 소모
            usageTokenService.initializeUserTokens(testMember.getMemberId());
            usageTokenService.initializeUserTokens(user2.getMemberId());

            usageTokenService.tryConsumeTokens(testMember.getMemberId(), UsageType.INTERVIEW_AUTO); // 3토큰 소모 -> 37
            usageTokenService.tryConsumeTokens(user2.getMemberId(), UsageType.ESSAY_REVIEW); // 5토큰 소모 -> 35

            log.info("초기 설정 완료: user1=37토큰, user2=35토큰");

            // when
            usageTokenService.refillAllUsersTokens();
            log.info("전체 사용자 토큰 충전 실행");

            // then
            String tokenKey1 = UsageType.getTokenKey(testMember.getMemberId());
            String tokenKey2 = UsageType.getTokenKey(user2.getMemberId());

            Integer user1FinalTokens = (Integer) redisTemplate.opsForValue().get(tokenKey1);
            Integer user2FinalTokens = (Integer) redisTemplate.opsForValue().get(tokenKey2);

            assertThat(user1FinalTokens).isEqualTo(40); // 37 + 10 = 47이지만 최대 40
            assertThat(user2FinalTokens).isEqualTo(40); // 35 + 10 = 45이지만 최대 40

            log.info("✅ 전체 충전 결과 확인:");
            log.info("  - 사용자1: 37 + 10 = 47 -> 40 (최대값 제한)");
            log.info("  - 사용자2: 35 + 10 = 45 -> 40 (최대값 제한)");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("실제 Redis 경계값 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("토큰이 0개일 때 추가 소모 시도 시 예외 발생")
        void throwException_whenZeroTokens() {
            log.info("=== 테스트 시작: 0토큰 상태에서 소모 시도 ===");

            // given
            String tokenKey = UsageType.getTokenKey(testMember.getMemberId());
            redisTemplate.opsForValue().set(tokenKey, 0); // 직접 0으로 설정
            log.info("토큰을 0개로 설정");

            // when & then
            assertThatThrownBy(() ->
                    usageTokenService.tryConsumeTokens(testMember.getMemberId(), UsageType.INTERVIEW_CUSTOM))
                    .isInstanceOf(UsageLimitExceededException.class);

            // 토큰이 차감되지 않았는지 확인
            Integer remainingTokens = (Integer) redisTemplate.opsForValue().get(tokenKey);
            assertThat(remainingTokens).isEqualTo(0);

            log.info("✅ 0토큰 상태에서 소모 시도 -> 예외 발생, 토큰 변화 없음");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("정확히 필요한 토큰만 있을 때 소모 성공")
        void success_whenExactTokensAvailable() {
            log.info("=== 테스트 시작: 정확한 토큰량 소모 ===");

            // given
            String tokenKey = UsageType.getTokenKey(testMember.getMemberId());
            UsageType usageType = UsageType.ESSAY_REVIEW; // 5토큰 필요
            redisTemplate.opsForValue().set(tokenKey, usageType.getCost()); // 정확히 5토큰 설정
            log.info("토큰을 정확히 {}개로 설정", usageType.getCost());

            // when
            usageTokenService.tryConsumeTokens(testMember.getMemberId(), usageType);
            log.info("토큰 소모 실행");

            // then
            Integer remainingTokens = (Integer) redisTemplate.opsForValue().get(tokenKey);
            assertThat(remainingTokens).isEqualTo(0);

            log.info("✅ 정확한 토큰 소모 성공: {} -> 0", usageType.getCost());
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("연속 시나리오 테스트")
    class SequentialScenarioTest {

        @Test
        @DisplayName("실제 사용자 여정: 가입 -> 사용 -> 충전 -> 다시 사용")
        void success_realUserJourney() {
            log.info("=== 테스트 시작: 실제 사용자 여정 시뮬레이션 ===");

            // 1. 신규 가입 (토큰 초기화)
            usageTokenService.initializeUserTokens(testMember.getMemberId());
            TokenUsageInfo step1 = usageTokenService.getTokenUsage(TEST_EMAIL);
            assertThat(step1.remainingTokens()).isEqualTo(40);
            log.info("1단계 - 신규 가입: {}토큰 초기화", step1.remainingTokens());

            // 2. 여러 기능 사용
            usageTokenService.tryConsumeTokens(testMember.getMemberId(), UsageType.ESSAY_REVIEW); // 5토큰
            usageTokenService.tryConsumeTokens(testMember.getMemberId(), UsageType.INTERVIEW_AUTO); // 3토큰
            usageTokenService.tryConsumeTokens(testMember.getMemberId(), UsageType.INTERVIEW_CUSTOM); // 1토큰

            TokenUsageInfo step2 = usageTokenService.getTokenUsage(TEST_EMAIL);
            assertThat(step2.remainingTokens()).isEqualTo(31); // 40 - 5 - 3 - 1
            log.info("2단계 - 기능 사용: 40 -> {}토큰 (9토큰 소모)", step2.remainingTokens());

            // 3. 토큰 충전
            usageTokenService.refillAllUsersTokens();
            TokenUsageInfo step3 = usageTokenService.getTokenUsage(TEST_EMAIL);
            assertThat(step3.remainingTokens()).isEqualTo(40); // 31 + 10 = 41이지만 최대 40
            log.info("3단계 - 토큰 충전: 31 + 10 = 41 -> {}토큰 (최대값 제한)", step3.remainingTokens());

            // 4. 다시 사용
            usageTokenService.tryConsumeTokens(testMember.getMemberId(), UsageType.ESSAY_REVIEW); // 5토큰
            TokenUsageInfo step4 = usageTokenService.getTokenUsage(TEST_EMAIL);
            assertThat(step4.remainingTokens()).isEqualTo(35);
            log.info("4단계 - 추가 사용: 40 -> {}토큰 (5토큰 소모)", step4.remainingTokens());

            log.info("✅ 전체 사용자 여정 시뮬레이션 성공");
            log.info("=== 테스트 완료 ===\n");
        }
    }
}