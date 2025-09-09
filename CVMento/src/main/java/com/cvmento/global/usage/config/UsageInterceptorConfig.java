package com.cvmento.global.usage.config;

import com.cvmento.global.usage.interceptor.UsageLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 사용량 제한 인터셉터 설정
 */
@Configuration
@RequiredArgsConstructor
public class UsageInterceptorConfig implements WebMvcConfigurer {

    private final UsageLimitInterceptor usageLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(usageLimitInterceptor)
                .addPathPatterns(
                        "/api/v1/cover-letters/**",  // 자소서 관련
                        "/api/v1/me/cover-letters/{coverLetterId}/interview-questions/**",  // 면접 관련
                        "/api/v1/resumes/**"  // 이력서 관련
                );
    }
}