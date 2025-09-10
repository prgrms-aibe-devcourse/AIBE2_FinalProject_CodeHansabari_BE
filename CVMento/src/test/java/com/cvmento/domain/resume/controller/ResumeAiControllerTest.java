package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.domain.resume.service.ResumeAiService;
import com.cvmento.global.exception.customException.ResumeAiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeAiController.class)
@DisplayName("이력서 AI 컨트롤러 테스트")
class ResumeAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeAiService resumeAiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("이력서 섹션 제안 요청 - 성공")
    void suggestResumeSections_Success() throws Exception {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "네이버에서 3년간 백엔드 개발자로 근무했습니다. Java와 Spring Boot를 사용해서 REST API를 개발했고, AWS 클라우드 환경에서 MSA 아키텍처를 구축한 경험이 있습니다. 정보처리기사 자격증도 보유하고 있습니다."
        );

        SuggestedResume suggestedResume = new SuggestedResume(
                "백엔드 개발자 이력서",
                ResumeType.DEFAULT,
                "테스트사용자",
                "test@example.com",
                null,
                "010-1234-5678",
                CareerType.EXPERIENCED,
                "백엔드 개발자",
                "3년간의 백엔드 개발 경험을 바탕으로...",
                null, null, null,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );

        ResumeAiSuggestionResponse response = new ResumeAiSuggestionResponse(
                suggestedResume,
                List.of("더 구체적인 성과 지표를 추가하세요", "기술 스택별 숙련도를 명시하세요"),
                List.of("깃허브 링크", "포트폴리오 링크")
        );

        when(resumeAiService.generateResumeSuggestions(any(UserExperienceRequest.class), anyString()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/me/resumes/ai-suggestions/sections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이력서 섹션 추가 제안 생성 성공"))
                .andExpect(jsonPath("$.data.suggestedResume.title").value("백엔드 개발자 이력서"))
                .andExpect(jsonPath("$.data.suggestedResume.name").value("테스트사용자"))
                .andExpect(jsonPath("$.data.suggestedResume.careerType").value("EXPERIENCED"))
                .andExpect(jsonPath("$.data.improvementTips").isArray())
                .andExpect(jsonPath("$.data.improvementTips.length()").value(2))
                .andExpect(jsonPath("$.data.missingElements").isArray())
                .andExpect(jsonPath("$.data.missingElements.length()").value(2));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("이력서 섹션 제안 요청 - 유효성 검사 실패 (경험 글이 너무 짧음)")
    void suggestResumeSections_ValidationFailed_TooShort() throws Exception {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "짧은 경험"  // 50자 미만
        );

        // When & Then
        mockMvc.perform(post("/api/v1/me/resumes/ai-suggestions/sections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("이력서 섹션 제안 요청 - 유효성 검사 실패 (경험 글이 비어있음)")
    void suggestResumeSections_ValidationFailed_Empty() throws Exception {
        // Given
        UserExperienceRequest request = new UserExperienceRequest("");

        // When & Then
        mockMvc.perform(post("/api/v1/me/resumes/ai-suggestions/sections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("이력서 섹션 제안 요청 - AI 서비스 오류")
    void suggestResumeSections_ServiceError() throws Exception {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "네이버에서 3년간 백엔드 개발자로 근무했습니다. Java와 Spring Boot를 사용해서 REST API를 개발했고, AWS 클라우드 환경에서 MSA 아키텍처를 구축한 경험이 있습니다."
        );

        when(resumeAiService.generateResumeSuggestions(any(UserExperienceRequest.class), anyString()))
                .thenThrow(new ResumeAiException("AI 서비스 오류가 발생했습니다."));

        // When & Then
        mockMvc.perform(post("/api/v1/me/resumes/ai-suggestions/sections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("AI 서비스 요청 중 오류가 발생했습니다."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("이력서 섹션 제안 요청 - 경험 글이 너무 김")
    void suggestResumeSections_ValidationFailed_TooLong() throws Exception {
        // Given
        String longExperience = "네이버에서 백엔드 개발자로 근무했습니다. ".repeat(200); // 3000자 초과
        UserExperienceRequest request = new UserExperienceRequest(longExperience);

        // When & Then
        mockMvc.perform(post("/api/v1/me/resumes/ai-suggestions/sections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이력서 섹션 제안 요청 - 인증되지 않은 사용자")
    void suggestResumeSections_Unauthorized() throws Exception {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "네이버에서 3년간 백엔드 개발자로 근무했습니다. Java와 Spring Boot를 사용해서 REST API를 개발했습니다."
        );

        // When & Then
        mockMvc.perform(post("/api/v1/me/resumes/ai-suggestions/sections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}