package com.cvmento.global.usage.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import com.cvmento.global.usage.dto.TokenUsageInfo;
import com.cvmento.global.usage.enums.UsageType;
import com.cvmento.global.exception.customException.UsageLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 통합 토큰 사용량 관리 서비스 (고정 충전 시점)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UsageTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberRepository memberRepository;

    /**
     * 토큰 소모 시도
     *
     * @param memberId    사용자 ID
     * @param usageType 사용량 타입 (소모량이 결정됨)
     * @throws UsageLimitExceededException 토큰 부족 시
     */
    public void tryConsumeTokens(Long memberId, UsageType usageType) {
        MDC.put("spanId", "token-consumption-service");

        // 사용자 토큰 키 생성
        String tokenKey = UsageType.getTokenKey(memberId);

        MDC.put("spanId", "redis-token-read");
        // 현재 토큰 조회
        Integer currentTokens = (Integer) redisTemplate.opsForValue().get(tokenKey);

        MDC.put("spanId", "token-consumption-service");
        // 첫 사용자 체크 (안전장치)
        if (currentTokens == null) {
            // 첫 사용자면 최대 토큰으로 초기화
            initializeUserTokens(memberId);
            currentTokens = UsageType.MAX_TOKENS;
        }

        // 필요 토큰 계산
        int requiredTokens = usageType.getCost();

        // 토큰 부족 체크
        if (currentTokens < requiredTokens) {
            LocalDateTime nextRefillTime = UsageType.getNextRefillTime();
            log.warn("토큰 부족 - 사용자: {}, 현재: {}개, 필요: {}개",
                    memberId, currentTokens, requiredTokens);
            throw new UsageLimitExceededException(usageType, currentTokens, requiredTokens, nextRefillTime);
        }

        MDC.put("spanId", "redis-token-write");
        // 토큰 차감
        Long remaining = redisTemplate.opsForValue().decrement(tokenKey, requiredTokens);

        MDC.put("spanId", "token-consumption-service");
        log.info("토큰 소모 완료 - 사용자: {}, 기능: {}, 소모량: {}, 남은량: {}",
                memberId, usageType.getDescription(), requiredTokens, remaining);
    }

    /**
     * 사용자의 토큰 사용량 조회 (이메일 기반)
     */
    public TokenUsageInfo getTokenUsage(String userEmail) {
        MDC.put("spanId", "token-usage-service");

        // 이메일로 Member 조회 (캐시 적용)
        Member member = findMemberByEmailCached(userEmail);

        MDC.put("spanId", "token-usage-service");
        // 사용자 토큰 키 생성
        String tokenKey = UsageType.getTokenKey(member.getMemberId());

        MDC.put("spanId", "redis-token-read");
        // 현재 토큰 조회
        Integer currentTokens = (Integer) redisTemplate.opsForValue().get(tokenKey);

        MDC.put("spanId", "token-usage-service");
        // 첫 사용자 체크 (안전장치)
        if (currentTokens == null) {
            initializeUserTokens(member.getMemberId());
            currentTokens = UsageType.MAX_TOKENS;
        }

        log.info("토큰 사용량 조회 완료 - 사용자: {}, 남은토큰: {}/{}",
                member.getMemberId(), currentTokens, UsageType.MAX_TOKENS);

        // 토큰 정보 반환
        return new TokenUsageInfo(
                currentTokens,
                UsageType.MAX_TOKENS,
                UsageType.getNextRefillTime(),
                UsageType.REFILL_AMOUNT
        );
    }

    /**
     * 사용자 토큰 초기화 (신규 가입자 또는 서버 시작 시)
     */
    public void initializeUserTokens(Long memberId) {
        MDC.put("spanId", "token-initialization-service");

        // 사용자 토큰 키 생성
        String tokenKey = UsageType.getTokenKey(memberId);

        MDC.put("spanId", "redis-token-write");
        // 최대 토큰으로 설정
        redisTemplate.opsForValue().set(tokenKey, UsageType.MAX_TOKENS);

        MDC.put("spanId", "token-initialization-service");
        log.info("사용자 토큰 초기화 완료 - 사용자 ID: {}, 토큰: {}개", memberId, UsageType.MAX_TOKENS);
    }

    /**
     * 전체 사용자 토큰 충전 (스케줄러 전용)
     * 고정 시점(매 2시간)에 모든 사용자에게 토큰 충전
     */
    public void refillAllUsersTokens() {
        MDC.put("spanId", "token-refill-service");

        try {
            log.info("전체 사용자 토큰 충전 시작 - 시간: {}", LocalDateTime.now());

            MDC.put("spanId", "redis-keys-scan");
            // 모든 사용자 토큰 키 검색
            String pattern = "user:*:tokens";
            var tokenKeys = redisTemplate.keys(pattern);

            MDC.put("spanId", "token-refill-service");
            if (tokenKeys.isEmpty()) {
                log.info("충전할 사용자 토큰이 없습니다.");
                return;
            }

            int processedCount = 0;
            for (String tokenKey : tokenKeys) {
                try {
                    MDC.put("spanId", "redis-token-read");
                    // 현재 토큰 조회
                    Integer currentTokens = (Integer) redisTemplate.opsForValue().get(tokenKey);

                    MDC.put("spanId", "token-refill-service");
                    // 토큰이 null이 아니고 최대치 미만인 경우에만 충전
                    if (currentTokens != null && currentTokens < UsageType.MAX_TOKENS) {
                        // 충전 후 토큰 계산
                        int newTokenCount = Math.min(currentTokens + UsageType.REFILL_AMOUNT, UsageType.MAX_TOKENS);

                        MDC.put("spanId", "redis-token-write");
                        redisTemplate.opsForValue().set(tokenKey, newTokenCount);

                        MDC.put("spanId", "token-refill-service");
                        // 사용자 ID 추출해서 로깅
                        String[] parts = tokenKey.split(":");
                        if (parts.length >= 2) {
                            log.debug("토큰 충전 완료 - 사용자: {}, {}개 → {}개",
                                    parts[1], currentTokens, newTokenCount);
                        }
                        processedCount++;
                    }
                } catch (Exception e) {
                    log.warn("토큰 충전 실패 - 키: {}, 에러: {}", tokenKey, e.getMessage());
                }
            }

            MDC.put("spanId", "redis-global-update");
            // 전역 마지막 충전 시간 업데이트
            redisTemplate.opsForValue().set(UsageType.getGlobalLastRefillKey(),
                    LocalDateTime.now().toString());

            MDC.put("spanId", "token-refill-service");
            log.info("전체 사용자 토큰 충전 완료 - 처리된 사용자: {}명", processedCount);

        } catch (Exception e) {
            log.error("전체 토큰 충전 중 오류 발생", e);
        }
    }

    /**
     * 캐시 적용 버전
     */
    @Cacheable(value = "memberCache", key = "#email")
    public Member findMemberByEmailCached(String email) {
        MDC.put("spanId", "member-repository");
        log.info("🔥 DB 조회 (사용량토큰 캐시 적용): {}", email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다"));
        MDC.put("spanId", "token-usage-service");
        return member;
    }

    /**
     * 캐시 미적용 버전 (벤치마크 비교용)
     */
    public Member findMemberByEmailNoCache(String email) {
        MDC.put("spanId", "member-repository");
        log.info("🔥 DB 조회 (사용량토큰 캐시 미적용): {}", email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다"));
        MDC.put("spanId", "token-usage-service");
        return member;
    }

    /**
     * 벤치마크 테스트용 public 메서드
     */
    public Member findMemberByEmailForBenchmark(String email) {
        return findMemberByEmailCached(email);
    }
}
