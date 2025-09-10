package com.cvmento.domain.resume.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.dto.request.UserExperienceRequest;
import com.cvmento.domain.resume.dto.response.ResumeAiSuggestionResponse;
import com.cvmento.domain.resume.dto.response.SuggestedResume;
import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.global.exception.customException.ResumeAiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("이력서 AI 서비스 테스트")
class ResumeAiServiceTest {

    private ResumeAiService resumeAiService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ResumeAiLlmPromptService promptService;

    @Mock
    private ResumeAiLlmClientService llmClientService;

    @Mock
    private ResumeAiResponseParserService responseParserService;

    private Member testMember;
    private UserExperienceRequest testRequest;

    @BeforeEach
    void setUp() {
        resumeAiService = new ResumeAiService(
                memberRepository, 
                promptService, 
                llmClientService, 
                responseParserService
        );

        testMember = Member.builder()
                .name("김개발자")
                .email("dev@example.com")
                .build();

        testRequest = new UserExperienceRequest(
                "네이버에서 3년간 백엔드 개발자로 근무했습니다. Java와 Spring Boot를 주로 사용했으며, 대용량 트래픽 처리 경험이 있습니다. AWS 클라우드 환경에서 MSA 아키텍처를 구축한 경험도 있습니다."
        );
    }

    @Test
    @DisplayName("이력서 AI 제안 생성 - 성공")
    void generateResumeSuggestions_Success() {
        // Given
        String testEmail = "dev@example.com";
        String generatedPrompt = "AI 프롬프트 내용...";
        String llmResponse = "LLM 응답 내용...";
        
        SuggestedResume suggestedResume = new SuggestedResume(
                "백엔드 개발자 이력서",
                ResumeType.DEFAULT,
                "김개발자",
                "dev@example.com",
                null,
                "010-1234-5678",
                CareerType.EXPERIENCED,
                "백엔드 개발자",
                "3년간의 백엔드 개발 경험...",
                null, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );
        
        ResumeAiSuggestionResponse expectedResponse = new ResumeAiSuggestionResponse(
                suggestedResume,
                List.of("더 구체적인 성과 지표를 추가하세요"),
                List.of("깃허브 링크", "포트폴리오")
        );

        // Mock 설정
        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.of(testMember));
        when(promptService.buildFullResumeSuggestionPrompt(any(), any())).thenReturn(generatedPrompt);
        when(llmClientService.generateResumeSuggestion(generatedPrompt)).thenReturn(llmResponse);
        when(responseParserService.parseResumeSuggestionResponse(any(), any(), any()))
                .thenReturn(expectedResponse);

        // When
        ResumeAiSuggestionResponse result = resumeAiService.generateResumeSuggestions(testRequest, testEmail);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.suggestedResume()).isNotNull();
        assertThat(result.suggestedResume().name()).isEqualTo("김개발자");
        assertThat(result.suggestedResume().careerType()).isEqualTo(CareerType.EXPERIENCED);
        assertThat(result.improvementTips()).hasSize(1);
        assertThat(result.missingElements()).hasSize(2);
    }

    @Test
    @DisplayName("이력서 AI 제안 생성 - 사용자 없음으로 실패")
    void generateResumeSuggestions_UserNotFound() {
        // Given
        String testEmail = "notfound@example.com";
        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> resumeAiService.generateResumeSuggestions(testRequest, testEmail))
                .isInstanceOf(ResumeAiException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("이력서 AI 제안 생성 - LLM 호출 실패")
    void generateResumeSuggestions_LlmCallFailed() {
        // Given
        String testEmail = "dev@example.com";
        String generatedPrompt = "AI 프롬프트 내용...";

        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.of(testMember));
        when(promptService.buildFullResumeSuggestionPrompt(any(), any())).thenReturn(generatedPrompt);
        when(llmClientService.generateResumeSuggestion(generatedPrompt))
                .thenThrow(new RuntimeException("LLM API 호출 실패"));

        // When & Then
        assertThatThrownBy(() -> resumeAiService.generateResumeSuggestions(testRequest, testEmail))
                .isInstanceOf(ResumeAiException.class)
                .hasMessageContaining("AI 섹션 추가 제안 생성 중 오류가 발생했습니다");
    }

    @Test
    @DisplayName("이력서 AI 제안 생성 - 응답 파싱 실패")
    void generateResumeSuggestions_ResponseParsingFailed() {
        // Given
        String testEmail = "dev@example.com";
        String generatedPrompt = "AI 프롬프트 내용...";
        String llmResponse = "잘못된 응답 형식";

        when(memberRepository.findByEmail(testEmail)).thenReturn(Optional.of(testMember));
        when(promptService.buildFullResumeSuggestionPrompt(any(), any())).thenReturn(generatedPrompt);
        when(llmClientService.generateResumeSuggestion(generatedPrompt)).thenReturn(llmResponse);
        when(responseParserService.parseResumeSuggestionResponse(any(), any(), any()))
                .thenThrow(new ResumeAiException("응답 파싱 실패"));

        // When & Then
        assertThatThrownBy(() -> resumeAiService.generateResumeSuggestions(testRequest, testEmail))
                .isInstanceOf(ResumeAiException.class)
                .hasMessageContaining("응답 파싱 실패");
    }
}