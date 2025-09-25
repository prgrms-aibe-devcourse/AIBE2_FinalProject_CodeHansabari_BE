package com.cvmento.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(100)
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.internal.api-key}") // application.yml에서 설정
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // /api/internal/** 경로에만 적용
        if (requestPath.startsWith("/api/internal/")) {
            String apiKey = request.getHeader("X-API-Key");

            if (apiKey == null || !apiKey.equals(internalApiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");

                String jsonResponse = String.format(
                        "{\"success\":false,\"errorCode\":\"INVALID_API_KEY\",\"message\":\"유효하지 않은 API Key입니다.\",\"timestamp\":%d}",
                        System.currentTimeMillis()
                );

                response.getWriter().write(jsonResponse);
                return;
            }

            // 유효한 API Key면 인증된 상태로 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            "internal-service",
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}