package com.cvmento.global.exception;


import com.cvmento.global.exception.customException.*;
import com.cvmento.global.common.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MetricsService metricsService;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("VALIDATION_ERROR");

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

    @ExceptionHandler(InvalidAuthorizationCodeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAuthorizationCodeException(
            InvalidAuthorizationCodeException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("INVALID_AUTHORIZATION_CODE");

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
        metricsService.incrementErrorCount("INVALID_TOKEN");

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
        metricsService.incrementErrorCount("GOOGLE_API_ERROR");

        log.error("Google API error: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                request,
                HttpStatus.BAD_GATEWAY,
                "GOOGLE_API_ERROR",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("ACCESS_DENIED");

        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(SelfActionNotAllowedException.class)
    public ResponseEntity<Map<String, Object>> handleSelfActionNotAllowed(
            SelfActionNotAllowedException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("SELF_ACTION_NOT_ALLOWED");

        log.warn("SelfActionNotAllowedException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "SELF_ACTION_NOT_ALLOWED",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(CoverLetterAiException.class)
    public ResponseEntity<Map<String, Object>> handleCoverLetterAiException(CoverLetterAiException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("AI_SERVICE_ERROR");

        log.error("CoverLetterAiException: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "AI_SERVICE_ERROR",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMemberNotFoundException(MemberNotFoundException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("MEMBER_NOT_FOUND");

        log.warn("MemberNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "MEMBER_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(CoverLetterException.class)
    public ResponseEntity<Map<String, Object>> handleCoverLetterException(CoverLetterException ex, HttpServletRequest request) {
        // "찾을 수 없습니다" 메시지면 404, 그 외는 400
        HttpStatus status = ex.getMessage().contains("찾을 수 없습니다") ?
                HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

        String errorCode = status == HttpStatus.NOT_FOUND ?
                "COVER_LETTER_NOT_FOUND" : "COVER_LETTER_ERROR";

        metricsService.incrementErrorCount(errorCode);

        log.warn("CoverLetterException: {}", ex.getMessage());

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
        metricsService.incrementErrorCount("INTERVIEW_SERVICE_ERROR");

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
        metricsService.incrementErrorCount("INTERVIEW_LIMIT_EXCEEDED");

        log.error("InterviewLimitExceededException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.CONFLICT,
                "INTERVIEW_LIMIT_EXCEEDED",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(ResumeException.class)
    public ResponseEntity<Map<String, Object>> handleResumeException(ResumeException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("RESUME_ERROR");

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
        metricsService.incrementErrorCount("RESUME_NOT_FOUND");

        log.warn("ResumeNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "RESUME_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(LambdaException.class)
    public ResponseEntity<Map<String, Object>> handleLambdaException(LambdaException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("LAMBDA_SERVICE_ERROR");

        log.error("Lambda 서비스 오류: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "LAMBDA_SERVICE_ERROR",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(UsageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleUsageLimitExceededException(
            UsageLimitExceededException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("USAGE_LIMIT_EXCEEDED");

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

    // 크롤링 관련 예외
    @ExceptionHandler(CrawlCoverLetterNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCrawlCoverLetterNotFoundException(
            CrawlCoverLetterNotFoundException ex, HttpServletRequest request) {
        log.warn("CrawlCoverLetterNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "CRAWL_COVER_LETTER_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(CrawlCoverLetterException.class)
    public ResponseEntity<Map<String, Object>> handleCrawlCoverLetterException(
            CrawlCoverLetterException ex, HttpServletRequest request) {
        log.error("CrawlCoverLetterException: {}", ex.getMessage(), ex);
        
        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "CRAWLING_ERROR",
                ex.getMessage(),
                null
        );
    }

    // 특징 추출 관련 예외
    @ExceptionHandler(FeatureExtractionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFeatureExtractionNotFoundException(
            FeatureExtractionNotFoundException ex, HttpServletRequest request) {
        log.warn("FeatureExtractionNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "FEATURE_EXTRACTION_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(FeatureExtractionException.class)
    public ResponseEntity<Map<String, Object>> handleFeatureExtractionException(
            FeatureExtractionException ex, HttpServletRequest request) {
        log.error("FeatureExtractionException: {}", ex.getMessage(), ex);
        
        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "FEATURE_EXTRACTION_ERROR",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(AiInvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleAiInvalidRequestException(
            AiInvalidRequestException ex,
            HttpServletRequest request
    ) {
        metricsService.incrementErrorCount("AI_INVALID_REQUEST");

        log.warn("AiInvalidRequestException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.UNPROCESSABLE_ENTITY,  // 422
                "AI_INVALID_REQUEST",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedFileTypeException(
            UnsupportedFileTypeException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("UNSUPPORTED_FILE_TYPE");

        log.warn("UnsupportedFileTypeException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "UNSUPPORTED_FILE_TYPE",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileSizeExceededException(
            FileSizeExceededException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("FILE_SIZE_EXCEEDED");

        log.warn("FileSizeExceededException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "FILE_SIZE_EXCEEDED",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFileException(
            InvalidFileException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("INVALID_FILE");

        log.warn("InvalidFileException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_FILE",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(ResumeValidationException.class)
    public ResponseEntity<Map<String, Object>> handleResumeValidationException(
            ResumeValidationException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("RESUME_VALIDATION_ERROR");

        log.warn("ResumeValidationException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "RESUME_VALIDATION_ERROR",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(ResumeConversionException.class)
    public ResponseEntity<Map<String, Object>> handleResumeConversionException(
            ResumeConversionException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("RESUME_CONVERSION_ERROR");

        log.error("ResumeConversionException: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "RESUME_CONVERSION_ERROR",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStatusException(
            InvalidStatusException ex, HttpServletRequest request) {
        metricsService.incrementErrorCount("INVALID_STATUS");

        log.warn("InvalidStatusException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_STATUS",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidAnalysisStepException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAnalysisStepException(
            InvalidAnalysisStepException ex, HttpServletRequest request) {
        log.warn("InvalidAnalysisStepException: {}", ex.getMessage());
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_ANALYSIS_STEP",
                ex.getMessage(),
                null
        );
    }

}