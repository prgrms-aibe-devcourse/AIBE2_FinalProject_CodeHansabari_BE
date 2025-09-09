package com.cvmento.domain.resume.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("이력서 AI 검증 서비스 테스트")
class ResumeAiValidationServiceTest {

    private ResumeAiValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ResumeAiValidationService();
    }

    @Test
    @DisplayName("유효한 LLM 응답 검증 - 성공")
    void isValidLlmResponse_Success() {
        // Given
        String validResponse = "이것은 충분히 긴 유효한 LLM 응답입니다. 이력서 제안을 위한 충분한 내용을 포함하고 있습니다.";
        
        // When
        boolean result = validationService.isValidLlmResponse(validResponse);
        
        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 LLM 응답 검증 - 너무 짧음")
    void isValidLlmResponse_TooShort() {
        // Given
        String shortResponse = "짧음";
        
        // When
        boolean result = validationService.isValidLlmResponse(shortResponse);
        
        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("유효하지 않은 LLM 응답 검증 - 빈 문자열")
    void isValidLlmResponse_Empty() {
        // Given
        String emptyResponse = "";
        
        // When
        boolean result = validationService.isValidLlmResponse(emptyResponse);
        
        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("JSON 형식 검증 - 성공")
    void isValidJsonFormat_Success() {
        // Given
        String validJson = "{\"name\": \"홍길동\", \"age\": 30}";
        
        // When
        boolean result = validationService.isValidJsonFormat(validJson);
        
        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("JSON 형식 검증 - 실패")
    void isValidJsonFormat_Invalid() {
        // Given
        String invalidJson = "이것은 JSON이 아닙니다";
        
        // When
        boolean result = validationService.isValidJsonFormat(invalidJson);
        
        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("이력서 제안 응답 내용 검증 - 성공")
    void isValidResumeSuggestionContent_Success() {
        // Given
        String validContent = """
                {
                  "suggestedResume": {
                    "name": "홍길동"
                  },
                  "improvementTips": ["팁1"],
                  "missingElements": ["요소1"]
                }
                """;
        
        // When
        boolean result = validationService.isValidResumeSuggestionContent(validContent);
        
        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이력서 제안 응답 내용 검증 - 필수 키 누락")
    void isValidResumeSuggestionContent_MissingKey() {
        // Given
        String invalidContent = """
                {
                  "suggestedResume": {
                    "name": "홍길동"
                  }
                }
                """;
        
        // When
        boolean result = validationService.isValidResumeSuggestionContent(invalidContent);
        
        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("민감한 정보 검증 - 비밀번호 포함")
    void containsSensitiveInfo_Password() {
        // Given
        String contentWithPassword = "사용자 계정의 password는 123456입니다.";
        
        // When
        boolean result = validationService.containsSensitiveInfo(contentWithPassword);
        
        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("민감한 정보 검증 - 정상 내용")
    void containsSensitiveInfo_Safe() {
        // Given
        String safeContent = "이것은 안전한 이력서 내용입니다. Java와 Spring Boot를 사용한 개발 경험이 있습니다.";
        
        // When
        boolean result = validationService.containsSensitiveInfo(safeContent);
        
        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("응답 품질 점수 계산 - 고품질")
    void calculateResponseQuality_HighQuality() {
        // Given
        String highQualityResponse = """
                {
                  "suggestedResume": {
                    "name": "김개발",
                    "introduction": "5년간의 백엔드 개발 경험을 바탕으로 안정적인 서비스를 개발합니다."
                  },
                  "improvementTips": ["구체적인 성과를 추가하세요"],
                  "missingElements": ["교육 이력"]
                }
                """;
        
        // When
        int score = validationService.calculateResponseQuality(highQualityResponse);
        
        // Then
        assertThat(score).isGreaterThan(70);
    }

    @Test
    @DisplayName("응답 품질 점수 계산 - 저품질")
    void calculateResponseQuality_LowQuality() {
        // Given
        String lowQualityResponse = "짧은 응답";
        
        // When
        int score = validationService.calculateResponseQuality(lowQualityResponse);
        
        // Then
        assertThat(score).isLessThan(70);
    }
}