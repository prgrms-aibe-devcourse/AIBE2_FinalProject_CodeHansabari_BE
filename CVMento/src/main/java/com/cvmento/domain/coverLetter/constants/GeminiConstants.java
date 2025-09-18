package com.cvmento.domain.coverLetter.constants;

/**
 * Gemini API 관련 상수 정의
 * - Gemini 2.5 Flash 모델 설정
 * - 사고 예산(Thinking Budget) 설정
 * - 공통 모델명 및 설정값
 */
public class GeminiConstants {
    
    // 공통 모델명
    public static final String GEMINI_MODEL = "gemini-2.5-flash";
    
    // Gemini 2.5 Flash 사고 예산 설정 상수
    public static final String THINKING_BUDGET_LOW = "500";      // 빠른 응답, 기본적인 추론
    public static final String THINKING_BUDGET_MEDIUM = "1000";  // 균형잡힌 추론 (기본값)
    public static final String THINKING_BUDGET_HIGH = "2000";    // 깊은 추론, 복잡한 분석
    
    // 특징 추출 관련 상수
    public static final int FEATURES_PER_COVER_LETTER = 3;       // 자소서당 특징 개수
    
    private GeminiConstants() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }
}

