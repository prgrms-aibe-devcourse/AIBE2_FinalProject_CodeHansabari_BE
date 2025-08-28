package com.cvmento.global.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// ApiResponse → CommonResponse로 이름 변경
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final String errorCode;
    private final Boolean canRetry;
    private final LocalDateTime timestamp;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return CommonResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static CommonResponse<Void> success(String message) {
        return CommonResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> CommonResponse<T> error(String errorCode, String message) {
        return CommonResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> CommonResponse<T> error(String errorCode, String message, boolean canRetry) {
        return CommonResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .canRetry(canRetry)
                .timestamp(LocalDateTime.now())
                .build();
    }
}