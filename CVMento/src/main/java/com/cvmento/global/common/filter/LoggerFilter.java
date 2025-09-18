package com.cvmento.global.common.filter;

import com.cvmento.global.common.util.CookieUtil;
import com.cvmento.global.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class LoggerFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public LoggerFilter(JwtUtil jwtUtil, CookieUtil cookieUtil) {
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (uri.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        var req = new ContentCachingRequestWrapper(request);
        var res = new ContentCachingResponseWrapper(response);

        // 시작 시간 기록
        long startTime = System.currentTimeMillis();

        try {
            // 기본 MDC 설정
            String traceId = UUID.randomUUID().toString().substring(0, 8);
            String userEmail = extractUserEmailFromCookie(req);

            MDC.put("traceId", traceId);
            MDC.put("spanId", "filter");
            MDC.put("method", req.getMethod());
            MDC.put("uri", req.getRequestURI());
            MDC.put("userEmail", userEmail != null ? userEmail : "anonymous");

            // 실제 필터 체인 실행
            filterChain.doFilter(req, res);

        } catch (Exception e) {
            // 예외 발생시 로깅
            long errorTime = System.currentTimeMillis() - startTime;
            MDC.put("responseTime", errorTime + "ms");
            MDC.put("statusCode", "500");
            MDC.put("errorType", e.getClass().getSimpleName());

            log.error("Filter에서 예외 발생 - responseTime: {}ms, error: {}",
                    errorTime, e.getMessage(), e);
            throw e;

        } finally {
            // 응답 완료 후 추가 정보 설정
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            MDC.put("responseTime", responseTime + "ms");
            MDC.put("statusCode", String.valueOf(res.getStatus()));

            // 비즈니스 에러 코드 추출 (GlobalExceptionHandler에서 설정한 것)
            Object businessErrorCode = req.getAttribute("businessErrorCode");
            if (businessErrorCode != null) {
                MDC.put("businessError", businessErrorCode.toString());
            }

            // 로그 출력
            logRequestResponse(req, res, responseTime);

            // MDC 정리
            MDC.clear();
        }

        res.copyBodyToResponse();
    }

    private void logRequestResponse(ContentCachingRequestWrapper req,
                                    ContentCachingResponseWrapper res,
                                    long responseTime) {

        String method = req.getMethod();
        String uri = req.getRequestURI();
        int status = res.getStatus();

        String reqBody = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8).trim();
        String resBody = new String(res.getContentAsByteArray(), StandardCharsets.UTF_8).trim();

        // 상태 코드에 따른 로그 레벨 조정
        if (status >= 500) {
            log.error(
                    "\n=================  [ERROR RESPONSE] ================\n" +
                            "▶ Method : {} | URI : {} | Status : {} | Time : {}ms\n" +
                            "▶ Request Body  : {}\n" +
                            "◀ Response Body : {}\n" +
                            "===================================================",
                    method, uri, status, responseTime,
                    reqBody.isEmpty() ? "(no body)" : reqBody,
                    resBody.isEmpty() ? "(no body)" : resBody
            );
        } else if (status >= 400) {
            log.warn(
                    "\n=================  [CLIENT ERROR] ================\n" +
                            "▶ Method : {} | URI : {} | Status : {} | Time : {}ms\n" +
                            "▶ Request Body  : {}\n" +
                            "◀ Response Body : {}\n" +
                            "===================================================",
                    method, uri, status, responseTime,
                    reqBody.isEmpty() ? "(no body)" : reqBody,
                    resBody.isEmpty() ? "(no body)" : resBody
            );
        } else {
            log.info(
                    "\n=================  [SUCCESS] ================\n" +
                            "▶ Method : {} | URI : {} | Status : {} | Time : {}ms\n" +
                            "▶ Request Body  : {}\n" +
                            "◀ Response Body : {}\n" +
                            "==============================================",
                    method, uri, status, responseTime,
                    reqBody.isEmpty() ? "(no body)" : reqBody,
                    resBody.isEmpty() ? "(no body)" : resBody
            );
        }
    }

    private String extractUserEmailFromCookie(HttpServletRequest request) {
        try {
            Optional<String> tokenOpt = cookieUtil.getAccessTokenFromCookies(request);
            if (tokenOpt.isEmpty()) return null;

            String token = tokenOpt.get();
            if (jwtUtil.isValidToken(token) && jwtUtil.isAccessToken(token) && !jwtUtil.isTokenExpired(token)) {
                return jwtUtil.extractEmail(token);
            }
        } catch (Exception e) {
            log.debug("JWT에서 이메일 추출 실패: {}", e.getMessage());
        }
        return null;
    }
}