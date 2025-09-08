package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.request.ResumeAiExperienceRequest;
import com.cvmento.domain.resume.dto.response.ResumeAiSuggestionResponse;
import com.cvmento.domain.resume.dto.response.SuggestedResumeItemDto;
import com.cvmento.domain.resume.dto.response.SuggestedResumeSectionDto;
import com.cvmento.domain.resume.enums.ResumeSectionType;
import com.cvmento.domain.resume.service.ResumeAiService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import com.cvmento.global.exception.customException.ResumeAiException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * ResumeAiController의 단위 테스트
 *
 * 정상 시나리오:
 * - POST /resumes/ai-suggest: AI 기반 이력서 제안
 *
 * 비정상 요청 시나리오:
 * - 존재하지 않는 사용자
 * - AI 서비스 오류
 * - 잘못된 입력 데이터
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeAiController 단위 테스트")
@Slf4j
class ResumeAiControllerTest {

    @Mock
    private ResumeAiService resumeAiService;

    @InjectMocks
    private ResumeAiController resumeAiController;

    private String userEmail = "test@example.com";
    private UserDetails mockUserDetails;
    private ResumeAiExperienceRequest aiRequest;
    private ResumeAiSuggestionResponse aiResponse;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 설정 시작 ===");

        mockUserDetails = User.withUsername(userEmail)
                .password("")
                .authorities("ROLE_USER")
                .build();

        // AI 제안 요청 Mock
        aiRequest = new ResumeAiExperienceRequest(
                "Spring Boot 기반의 백엔드 개발자로, RESTful API 개발 및 시스템 최적화 경험을 보유하고 있습니다. " +
                "특히, 캐싱 전략을 통해 시스템 성능을 20% 개선하고, CI/CD 파이프라인 구축을 통해 배포 자동화에 기여했습니다."
        );

        // AI 제안 응답 Mock
        SuggestedResumeItemDto experienceItem = new SuggestedResumeItemDto(
                "ABC 회사",
                "백엔드 개발자", 
                "2022-01",
                "2024-12",
                "Spring Boot 기반 RESTful API 개발, 시스템 성능 20% 개선"
        );

        SuggestedResumeSectionDto experienceSection = new SuggestedResumeSectionDto(
                ResumeSectionType.WORK_EXPERIENCE,
                "경력",
                List.of(experienceItem)
        );

        aiResponse = new ResumeAiSuggestionResponse(List.of(experienceSection));

        log.info("테스트 데이터 설정 완료");
        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("getAiSuggestions 메서드")
    class GetAiSuggestionsTest {

        @Test
        @DisplayName("정상적인 AI 제안 생성")
        void shouldGetAiSuggestionsSuccessfully() {
            log.info("=== 테스트 시작: 정상적인 AI 제안 생성 ===");

            // given
            given(resumeAiService.getResumeSuggestions(aiRequest, userEmail))
                    .willReturn(aiResponse);
            log.info("Mock 설정: AI 제안 서비스 호출 -> aiResponse 반환");

            // when
            log.info("=== 컨트롤러 메서드 실행 ===");
            ResponseEntity<CommonResponse<ResumeAiSuggestionResponse>> result =
                    resumeAiController.getAiSuggestions(aiRequest, mockUserDetails);

            // then
            log.info("=== 결과 검증 ===");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("이력서 AI 제안 성공");
            assertThat(result.getBody().getData().suggestedSections()).hasSize(1);
            assertThat(result.getBody().getData().suggestedSections().get(0).sectionType()).isEqualTo(ResumeSectionType.WORK_EXPERIENCE);

            verify(resumeAiService).getResumeSuggestions(aiRequest, userEmail);
            log.info("✅ AI 제안 생성 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 때 예외 발생")
        void shouldThrowExceptionWhenMemberNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 사용자일 때 예외 발생 ===");

            // given
            given(resumeAiService.getResumeSuggestions(aiRequest, userEmail))
                    .willThrow(new MemberNotFoundException("사용자를 찾을 수 없습니다."));

            // when & then
            assertThatThrownBy(() ->
                    resumeAiController.getAiSuggestions(aiRequest, mockUserDetails))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");

            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("AI 서비스 오류시 예외 발생")
        void shouldThrowExceptionWhenAiServiceError() {
            log.info("=== 테스트 시작: AI 서비스 오류시 예외 발생 ===");

            // given
            given(resumeAiService.getResumeSuggestions(aiRequest, userEmail))
                    .willThrow(new ResumeAiException("AI 서비스 처리 중 오류가 발생했습니다."));

            // when & then
            assertThatThrownBy(() ->
                    resumeAiController.getAiSuggestions(aiRequest, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("AI 서비스 처리 중 오류가 발생했습니다.");

            log.info("✅ 예상된 AI 서비스 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("빈 경험 내용으로 AI 제안 요청시 처리")
        void shouldHandleEmptyExperienceContent() {
            log.info("=== 테스트 시작: 빈 경험 내용으로 AI 제안 요청시 처리 ===");

            // given
            ResumeAiExperienceRequest emptyRequest = new ResumeAiExperienceRequest("");
            given(resumeAiService.getResumeSuggestions(emptyRequest, userEmail))
                    .willThrow(new ResumeAiException("AI가 유효한 제안을 생성하지 못했습니다. 입력 내용을 수정하여 다시 시도해주세요."));

            // when & then
            assertThatThrownBy(() ->
                    resumeAiController.getAiSuggestions(emptyRequest, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("AI가 유효한 제안을 생성하지 못했습니다. 입력 내용을 수정하여 다시 시도해주세요.");

            log.info("✅ 빈 내용에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("AI 응답 파싱 오류시 예외 발생")
        void shouldThrowExceptionWhenAiResponseParsingError() {
            log.info("=== 테스트 시작: AI 응답 파싱 오류시 예외 발생 ===");

            // given
            given(resumeAiService.getResumeSuggestions(aiRequest, userEmail))
                    .willThrow(new ResumeAiException("AI 응답을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));

            // when & then
            assertThatThrownBy(() ->
                    resumeAiController.getAiSuggestions(aiRequest, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("AI 응답을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

            log.info("✅ 예상된 응답 파싱 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("비정상 요청 처리 테스트")
    class AbnormalRequestTest {

        @Test
        @DisplayName("네트워크 연결 오류시 처리")
        void shouldHandleNetworkConnectionError() {
            log.info("=== 테스트 시작: 네트워크 연결 오류시 처리 ===");

            // given - AI 서비스 연결 실패 시나리오
            given(resumeAiService.getResumeSuggestions(aiRequest, userEmail))
                    .willThrow(new ResumeAiException("AI 서비스에 연결할 수 없습니다. 네트워크 상태를 확인하거나 잠시 후 다시 시도해주세요."));

            // when & then
            assertThatThrownBy(() ->
                    resumeAiController.getAiSuggestions(aiRequest, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("AI 서비스에 연결할 수 없습니다. 네트워크 상태를 확인하거나 잠시 후 다시 시도해주세요.");

            log.info("✅ 네트워크 연결 오류에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("과도하게 긴 경험 내용으로 요청시 처리")
        void shouldHandleExcessivelyLongContent() {
            log.info("=== 테스트 시작: 과도하게 긴 경험 내용으로 요청시 처리 ===");

            // given - 매우 긴 경험 내용 (실제 제한은 서비스에서 처리)
            String longContent = "A".repeat(10000); // 10KB 문자열
            ResumeAiExperienceRequest longRequest = new ResumeAiExperienceRequest(longContent);
            
            given(resumeAiService.getResumeSuggestions(longRequest, userEmail))
                    .willThrow(new ResumeAiException("입력 내용이 너무 깁니다. 내용을 줄여서 다시 시도해주세요."));

            // when & then
            assertThatThrownBy(() ->
                    resumeAiController.getAiSuggestions(longRequest, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("입력 내용이 너무 깁니다. 내용을 줄여서 다시 시도해주세요.");

            log.info("✅ 과도한 입력에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }
}