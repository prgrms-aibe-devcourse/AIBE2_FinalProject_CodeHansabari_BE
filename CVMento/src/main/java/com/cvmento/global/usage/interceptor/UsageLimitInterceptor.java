package com.cvmento.global.usage.interceptor;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.usage.annotation.RequireTokens;
import com.cvmento.global.usage.service.UsageTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 통합 토큰 사용량 제한 인터셉터 (고정 시점 충전 방식)
 * @RequireTokens 어노테이션이 붙은 메서드의 토큰 사용량을 체크합니다.
 * 토큰 충전은 스케줄러가 담당하므로 여기서는 소모만 처리합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UsageLimitInterceptor implements HandlerInterceptor {

    private final UsageTokenService usageTokenService;
    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // HandlerMethod가 아니면 통과 (정적 리소스 등)
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // @RequireTokens 어노테이션 체크
        RequireTokens requireTokens = handlerMethod.getMethodAnnotation(RequireTokens.class);
        if (requireTokens == null) {
            return true; // 어노테이션이 없으면 통과
        }

        // 스프링 시큐리티에서 이미 인증 체크했으므로 바로 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        try {
            // 사용자 이메일로 Member 조회
            String userEmail = userDetails.getUsername();
            Member member = memberRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));

            // 토큰 소모 시도 (충전 로직 없음, 스케줄러가 담당)
            usageTokenService.tryConsumeTokens(member.getMemberId(), requireTokens.value());

            log.info("토큰 소모 성공 - 사용자: {}, 기능: {}, 소모량: {}개, API: {}",
                    member.getMemberId(), requireTokens.value().getDescription(),
                    requireTokens.value().getCost(), request.getRequestURI());

            return true;

        } catch (Exception e) {
            log.error("토큰 사용량 체크 중 오류 발생", e);
            // UsageLimitExceededException은 GlobalExceptionHandler에서 처리하도록 던짐
            throw e;
        }
    }
}