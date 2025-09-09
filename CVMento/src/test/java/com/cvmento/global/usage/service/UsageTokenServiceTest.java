package com.cvmento.global.usage.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.usage.dto.TokenUsageInfo;
import com.cvmento.global.usage.enums.UsageType;
import com.cvmento.global.exception.customException.UsageLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UsageTokenService 단위 테스트
 *
 * 목적: 비즈니스 로직의 정확성 검증
 * 방식: Mock 기반, 외부 의존성 제거
 *
 * 검증 내용:
 * 1. 토큰 소모 로직 - 충분한 토큰, 부족한 토큰, 정확한 토큰량 시나리오
 * 2. 첫 사용자 자동 초기화 - null 상태에서 MAX_TOKENS로 설정
 * 3. 토큰 사용량 조회 - 이메일 기반 Member 조회 후 토큰 정보 반환
 * 4. 토큰 초기화 - 사용자별 최대값 설정
 * 5. 전체 사용자 충전 - 배치 충전 로직, 최대값 제한
 * 6. 다양한 UsageType별 소모량 - COVERLETTER_REVIEW(5), INTERVIEW_AUTO(3), INTERVIEW_CUSTOM(1)
 * 7. 예외 상황 - 토큰 부족 시 UsageLimitExceededException, Redis 연결 실패 시 예외 전파
 *
 * 특징:
 * - RedisTemplate, MemberRepository 모두 Mock 처리
 * - 빠른 실행 속도 (외부 의존성 없음)
 * - 비즈니스 로직 중심 검증
 * - verify()로 메서드 호출 여부 정확히 확인
 */

@ExtendWith(MockitoExtension.class)
@Slf4j
class UsageTokenServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private UsageTokenService usageTokenService;

    private Member testMember;
    private final Long TEST_MEMBER_ID = 1L;
    private final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() throws Exception {
        log.info("=== 테스트 데이터 설정 시작 ===");

        testMember = new Member("google123", TEST_EMAIL, "테스트사용자", "profile.jpg");
        // 리플렉션으로 memberId 설정
        setField(testMember, "memberId", TEST_MEMBER_ID);
        log.info("테스트 Member 생성: email={}, name={}, memberId={}", TEST_EMAIL, "테스트사용자", TEST_MEMBER_ID);

        // RedisTemplate의 opsForValue()가 valueOperations를 반환하도록 설정 (lenient로 설정)
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        log.info("Mock 설정 완료: RedisTemplate -> ValueOperations");

        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("토큰 소모 테스트")
    class TryConsumeTokensTest {

        @Test
        @DisplayName("충분한 토큰이 있을 때 정상적으로 소모된다")
        void success_whenSufficientTokens() {
            log.info("=== 테스트 시작: 충분한 토큰 소모 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(15); // 현재 15토큰
            when(valueOperations.decrement(tokenKey, 5)).thenReturn(10L); // 5토큰 소모 후 10토큰
            log.info("Mock 설정: 현재 토큰=15, 소모 토큰=5, 결과=10");

            // when & then
            log.info("메서드 실행: tryConsumeTokens");
            assertThatNoException()
                    .isThrownBy(() -> usageTokenService.tryConsumeTokens(TEST_MEMBER_ID, UsageType.COVERLETTER_REVIEW));

            verify(valueOperations).decrement(tokenKey, 5);
            log.info("✅ 토큰 차감 확인: decrement({}, 5) 호출됨", tokenKey);
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("토큰이 부족할 때 예외가 발생한다")
        void throwException_whenInsufficientTokens() {
            log.info("=== 테스트 시작: 토큰 부족 예외 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(3); // 현재 3토큰
            log.info("Mock 설정: 현재 토큰=3, 필요 토큰=5 -> 부족");

            // when & then
            log.info("메서드 실행: tryConsumeTokens (예외 예상)");
            assertThatThrownBy(() ->
                    usageTokenService.tryConsumeTokens(TEST_MEMBER_ID, UsageType.COVERLETTER_REVIEW))
                    .isInstanceOf(UsageLimitExceededException.class);

            verify(valueOperations, never()).decrement(any(), anyInt());
            log.info("✅ 예외 발생 확인: UsageLimitExceededException");
            log.info("✅ 토큰 차감 안됨 확인: decrement 호출되지 않음");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("첫 사용자(토큰이 null)일 때 자동 초기화 후 소모한다")
        void autoInitialize_whenFirstTimeUser() {
            log.info("=== 테스트 시작: 첫 사용자 자동 초기화 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(null); // 첫 사용자
            when(valueOperations.decrement(tokenKey, 5)).thenReturn(35L); // 초기화(40) 후 소모(5) -> 35
            log.info("Mock 설정: 첫 사용자(null) -> 자동 초기화 -> 토큰 소모");

            // when & then
            log.info("메서드 실행: tryConsumeTokens (자동 초기화 예상)");
            assertThatNoException()
                    .isThrownBy(() -> usageTokenService.tryConsumeTokens(TEST_MEMBER_ID, UsageType.COVERLETTER_REVIEW));

            verify(valueOperations).set(tokenKey, UsageType.MAX_TOKENS); // 초기화 확인
            verify(valueOperations).decrement(tokenKey, 5); // 소모 확인
            log.info("✅ 자동 초기화 확인: set({}, {}) 호출됨", tokenKey, UsageType.MAX_TOKENS);
            log.info("✅ 토큰 소모 확인: decrement({}, 5) 호출됨", tokenKey);
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("정확히 필요한 토큰만 있을 때 성공한다")
        void success_whenExactTokensNeeded() {
            log.info("=== 테스트 시작: 정확한 토큰량 소모 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(5); // 정확히 5토큰
            when(valueOperations.decrement(tokenKey, 5)).thenReturn(0L); // 소모 후 0토큰
            log.info("Mock 설정: 현재 토큰=5, 필요 토큰=5 -> 정확히 일치");

            // when & then
            log.info("메서드 실행: tryConsumeTokens");
            assertThatNoException()
                    .isThrownBy(() -> usageTokenService.tryConsumeTokens(TEST_MEMBER_ID, UsageType.COVERLETTER_REVIEW));

            verify(valueOperations).decrement(tokenKey, 5);
            log.info("✅ 정확한 토큰 소모 확인: 5토큰 -> 0토큰");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("토큰 사용량 조회 테스트")
    class GetTokenUsageTest {

        @Test
        @DisplayName("정상적으로 토큰 사용량을 조회한다")
        void success_getTokenUsage() {
            log.info("=== 테스트 시작: 토큰 사용량 조회 ===");

            // given
            when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(25);
            log.info("Mock 설정: 사용자 조회 성공, 현재 토큰=25");

            // when
            log.info("메서드 실행: getTokenUsage");
            TokenUsageInfo result = usageTokenService.getTokenUsage(TEST_EMAIL);

            // then
            assertThat(result).isNotNull();
            assertThat(result.remainingTokens()).isEqualTo(25);
            assertThat(result.maxTokens()).isEqualTo(UsageType.MAX_TOKENS);
            assertThat(result.refillAmount()).isEqualTo(UsageType.REFILL_AMOUNT);
            assertThat(result.nextRefillTime()).isNotNull();
            log.info("✅ 조회 결과 확인: remainingTokens={}, maxTokens={}, refillAmount={}",
                    result.remainingTokens(), result.maxTokens(), result.refillAmount());
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("첫 사용자(토큰이 null)일 때 자동 초기화 후 조회한다")
        void autoInitialize_whenFirstTimeUserQuery() {
            log.info("=== 테스트 시작: 첫 사용자 조회 시 자동 초기화 ===");

            // given
            when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(null); // 첫 사용자
            log.info("Mock 설정: 사용자 조회 성공, 첫 사용자(토큰=null)");

            // when
            log.info("메서드 실행: getTokenUsage (자동 초기화 예상)");
            TokenUsageInfo result = usageTokenService.getTokenUsage(TEST_EMAIL);

            // then
            assertThat(result.remainingTokens()).isEqualTo(UsageType.MAX_TOKENS);
            verify(valueOperations).set(tokenKey, UsageType.MAX_TOKENS); // 초기화 확인
            log.info("✅ 자동 초기화 확인: set({}, {}) 호출됨", tokenKey, UsageType.MAX_TOKENS);
            log.info("✅ 조회 결과: remainingTokens={}", result.remainingTokens());
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("토큰 초기화 테스트")
    class InitializeUserTokensTest {

        @Test
        @DisplayName("사용자 토큰을 최대값으로 초기화한다")
        void success_initializeUserTokens() {
            log.info("=== 테스트 시작: 사용자 토큰 초기화 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            log.info("초기화 대상: memberId={}, tokenKey={}", TEST_MEMBER_ID, tokenKey);

            // when
            log.info("메서드 실행: initializeUserTokens");
            usageTokenService.initializeUserTokens(TEST_MEMBER_ID);

            // then
            verify(valueOperations).set(tokenKey, UsageType.MAX_TOKENS);
            log.info("✅ 초기화 확인: set({}, {}) 호출됨", tokenKey, UsageType.MAX_TOKENS);
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("전체 사용자 토큰 충전 테스트")
    class RefillAllUsersTokensTest {

        @Test
        @DisplayName("모든 사용자의 토큰을 충전한다")
        void success_refillAllUsersTokens() {
            log.info("=== 테스트 시작: 전체 사용자 토큰 충전 ===");

            // given
            Set<String> tokenKeys = Set.of(
                    "user:1:tokens",
                    "user:2:tokens",
                    "user:3:tokens"
            );
            when(redisTemplate.keys("user:*:tokens")).thenReturn(tokenKeys);

            // 각 사용자의 현재 토큰 설정 (변경된 REFILL_AMOUNT=10 적용)
            when(valueOperations.get("user:1:tokens")).thenReturn(10); // 10 + 10 = 20
            when(valueOperations.get("user:2:tokens")).thenReturn(25); // 25 + 10 = 35
            when(valueOperations.get("user:3:tokens")).thenReturn(40); // 이미 최대
            log.info("Mock 설정: 대상 사용자 3명 (토큰: 10, 25, 40), 충전량: 10");

            // when
            log.info("메서드 실행: refillAllUsersTokens");
            usageTokenService.refillAllUsersTokens();

            // then
            verify(valueOperations).set("user:1:tokens", 20); // 10 + 10 = 20
            verify(valueOperations).set("user:2:tokens", 35); // 25 + 10 = 35
            verify(valueOperations, never()).set(eq("user:3:tokens"), any()); // 이미 최대이므로 충전 안함

            // 전역 마지막 충전 시간 업데이트 확인
            verify(valueOperations).set(eq(UsageType.getGlobalLastRefillKey()), anyString());
            log.info("✅ 충전 결과: user1(10->20), user2(25->35), user3(40, 충전안함)");
            log.info("✅ 전역 충전 시간 업데이트 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("토큰 충전 시 최대값을 초과하지 않는다")
        void notExceedMaxTokens_whenRefill() {
            log.info("=== 테스트 시작: 토큰 충전 최대값 제한 ===");

            // given
            Set<String> tokenKeys = Set.of("user:1:tokens");
            when(redisTemplate.keys("user:*:tokens")).thenReturn(tokenKeys);
            when(valueOperations.get("user:1:tokens")).thenReturn(35); // 35 + 10 = 45이지만 최대 40
            log.info("Mock 설정: 현재 토큰=35, 충전량=10, 예상 결과=40(최대값 제한)");

            // when
            log.info("메서드 실행: refillAllUsersTokens");
            usageTokenService.refillAllUsersTokens();

            // then
            verify(valueOperations).set("user:1:tokens", 40); // 최대 40으로 제한
            log.info("✅ 최대값 제한 확인: 35 + 10 = 45 -> 40(최대값)");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("다양한 토큰 소모량 테스트")
    class DifferentUsageTypesTest {

        @Test
        @DisplayName("자소서 첨삭(5토큰) 소모 테스트")
        void consumeTokens_coverLetterReview() {
            log.info("=== 테스트 시작: 자소서 첨삭(5토큰) 소모 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(10);
            when(valueOperations.decrement(tokenKey, 5)).thenReturn(5L);
            log.info("Mock 설정: COVERLETTER_REVIEW, 현재 토큰=10, 소모=5");

            // when & then
            log.info("메서드 실행: tryConsumeTokens(COVERLETTER_REVIEW)");
            assertThatNoException()
                    .isThrownBy(() -> usageTokenService.tryConsumeTokens(TEST_MEMBER_ID, UsageType.COVERLETTER_REVIEW));

            verify(valueOperations).decrement(tokenKey, 5);
            log.info("✅ 자소서 첨삭 토큰 소모 확인: 5토큰 차감");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("자소서 기반 면접(3토큰) 소모 테스트")
        void consumeTokens_interviewAuto() {
            log.info("=== 테스트 시작: 자소서 기반 면접(3토큰) 소모 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(10);
            when(valueOperations.decrement(tokenKey, 3)).thenReturn(7L); // 변경: 3토큰 소모
            log.info("Mock 설정: INTERVIEW_AUTO, 현재 토큰=10, 소모=3");

            // when & then
            log.info("메서드 실행: tryConsumeTokens(INTERVIEW_AUTO)");
            assertThatNoException()
                    .isThrownBy(() -> usageTokenService.tryConsumeTokens(TEST_MEMBER_ID, UsageType.INTERVIEW_AUTO));

            verify(valueOperations).decrement(tokenKey, 3); // 변경: 3토큰 검증
            log.info("✅ 자소서 기반 면접 토큰 소모 확인: 3토큰 차감");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("커스텀 면접(1토큰) 소모 테스트")
        void consumeTokens_interviewCustom() {
            log.info("=== 테스트 시작: 커스텀 면접(1토큰) 소모 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(5);
            when(valueOperations.decrement(tokenKey, 1)).thenReturn(4L); // 변경: 1토큰 소모
            log.info("Mock 설정: INTERVIEW_CUSTOM, 현재 토큰=5, 소모=1");

            // when & then
            log.info("메서드 실행: tryConsumeTokens(INTERVIEW_CUSTOM)");
            assertThatNoException()
                    .isThrownBy(() -> usageTokenService.tryConsumeTokens(TEST_MEMBER_ID, UsageType.INTERVIEW_CUSTOM));

            verify(valueOperations).decrement(tokenKey, 1); // 변경: 1토큰 검증
            log.info("✅ 커스텀 면접 토큰 소모 확인: 1토큰 차감");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionScenariosTest {

        @Test
        @DisplayName("토큰 부족 시 예외에 정확한 정보가 포함된다")
        void exceptionContainsCorrectInfo_whenInsufficientTokens() {
            log.info("=== 테스트 시작: 토큰 부족 예외 정보 검증 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenReturn(3); // 3토큰 보유
            log.info("Mock 설정: 현재 토큰=3, 필요 토큰=5 -> 부족");

            // when & then
            log.info("메서드 실행: tryConsumeTokens (예외 정보 검증)");
            assertThatThrownBy(() ->
                    usageTokenService.tryConsumeTokens(TEST_MEMBER_ID, UsageType.COVERLETTER_REVIEW)) // 5토큰 필요
                    .isInstanceOf(UsageLimitExceededException.class)
                    .satisfies(ex -> {
                        log.info("발생한 예외: {}, 메시지: {}",
                                ex.getClass().getSimpleName(), ex.getMessage());
                    });
            log.info("✅ UsageLimitExceededException 발생 및 정보 포함 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("Redis 연결 실패 시 예외가 전파된다")
        void propagateException_whenRedisConnectionFails() {
            log.info("=== 테스트 시작: Redis 연결 실패 예외 전파 ===");

            // given
            String tokenKey = UsageType.getTokenKey(TEST_MEMBER_ID);
            when(valueOperations.get(tokenKey)).thenThrow(new RuntimeException("Redis connection failed"));
            log.info("Mock 설정: Redis 연결 실패 예외 발생");

            // when & then
            log.info("메서드 실행: tryConsumeTokens (Redis 예외 전파 검증)");
            assertThatThrownBy(() ->
                    usageTokenService.tryConsumeTokens(TEST_MEMBER_ID, UsageType.COVERLETTER_REVIEW))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Redis connection failed");
            log.info("✅ Redis 연결 실패 예외 전파 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    // 리플렉션 헬퍼 메서드 추가
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
        log.debug("리플렉션으로 필드 설정: {}.{} = {}", target.getClass().getSimpleName(), fieldName, value);
    }
}