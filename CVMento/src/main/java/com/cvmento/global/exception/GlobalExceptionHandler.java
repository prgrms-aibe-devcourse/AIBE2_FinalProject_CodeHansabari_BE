package com.cvmento.global.exception;


import com.cvmento.global.exception.customException.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpServletRequest request,
            HttpStatus status,
            String errorCode,
            String message,
            Map<String, String> errors
    ) {
        request.setAttribute("businessErrorCode", errorCode);
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "errorCode", errorCode,
                "message", message,
                "errors", errors != null ? errors : Map.of()
        );
        return ResponseEntity.status(status).body(body);
    }

    // Validation 오류 핸들링
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "잘못된 입력입니다.",
                        (existing, replacement) -> existing
                ));

        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "입력값이 올바르지 않습니다.",
                fieldErrors
        );
    }

    // ===== Google OAuth 관련 예외 핸들러들 =====

    @ExceptionHandler(InvalidAuthorizationCodeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAuthorizationCodeException(
            InvalidAuthorizationCodeException ex, HttpServletRequest request) {
        log.warn("Invalid authorization code: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_AUTHORIZATION_CODE",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTokenException(
            InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Invalid token: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.UNAUTHORIZED,
                "INVALID_TOKEN",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(GoogleApiException.class)
    public ResponseEntity<Map<String, Object>> handleGoogleApiException(
            GoogleApiException ex, HttpServletRequest request) {
        log.error("Google API error: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                request,
                HttpStatus.BAD_GATEWAY,
                "GOOGLE_API_ERROR",
                ex.getMessage(),
                null
        );
    }

    // AI 자소서 서비스 관련 예외
    @ExceptionHandler(CoverLetterAiException.class)
    public ResponseEntity<Map<String, Object>> handleCoverLetterAiException(CoverLetterAiException ex, HttpServletRequest request) {
        log.error("CoverLetterAiException: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "AI_SERVICE_ERROR",
                ex.getMessage(),
                null
        );
    }

    // 회원을 찾을 수 없을 때
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMemberNotFoundException(MemberNotFoundException ex, HttpServletRequest request) {
        log.warn("MemberNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "MEMBER_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

    // 자소서 관련 예외
    @ExceptionHandler(CoverLetterException.class)
    public ResponseEntity<Map<String, Object>> handleCoverLetterException(CoverLetterException ex, HttpServletRequest request) {
        log.warn("CoverLetterException: {}", ex.getMessage());

        // "찾을 수 없습니다" 메시지면 404, 그 외는 400
        HttpStatus status = ex.getMessage().contains("찾을 수 없습니다") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

        String errorCode = status == HttpStatus.NOT_FOUND ?
                "COVER_LETTER_NOT_FOUND" : "COVER_LETTER_ERROR";

        return buildErrorResponse(
                request,
                status,
                errorCode,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InterviewException.class)
    public ResponseEntity<Map<String, Object>> handleInterviewException(InterviewException ex, HttpServletRequest request) {
        log.error("InterviewException: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERVIEW_SERVICE_ERROR",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InterviewLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleInterviewLimitExceededException(InterviewLimitExceededException ex, HttpServletRequest request) {
        log.error("InterviewLimitExceededException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.CONFLICT,
                "INTERVIEW_LIMIT_EXCEEDED",
                ex.getMessage(),
                null
        );
    }
  
    // ===== 이력서 관련 예외 핸들러들 =====
    @ExceptionHandler(ResumeException.class)
    public ResponseEntity<Map<String, Object>> handleResumeException(ResumeException ex, HttpServletRequest request) {
        log.warn("ResumeException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "RESUME_ERROR",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResumeNotFoundException(ResumeNotFoundException ex, HttpServletRequest request) {
        log.warn("ResumeNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "RESUME_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

  
    @ExceptionHandler(UsageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleUsageLimitExceededException(
            UsageLimitExceededException ex, HttpServletRequest request) {

        log.warn("사용량 제한 초과 - 기능: {}, 필요토큰: {}, 보유토큰: {}",
                ex.getUsageType().getDescription(), ex.getRequiredTokens(), ex.getRemainingTokens());

        // 추가 정보를 errors 맵에 포함
        Map<String, String> errors = Map.of(
                "usageType", ex.getUsageType().name(),
                "remainingTokens", String.valueOf(ex.getRemainingTokens()),
                "requiredTokens", String.valueOf(ex.getRequiredTokens()),
                "nextRefillTime", ex.getNextRefillTime().toString()
        );

        return buildErrorResponse(
                request,
                HttpStatus.TOO_MANY_REQUESTS, // 429 상태코드가 적절
                "USAGE_LIMIT_EXCEEDED",
                ex.getMessage(),
                errors
        );
    }

}