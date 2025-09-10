package com.cvmento.global.usage.annotation;

import com.cvmento.global.usage.enums.UsageType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 통합 토큰 사용을 요구하는 메서드에 붙이는 어노테이션
 */
@Target(ElementType.METHOD)  // 메서드에만 붙일 수 있음
@Retention(RetentionPolicy.RUNTIME)  // 런타임에 리플렉션으로 읽을 수 있음
public @interface RequireTokens {  // @interface로 어노테이션 정의

    /**
     * 사용량 타입 (토큰 소모량이 결정됨)
     * value 속성은 사용 시 생략 가능: @RequireTokens(UsageType.ESSAY_REVIEW)
     */
    UsageType value();  // 필수 속성, 메서드 형태로 정의
}