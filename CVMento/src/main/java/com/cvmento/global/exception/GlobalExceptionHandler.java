package com.cvmento.global.exception;


import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.cvmento.global.exception.customException.CoverLetterException;
import com.cvmento.global.exception.customException.MemberNotFoundException;
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
        log.error("CoverLetterException: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "COVER_LETTER_ERROR",
                ex.getMessage(),
                null
        );
    }
}