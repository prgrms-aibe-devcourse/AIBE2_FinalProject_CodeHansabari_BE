package com.cvmento.jwt;

import com.cvmento.redis.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    // 매 요청마다 실행되는 필터 로직
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {

        try {
            String token = resolveToken(request);

            if (token != null) {
                if (!jwtUtil.validateToken(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                    return;
                }

                String jti = jwtUtil.getJti(token); // JTI 추출
                if (redisService.isBlacklisted(jti)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                    return;
                }

                String username = jwtUtil.getUsername(token);
                String role = jwtUtil.getRole(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + e.getMessage());
        }
    }


    //Cookie에서 토큰을 추출하는 메서드
    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

}
