package com.cvmento.domain.resume.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.resume.dto.request.UserExperienceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("이력서 AI 프롬프트 서비스 테스트")
class ResumeAiLlmPromptServiceTest {

    private ResumeAiLlmPromptService promptService;
    private Member testMember;

    @BeforeEach
    void setUp() {
        promptService = new ResumeAiLlmPromptService();
        
        testMember = Member.builder()
                .name("김개발자")
                .email("dev@example.com")
                .build();
    }

    @Test
    @DisplayName("이력서 제안 프롬프트 생성 - 기본 구조 확인")
    void buildFullResumeSuggestionPrompt_BasicStructure() {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "네이버에서 3년간 백엔드 개발자로 근무했습니다. Java와 Spring Boot를 주로 사용했으며, REST API 개발과 데이터베이스 설계를 담당했습니다."
        );

        // When
        String prompt = promptService.buildFullResumeSuggestionPrompt(request, testMember);

        // Then
        assertThat(prompt).isNotNull();
        assertThat(prompt).isNotEmpty();
        
        // 프롬프트 구조 확인
        assertThat(prompt).contains("이력서 컨설턴트로서");
        assertThat(prompt).contains("## 사용자 정보");
        assertThat(prompt).contains("## 작업 요청");
        assertThat(prompt).contains("## 필수 응답 형식");
        assertThat(prompt).contains("### 🚨 절대 엄수 규칙 🚨");
    }

    @Test
    @DisplayName("이력서 제안 프롬프트 생성 - 사용자 정보 포함 확인")
    void buildFullResumeSuggestionPrompt_UserInfo() {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "카카오에서 2년간 프론트엔드 개발자로 근무했습니다. React와 TypeScript를 사용해서 웹 애플리케이션을 개발했습니다."
        );

        // When
        String prompt = promptService.buildFullResumeSuggestionPrompt(request, testMember);

        // Then
        // 사용자 정보가 포함되었는지 확인
        assertThat(prompt).contains("김개발자");
        assertThat(prompt).contains("dev@example.com");
        
        // 경험 정보가 포함되었는지 확인
        assertThat(prompt).contains("카카오에서 2년간");
        assertThat(prompt).contains("프론트엔드 개발자");
        assertThat(prompt).contains("React와 TypeScript");
    }

    @Test
    @DisplayName("이력서 제안 프롬프트 생성 - JSON 형식 지시 포함 확인")
    void buildFullResumeSuggestionPrompt_JsonFormat() {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "스타트업에서 1년간 풀스택 개발자로 근무했습니다."
        );

        // When
        String prompt = promptService.buildFullResumeSuggestionPrompt(request, testMember);

        // Then
        // JSON 형식 지시사항 확인
        assertThat(prompt).contains("```json");
        assertThat(prompt).contains("suggestedResume");
        assertThat(prompt).contains("improvementTips");
        assertThat(prompt).contains("missingElements");
        
        // 필수 필드들 확인
        assertThat(prompt).contains("title");
        assertThat(prompt).contains("name");
        assertThat(prompt).contains("careerType");
        assertThat(prompt).contains("fieldName");
        assertThat(prompt).contains("introduction");
    }

    @Test
    @DisplayName("이력서 제안 프롬프트 생성 - 가이드라인 포함 확인")
    void buildFullResumeSuggestionPrompt_Guidelines() {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "삼성전자에서 5년간 임베디드 소프트웨어 개발자로 근무했습니다."
        );

        // When
        String prompt = promptService.buildFullResumeSuggestionPrompt(request, testMember);

        // Then
        // 중요한 가이드라인들이 포함되었는지 확인
        assertThat(prompt).contains("오직 JSON만");
        assertThat(prompt).contains("모든 키 필수");
        assertThat(prompt).contains("빈 값 금지");
        assertThat(prompt).contains("형식 준수");
        
        // 제안 원칙들 확인
        assertThat(prompt).contains("사실 기반");
        assertThat(prompt).contains("현실적 수준");
        assertThat(prompt).contains("과장하거나 허위 내용 생성 금지");
    }

    @Test
    @DisplayName("이력서 제안 프롬프트 생성 - 섹션별 구조 확인")
    void buildFullResumeSuggestionPrompt_SectionStructure() {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "구글에서 4년간 머신러닝 엔지니어로 근무했습니다. Python과 TensorFlow를 주로 사용했으며, 추천 시스템 개발을 담당했습니다."
        );

        // When
        String prompt = promptService.buildFullResumeSuggestionPrompt(request, testMember);

        // Then
        // 이력서 섹션들이 포함되었는지 확인
        assertThat(prompt).contains("careers");
        assertThat(prompt).contains("projects");
        assertThat(prompt).contains("techStacks");
        assertThat(prompt).contains("educations");
        assertThat(prompt).contains("trainings");
        assertThat(prompt).contains("additionalInfos");
        
        // 제안 범위 설명 확인
        assertThat(prompt).contains("경력 정보");
        assertThat(prompt).contains("프로젝트");
        assertThat(prompt).contains("기술스택");
        assertThat(prompt).contains("학력 정보");
        assertThat(prompt).contains("교육이력");
        assertThat(prompt).contains("기타사항");
    }

    @Test
    @DisplayName("프롬프트 길이 적정성 확인")
    void buildFullResumeSuggestionPrompt_LengthCheck() {
        // Given
        UserExperienceRequest request = new UserExperienceRequest(
                "짧은 경험 설명입니다."
        );

        // When
        String prompt = promptService.buildFullResumeSuggestionPrompt(request, testMember);

        // Then
        // 프롬프트가 너무 짧거나 길지 않은지 확인 (대략 3000~10000자 정도 예상)
        assertThat(prompt.length()).isGreaterThan(3000);
        assertThat(prompt.length()).isLessThan(15000);
    }
}