package com.cvmento.domain.resume.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 이력서 AI 응답 검증 서비스
 */
@Service
@Slf4j
public class ResumeAiValidationService {

    /**
     * LLM 응답 기본 검증
     */
    public boolean isValidLlmResponse(String response) {
        if (!StringUtils.hasText(response)) {
            log.warn("LLM 응답이 비어있습니다");
            return false;
        }
        
        if (response.length() < 50) {
            log.warn("LLM 응답이 너무 짧습니다. 길이: {}", response.length());
            return false;
        }
        
        if (response.length() > 100000) {
            log.warn("LLM 응답이 너무 깁니다. 길이: {}", response.length());
            return false;
        }
        
        return true;
    }

    /**
     * JSON 형식 기본 검증
     */
    public boolean isValidJsonFormat(String jsonString) {
        if (!StringUtils.hasText(jsonString)) {
            return false;
        }
        
        String trimmed = jsonString.trim();
        
        // JSON 기본 구조 확인
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            log.warn("JSON 응답 형식이 올바르지 않습니다. 시작: {}, 끝: {}", 
                    trimmed.substring(0, Math.min(10, trimmed.length())),
                    trimmed.substring(Math.max(0, trimmed.length() - 10)));
            return false;
        }
        
        return true;
    }

    /**
     * 이력서 제안 응답 내용 검증
     */
    public boolean isValidResumeSuggestionContent(String jsonString) {
        if (!isValidJsonFormat(jsonString)) {
            return false;
        }
        
        // 필수 키 존재 여부 확인
        String[] requiredKeys = {
            "\"suggestedResume\"", 
            "\"improvementTips\"", 
            "\"missingElements\""
        };
        
        for (String key : requiredKeys) {
            if (!jsonString.contains(key)) {
                log.warn("필수 키가 누락되었습니다: {}", key);
                return false;
            }
        }
        
        return true;
    }

    /**
     * 섹션 개선 응답 내용 검증
     */
    public boolean isValidSectionImprovementContent(String jsonString) {
        if (!isValidJsonFormat(jsonString)) {
            return false;
        }
        
        // 필수 키 존재 여부 확인
        String[] requiredKeys = {
            "\"sectionType\"",
            "\"originalContent\"", 
            "\"improvedContent\"",
            "\"improvementPoints\"",
            "\"tips\""
        };
        
        for (String key : requiredKeys) {
            if (!jsonString.contains(key)) {
                log.warn("필수 키가 누락되었습니다: {}", key);
                return false;
            }
        }
        
        return true;
    }

    /**
     * 민감한 정보 포함 여부 검증 (보안)
     */
    public boolean containsSensitiveInfo(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        
        // 민감한 정보 패턴들
        String[] sensitivePatterns = {
            "password",
            "비밀번호", 
            "주민등록번호",
            "credit card",
            "신용카드",
            "계좌번호",
            "account number"
        };
        
        for (String pattern : sensitivePatterns) {
            if (lowerContent.contains(pattern)) {
                log.warn("민감한 정보가 포함된 것으로 의심됩니다: {}", pattern);
                return true;
            }
        }
        
        return false;
    }

    /**
     * 응답 품질 점수 계산 (1-100)
     */
    public int calculateResponseQuality(String response) {
        if (!StringUtils.hasText(response)) {
            return 0;
        }
        
        int score = 50; // 기본 점수
        
        // 길이 점수 (적절한 길이인지)
        int length = response.length();
        if (length >= 500 && length <= 10000) {
            score += 20;
        } else if (length >= 200 && length <= 20000) {
            score += 10;
        }
        
        // JSON 구조 점수
        if (isValidJsonFormat(response)) {
            score += 15;
        }
        
        // 한국어 포함 여부 (이력서는 한국어로 작성)
        if (containsKorean(response)) {
            score += 10;
        }
        
        // 민감한 정보 포함시 점수 차감
        if (containsSensitiveInfo(response)) {
            score -= 30;
        }
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 한국어 포함 여부 확인
     */
    private boolean containsKorean(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        
        for (char c : text.toCharArray()) {
            if ((c >= 0xAC00 && c <= 0xD7AF) ||  // 한글 완성형
                (c >= 0x3131 && c <= 0x318E)) {   // 한글 자모
                return true;
            }
        }
        
        return false;
    }
}