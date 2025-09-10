package com.cvmento.domain.resume.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.ResumeAiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("이력서 AI 응답 파서 서비스 테스트")
class ResumeAiResponseParserServiceTest {

    private ResumeAiResponseParserService parserService;
    
    @Mock
    private ResumeAiValidationService validationService;
    
    @Mock
    private OpenAiResponseParser openAiResponseParser;
    
    private ObjectMapper objectMapper;
    private Member testMember;
    private UserExperienceRequest testRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parserService = new ResumeAiResponseParserService(objectMapper, validationService, openAiResponseParser);
        
        testMember = Member.builder()
                .name("김테스트")
                .email("test@example.com")
                .build();
                
        testRequest = new UserExperienceRequest(
                "5년간 Spring Boot를 사용한 백엔드 개발 경험이 있습니다. 네이버에서 근무했으며 Java와 Spring Boot를 주로 사용했습니다. 정보처리기사 자격증도 보유하고 있습니다."
        );
    }

    @Test
    @DisplayName("이력서 제안 응답 파싱 - 성공")
    void parseResumeSuggestionResponse_Success() {
        // Given
        String validLlmResponse = """
                {
                  "suggestedResume": {
                    "title": "백엔드 개발자 이력서",
                    "type": "DEFAULT",
                    "name": "김테스트",
                    "email": "test@example.com",
                    "birthYear": 1990,
                    "phone": "010-1234-5678",
                    "careerType": "EXPERIENCED",
                    "fieldName": "백엔드 개발자",
                    "introduction": "경험 많은 백엔드 개발자입니다.",
                    "githubUrl": null,
                    "blogUrl": null,
                    "notionUrl": null,
                    "educations": [],
                    "techStacks": [],
                    "careers": [],
                    "projects": [],
                    "trainings": [],
                    "additionalInfos": []
                  },
                  "improvementTips": ["더 구체적인 경력을 추가하세요"],
                  "missingElements": ["교육 이력", "프로젝트 경험"]
                }
                """;
        
        // Mock 설정
        when(validationService.isValidLlmResponse(anyString())).thenReturn(true);
        when(validationService.isValidResumeSuggestionContent(anyString())).thenReturn(true);
        when(validationService.containsSensitiveInfo(anyString())).thenReturn(false);
        when(validationService.calculateResponseQuality(anyString())).thenReturn(85);
        
        // OpenAiResponseParser mock 설정 - 래핑된 응답에서 실제 JSON 추출
        when(openAiResponseParser.extractTextContent(anyString())).thenReturn(validLlmResponse);
        
        // When
        ResumeAiSuggestionResponse response = parserService.parseResumeSuggestionResponse(
                validLlmResponse, testRequest, testMember);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.suggestedResume()).isNotNull();
        assertThat(response.suggestedResume().name()).isEqualTo("김테스트");
        assertThat(response.suggestedResume().fieldName()).isEqualTo("백엔드 개발자");
        assertThat(response.improvementTips()).hasSize(1);
        assertThat(response.missingElements()).hasSize(2);
    }

    @Test
    @DisplayName("이력서 제안 응답 파싱 - 유효하지 않은 응답으로 실패")
    void parseResumeSuggestionResponse_InvalidResponse() {
        // Given
        String invalidLlmResponse = "유효하지 않은 응답";
        
        // Mock 설정
        when(validationService.isValidLlmResponse(anyString())).thenReturn(false);
        when(openAiResponseParser.extractTextContent(anyString())).thenReturn(invalidLlmResponse);
        
        // When & Then
        assertThatThrownBy(() -> parserService.parseResumeSuggestionResponse(
                invalidLlmResponse, testRequest, testMember))
                .isInstanceOf(ResumeAiException.class)
                .hasMessageContaining("유효하지 않은 LLM 응답입니다");
    }

    @Test
    @DisplayName("이력서 제안 응답 파싱 - 민감한 정보 포함으로 실패")
    void parseResumeSuggestionResponse_SensitiveInfo() {
        // Given
        String responseWithSensitiveInfo = """
                {
                  "suggestedResume": {
                    "name": "사용자의 password는 123456입니다"
                  },
                  "improvementTips": [],
                  "missingElements": []
                }
                """;
        
        // Mock 설정
        when(validationService.isValidLlmResponse(anyString())).thenReturn(true);
        when(validationService.isValidResumeSuggestionContent(anyString())).thenReturn(true);
        when(validationService.containsSensitiveInfo(anyString())).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> parserService.parseResumeSuggestionResponse(
                responseWithSensitiveInfo, testRequest, testMember))
                .isInstanceOf(ResumeAiException.class)
                .hasMessageContaining("부적절한 내용이 포함되어 있습니다");
    }

    @Test
    @DisplayName("JSON 추출 - 코드 블록 있는 경우")
    void extractJsonFromResponse_WithCodeBlock() {
        // Given
        String responseWithCodeBlock = """
                ```json
                {"name": "김테스트", "age": 30}
                ```
                """;
        
        // Mock 설정 (private 메서드이므로 간접 테스트)
        when(validationService.isValidLlmResponse(anyString())).thenReturn(true);
        when(validationService.isValidResumeSuggestionContent(anyString())).thenReturn(true);
        when(validationService.containsSensitiveInfo(anyString())).thenReturn(false);
        when(validationService.calculateResponseQuality(anyString())).thenReturn(80);
        
        String validResponse = """
                ```json
                {
                  "suggestedResume": {
                    "title": "테스트 이력서",
                    "type": "DEFAULT",
                    "name": "김테스트",
                    "email": "test@example.com",
                    "birthYear": 1990,
                    "phone": "010-1234-5678",
                    "careerType": "EXPERIENCED",
                    "fieldName": "개발자",
                    "introduction": "개발자입니다.",
                    "githubUrl": null,
                    "blogUrl": null,
                    "notionUrl": null,
                    "educations": [],
                    "techStacks": [],
                    "careers": [],
                    "projects": [],
                    "trainings": [],
                    "additionalInfos": []
                  },
                  "improvementTips": ["팁"],
                  "missingElements": ["요소"]
                }
                ```
                """;
        
        // When
        ResumeAiSuggestionResponse response = parserService.parseResumeSuggestionResponse(
                validResponse, testRequest, testMember);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.suggestedResume().name()).isEqualTo("김테스트");
    }
}