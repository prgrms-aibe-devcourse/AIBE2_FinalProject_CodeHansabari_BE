package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.client.ResumeLlmFeignClient;
import com.cvmento.domain.resume.dto.request.LlmRequest;
import com.cvmento.domain.resume.dto.response.LlmResponse;
import com.cvmento.global.exception.customException.ResumeAiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 이력서 AI용 LLM 클라이언트 서비스
 * 인터뷰 AI 서비스와 동일한 패턴으로 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAiLlmClientService {

    private final ResumeLlmFeignClient resumeLlmFeignClient;
    
    @Value("${llm.api.resume.key:}")
    private String apiKey;
    
    @Value("${llm.api.resume.enabled:true}")
    private boolean apiEnabled;

    /**
     * 사용자 경험 기반 이력서 제안 생성
     */
    public String generateResumeSuggestion(String prompt) {
        validatePrompt(prompt);
        
        try {
            log.info("=== 이력서 AI 제안 LLM 요청 시작 ===");
            log.debug("프롬프트 길이: {} 문자", prompt.length());
            
            // API 활성화 여부 확인
            if (!apiEnabled || apiKey.isEmpty()) {
                log.warn("LLM API가 비활성화 상태이거나 API 키가 없어 더미 응답 반환");
                return getDummyResumeSuggestionResponse();
            }
            
            // LLM API 호출
            LlmRequest request = LlmRequest.createForResumeSuggestion(prompt);
            String rawResponse = resumeLlmFeignClient.callLlm(
                    "Bearer " + apiKey,
                    request
            );
            
            log.info("=== 이력서 AI 제안 LLM 응답 완료. 응답 길이: {} 문자 ===", rawResponse.length());
            
            return rawResponse;
            
        } catch (Exception e) {
            log.error("이력서 AI 제안 LLM 요청 중 오류 발생: {}", e.getMessage(), e);
            throw new ResumeAiException("AI 서비스 요청 중 오류가 발생했습니다.", e);
        }
    }


    /**
     * 프롬프트 유효성 검증
     */
    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("프롬프트가 비어있습니다.");
        }
        
        if (prompt.length() > 50000) {
            log.warn("프롬프트 길이가 매우 깁니다: {} 문자", prompt.length());
        }
    }

    /**
     * 더미 이력서 제안 응답 반환
     */
    private String getDummyResumeSuggestionResponse() {
        return """
                {
                  "suggestedResume": {
                    "title": "백엔드 개발자 지원용 이력서",
                    "type": "DEFAULT",
                    "name": "홍길동",
                    "email": "hong@example.com",
                    "birthYear": 1995,
                    "phone": "010-1234-5678",
                    "careerType": "EXPERIENCED",
                    "fieldName": "백엔드 개발자",
                    "introduction": "2년간의 백엔드 개발 경험을 바탕으로 효율적인 API 설계와 데이터베이스 최적화에 전문성을 가지고 있습니다.",
                    "githubUrl": "https://github.com/hong",
                    "blogUrl": null,
                    "notionUrl": null,
                    "educations": [],
                    "techStacks": [],
                    "careers": [],
                    "projects": [],
                    "trainings": [],
                    "additionalInfos": []
                  },
                  "improvementTips": ["경력 사항을 더 구체적으로 작성하세요", "프로젝트 성과를 숫자로 표현하세요"],
                  "missingElements": ["교육 이력", "자격증 정보", "블로그/포트폴리오 링크"]
                }
                """;
    }

}