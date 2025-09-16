package com.cvmento.domain.interview.controller;

import com.cvmento.domain.interview.dto.request.CustomQuestionRequest;
import com.cvmento.domain.interview.dto.response.CustomAnswerResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaListResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaResponse;
import com.cvmento.domain.interview.service.InterviewService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.CoverLetterException;
import com.cvmento.global.exception.customException.InterviewException;
import com.cvmento.global.exception.customException.InterviewLimitExceededException;
import com.cvmento.global.exception.customException.MemberNotFoundException;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * InterviewController의 단위 테스트
 *
 * 정상 시나리오:
 * - GET: 기존 질문 조회 (있음/없음), 예외 처리 (사용자/자소서 없음)
 * - POST: 질문 생성, 제한 초과 예외, LLM 서비스 오류 예외
 *
 * 비정상 요청 시나리오:
 * - 존재하지 않는 자소서 ID 접근 (URL 공유/북마크)
 * - 다른 사용자의 자소서 접근 (URL 조작)
 * - 이미 15개 제한 도달 후 생성 시도 (프론트 상태 동기화 문제)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewController 단위 테스트")
@Slf4j
class InterviewControllerTest {

    @Mock
    private InterviewService interviewService;

    @InjectMocks
    private InterviewController interviewController;

    private Long coverLetterId = 1L;
    private String userEmail = "test@example.com";
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 설정 시작 ===");

        mockUserDetails = User.withUsername(userEmail)
                .password("")  // 소셜 로그인이므로 빈 패스워드
                .authorities("ROLE_USER")
                .build();

        log.info("테스트 Mock UserDetails 생성 완료: username={}, authorities={}",
                userEmail, mockUserDetails.getAuthorities());
        log.info("테스트 파라미터: coverLetterId={}, userEmail={}", coverLetterId, userEmail);
        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("getInterviewQuestions 메서드")
    class GetInterviewQuestionsTest {

        @Test
        @DisplayName("기존 질문이 있을 때 정상 조회")
        void shouldReturnExistingQuestions() {
            log.info("=== 테스트 시작: 기존 질문이 있을 때 정상 조회 ===");

            // given
            List<InterviewQnaResponse> qnaList = createMockQnaResponseList(3);
            InterviewQnaListResponse mockResponse = new InterviewQnaListResponse(qnaList, 3, 3);
            log.info("Mock 응답 생성: qnaList.size()={}, totalCount={}, generatedCount={}",
                    mockResponse.qnaList().size(), mockResponse.totalCount(), mockResponse.generatedCount());

            given(interviewService.getExistingInterviewQna(coverLetterId, userEmail))
                    .willReturn(mockResponse);
            log.info("Mock 설정: interviewService.getExistingInterviewQna({}, {}) -> mockResponse 반환",
                    coverLetterId, userEmail);

            // when
            log.info("=== 컨트롤러 메서드 실행 ===");
            ResponseEntity<CommonResponse<InterviewQnaListResponse>> result =
                    interviewController.getInterviewQuestions(coverLetterId, mockUserDetails);
            log.info("컨트롤러 응답: statusCode={}, hasBody={}",
                    result.getStatusCode(), result.getBody() != null);

            // then
            log.info("=== 결과 검증 ===");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

            assertThat(result.getBody()).isNotNull();
            log.info("✅ 응답 바디 null 아님 검증 통과");

            assertThat(result.getBody().isSuccess()).isTrue();
            log.info("✅ success 필드 검증 통과: {}", result.getBody().isSuccess());

            assertThat(result.getBody().getMessage()).isEqualTo("면접 질문/답변 조회 성공");
            log.info("✅ 메시지 검증 통과: '{}'", result.getBody().getMessage());

            assertThat(result.getBody().getData().qnaList()).hasSize(3);
            log.info("✅ qnaList 크기 검증 통과: {}", result.getBody().getData().qnaList().size());

            assertThat(result.getBody().getData().totalCount()).isEqualTo(3);
            log.info("✅ totalCount 검증 통과: {}", result.getBody().getData().totalCount());

            assertThat(result.getBody().getData().generatedCount()).isEqualTo(3);
            log.info("✅ generatedCount 검증 통과: {}", result.getBody().getData().generatedCount());

            verify(interviewService).getExistingInterviewQna(coverLetterId, userEmail);
            log.info("✅ 서비스 메서드 호출 검증 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("기존 질문이 없을 때 빈 배열 반환")
        void shouldReturnEmptyListWhenNoQuestions() {
            log.info("=== 테스트 시작: 기존 질문이 없을 때 빈 배열 반환 ===");

            // given
            InterviewQnaListResponse emptyResponse = new InterviewQnaListResponse(
                    new ArrayList<>(), 0, 0);
            log.info("빈 Mock 응답 생성: qnaList.size()={}, totalCount={}, generatedCount={}",
                    emptyResponse.qnaList().size(), emptyResponse.totalCount(), emptyResponse.generatedCount());

            given(interviewService.getExistingInterviewQna(coverLetterId, userEmail))
                    .willReturn(emptyResponse);
            log.info("Mock 설정: 빈 배열 반환하도록 설정");

            // when
            log.info("=== 컨트롤러 메서드 실행 ===");
            ResponseEntity<CommonResponse<InterviewQnaListResponse>> result =
                    interviewController.getInterviewQuestions(coverLetterId, mockUserDetails);

            // then
            log.info("=== 결과 검증 ===");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getData().qnaList()).isEmpty();
            log.info("✅ 빈 배열 반환 검증 통과");

            assertThat(result.getBody().getData().totalCount()).isEqualTo(0);
            assertThat(result.getBody().getData().generatedCount()).isEqualTo(0);
            log.info("✅ 카운트 필드들 0 검증 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 때 예외 발생")
        void shouldThrowExceptionWhenMemberNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 사용자일 때 예외 발생 ===");

            // given
            given(interviewService.getExistingInterviewQna(coverLetterId, userEmail))
                    .willThrow(new MemberNotFoundException("사용자를 찾을 수 없습니다."));
            log.info("Mock 설정: MemberNotFoundException 발생하도록 설정");

            // when & then
            log.info("예외 발생 예상 - MemberNotFoundException");
            assertThatThrownBy(() ->
                    interviewController.getInterviewQuestions(coverLetterId, mockUserDetails))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");
            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 자소서일 때 예외 발생")
        void shouldThrowExceptionWhenCoverLetterNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 자소서일 때 예외 발생 ===");

            // given
            given(interviewService.getExistingInterviewQna(coverLetterId, userEmail))
                    .willThrow(new CoverLetterException("자소서를 찾을 수 없습니다."));
            log.info("Mock 설정: CoverLetterException 발생하도록 설정");

            // when & then
            log.info("예외 발생 예상 - CoverLetterException");
            assertThatThrownBy(() ->
                    interviewController.getInterviewQuestions(coverLetterId, mockUserDetails))
                    .isInstanceOf(CoverLetterException.class)
                    .hasMessage("자소서를 찾을 수 없습니다.");
            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("비정상 요청 처리 테스트")
    class AbnormalRequestTest {

        @Test
        @DisplayName("존재하지 않는 자소서 ID 접근시 처리")
        void shouldHandleNonExistentCoverLetter() {
            log.info("=== 테스트 시작: 존재하지 않는 자소서 ID 접근시 처리 ===");

            // given - URL 공유나 북마크로 삭제된 자소서에 접근하는 경우
            Long nonExistentId = 999999L;
            given(interviewService.getExistingInterviewQna(nonExistentId, userEmail))
                    .willThrow(new CoverLetterException("자소서를 찾을 수 없습니다."));
            log.info("Mock 설정: 존재하지 않는 자소서 ID로 CoverLetterException 발생");

            // when & then
            log.info("예외 발생 예상 - CoverLetterException (존재하지 않는 자소서)");
            assertThatThrownBy(() ->
                    interviewController.getInterviewQuestions(nonExistentId, mockUserDetails))
                    .isInstanceOf(CoverLetterException.class)
                    .hasMessage("자소서를 찾을 수 없습니다.");
            log.info("✅ 존재하지 않는 자소서 접근에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("다른 사용자의 자소서 접근시 처리")
        void shouldHandleAccessToOtherUsersCoverLetter() {
            log.info("=== 테스트 시작: 다른 사용자의 자소서 접근시 처리 ===");

            // given - URL 조작으로 다른 사용자의 자소서에 접근하는 경우
            given(interviewService.getExistingInterviewQna(coverLetterId, userEmail))
                    .willThrow(new CoverLetterException("자소서를 찾을 수 없습니다."));
            log.info("Mock 설정: 권한 없는 자소서 접근으로 CoverLetterException 발생");

            // when & then
            log.info("예외 발생 예상 - CoverLetterException (권한 없는 접근)");
            assertThatThrownBy(() ->
                    interviewController.getInterviewQuestions(coverLetterId, mockUserDetails))
                    .isInstanceOf(CoverLetterException.class)
                    .hasMessage("자소서를 찾을 수 없습니다.");
            log.info("✅ 다른 사용자 자소서 접근에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("이미 15개 제한 도달 후 생성 시도시 처리")
        void shouldHandleCreateWhenLimitAlreadyReached() {
            log.info("=== 테스트 시작: 이미 15개 제한 도달 후 생성 시도시 처리 ===");

            // given - 프론트 상태 동기화 문제로 제한 도달 후에도 생성 시도하는 경우
            given(interviewService.createInterviewQuestions(coverLetterId, userEmail))
                    .willThrow(new InterviewLimitExceededException("더 이상 질문을 생성할 수 없습니다. (최대 15개)"));
            log.info("Mock 설정: 제한 초과 상태에서 InterviewLimitExceededException 발생");

            // when & then
            log.info("예외 발생 예상 - InterviewLimitExceededException");
            assertThatThrownBy(() ->
                    interviewController.createInterviewQuestions(coverLetterId, mockUserDetails))
                    .isInstanceOf(InterviewLimitExceededException.class)
                    .hasMessage("더 이상 질문을 생성할 수 없습니다. (최대 15개)");
            log.info("✅ 제한 도달 후 생성 시도에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("createInterviewQuestions 메서드")
    class CreateInterviewQuestionsTest {

        @Test
        @DisplayName("정상적인 질문 생성")
        void shouldCreateQuestionsSuccessfully() {
            log.info("=== 테스트 시작: 정상적인 질문 생성 ===");

            // given
            List<InterviewQnaResponse> newQuestions = createMockQnaResponseList(5);
            InterviewQnaListResponse mockResponse = new InterviewQnaListResponse(newQuestions, 5, 5);
            log.info("Mock 응답 생성: qnaList.size()={}, totalCount={}, generatedCount={}",
                    mockResponse.qnaList().size(), mockResponse.totalCount(), mockResponse.generatedCount());

            given(interviewService.createInterviewQuestions(coverLetterId, userEmail))
                    .willReturn(mockResponse);
            log.info("Mock 설정: interviewService.createInterviewQuestions({}, {}) -> mockResponse 반환",
                    coverLetterId, userEmail);

            // when
            log.info("=== 컨트롤러 메서드 실행 ===");
            ResponseEntity<CommonResponse<InterviewQnaListResponse>> result =
                    interviewController.createInterviewQuestions(coverLetterId, mockUserDetails);
            log.info("컨트롤러 응답: statusCode={}, hasBody={}",
                    result.getStatusCode(), result.getBody() != null);

            // then
            log.info("=== 결과 검증 ===");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            log.info("✅ success 필드 검증 통과: {}", result.getBody().isSuccess());

            assertThat(result.getBody().getMessage()).isEqualTo("면접 질문/답변 생성 성공");
            log.info("✅ 메시지 검증 통과: '{}'", result.getBody().getMessage());

            assertThat(result.getBody().getData().qnaList()).hasSize(5);
            log.info("✅ qnaList 크기 검증 통과: {}", result.getBody().getData().qnaList().size());

            assertThat(result.getBody().getData().totalCount()).isEqualTo(5);
            assertThat(result.getBody().getData().generatedCount()).isEqualTo(5);
            log.info("✅ 카운트 필드들 검증 통과: totalCount={}, generatedCount={}",
                    result.getBody().getData().totalCount(), result.getBody().getData().generatedCount());

            verify(interviewService).createInterviewQuestions(coverLetterId, userEmail);
            log.info("✅ 서비스 메서드 호출 검증 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("질문 생성 제한 초과시 예외 발생")
        void shouldThrowExceptionWhenLimitExceeded() {
            log.info("=== 테스트 시작: 질문 생성 제한 초과시 예외 발생 ===");

            // given
            given(interviewService.createInterviewQuestions(coverLetterId, userEmail))
                    .willThrow(new InterviewLimitExceededException("더 이상 질문을 생성할 수 없습니다. (최대 15개)"));
            log.info("Mock 설정: InterviewLimitExceededException 발생하도록 설정");

            // when & then
            log.info("예외 발생 예상 - InterviewLimitExceededException");
            assertThatThrownBy(() ->
                    interviewController.createInterviewQuestions(coverLetterId, mockUserDetails))
                    .isInstanceOf(InterviewLimitExceededException.class)
                    .hasMessage("더 이상 질문을 생성할 수 없습니다. (최대 15개)");
            log.info("✅ 예상된 제한 초과 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("LLM 서비스 오류시 예외 발생")
        void shouldThrowExceptionWhenServiceError() {
            log.info("=== 테스트 시작: LLM 서비스 오류시 예외 발생 ===");

            // given
            given(interviewService.createInterviewQuestions(coverLetterId, userEmail))
                    .willThrow(new InterviewException("질문/답변 생성에 실패했습니다."));
            log.info("Mock 설정: InterviewException 발생하도록 설정");

            // when & then
            log.info("예외 발생 예상 - InterviewException");
            assertThatThrownBy(() ->
                    interviewController.createInterviewQuestions(coverLetterId, mockUserDetails))
                    .isInstanceOf(InterviewException.class)
                    .hasMessage("질문/답변 생성에 실패했습니다.");
            log.info("✅ 예상된 서비스 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("createCustomAnswer 메서드")
    class CreateCustomAnswerTest {

        @Test
        @DisplayName("정상적인 커스텀 답변 생성")
        void shouldCreateCustomAnswerSuccessfully() {
            log.info("=== 테스트 시작: 정상적인 커스텀 답변 생성 ===");

            // given
            CustomQuestionRequest request = createMockCustomQuestionRequest();
            CustomAnswerResponse mockResponse = createMockCustomAnswerResponse();
            log.info("Mock 요청 생성: question='{}'", request.question());
            log.info("Mock 응답 생성: answer='{}', tip='{}'", mockResponse.answer(), mockResponse.tip());

            given(interviewService.createCustomAnswer(coverLetterId, userEmail, request.question()))
                    .willReturn(mockResponse);
            log.info("Mock 설정: interviewService.createCustomAnswer({}, {}, '{}') -> mockResponse 반환",
                    coverLetterId, userEmail, request.question());

            // when
            log.info("=== 컨트롤러 메서드 실행 ===");
            ResponseEntity<CommonResponse<CustomAnswerResponse>> result =
                    interviewController.createCustomAnswer(coverLetterId, request, mockUserDetails);
            log.info("컨트롤러 응답: statusCode={}, hasBody={}",
                    result.getStatusCode(), result.getBody() != null);

            // then
            log.info("=== 결과 검증 ===");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

            assertThat(result.getBody()).isNotNull();
            log.info("✅ 응답 바디 null 아님 검증 통과");

            assertThat(result.getBody().isSuccess()).isTrue();
            log.info("✅ success 필드 검증 통과: {}", result.getBody().isSuccess());

            assertThat(result.getBody().getMessage()).isEqualTo("커스텀 질문 답변 생성 성공");
            log.info("✅ 메시지 검증 통과: '{}'", result.getBody().getMessage());

            assertThat(result.getBody().getData().answer()).isEqualTo(mockResponse.answer());
            log.info("✅ answer 필드 검증 통과: '{}'", result.getBody().getData().answer());

            assertThat(result.getBody().getData().tip()).isEqualTo(mockResponse.tip());
            log.info("✅ tip 필드 검증 통과: '{}'", result.getBody().getData().tip());

            verify(interviewService).createCustomAnswer(coverLetterId, userEmail, request.question());
            log.info("✅ 서비스 메서드 호출 검증 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 때 예외 발생")
        void shouldThrowExceptionWhenMemberNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 사용자일 때 예외 발생 ===");

            // given
            CustomQuestionRequest request = createMockCustomQuestionRequest();
            given(interviewService.createCustomAnswer(coverLetterId, userEmail, request.question()))
                    .willThrow(new MemberNotFoundException("사용자를 찾을 수 없습니다."));
            log.info("Mock 설정: MemberNotFoundException 발생하도록 설정");

            // when & then
            log.info("예외 발생 예상 - MemberNotFoundException");
            assertThatThrownBy(() ->
                    interviewController.createCustomAnswer(coverLetterId, request, mockUserDetails))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");
            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 자소서일 때 예외 발생")
        void shouldThrowExceptionWhenCoverLetterNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 자소서일 때 예외 발생 ===");

            // given
            CustomQuestionRequest request = createMockCustomQuestionRequest();
            given(interviewService.createCustomAnswer(coverLetterId, userEmail, request.question()))
                    .willThrow(new CoverLetterException("자소서를 찾을 수 없습니다."));
            log.info("Mock 설정: CoverLetterException 발생하도록 설정");

            // when & then
            log.info("예외 발생 예상 - CoverLetterException");
            assertThatThrownBy(() ->
                    interviewController.createCustomAnswer(coverLetterId, request, mockUserDetails))
                    .isInstanceOf(CoverLetterException.class)
                    .hasMessage("자소서를 찾을 수 없습니다.");
            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("LLM 서비스 오류시 예외 발생")
        void shouldThrowExceptionWhenServiceError() {
            log.info("=== 테스트 시작: LLM 서비스 오류시 예외 발생 ===");

            // given
            CustomQuestionRequest request = createMockCustomQuestionRequest();
            given(interviewService.createCustomAnswer(coverLetterId, userEmail, request.question()))
                    .willThrow(new InterviewException("커스텀 질문 답변 생성에 실패했습니다."));
            log.info("Mock 설정: InterviewException 발생하도록 설정");

            // when & then
            log.info("예외 발생 예상 - InterviewException");
            assertThatThrownBy(() ->
                    interviewController.createCustomAnswer(coverLetterId, request, mockUserDetails))
                    .isInstanceOf(InterviewException.class)
                    .hasMessage("커스텀 질문 답변 생성에 실패했습니다.");
            log.info("✅ 예상된 서비스 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    // ================ 테스트 헬퍼 메서드들 ================

    private List<InterviewQnaResponse> createMockQnaResponseList(int count) {
        log.debug("가짜 질문 응답 목록 생성 시작: {}개", count);
        List<InterviewQnaResponse> qnaList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= count; i++) {
            InterviewQnaResponse response = new InterviewQnaResponse(
                    (long) i,
                    "질문 " + i,
                    "답변 " + i,
                    "팁 " + i,
                    now.plusMinutes(i)
            );

            qnaList.add(response);
            log.debug("가짜 질문 응답 {}번 생성: qnaId={}, question='{}', answer='{}', tip='{}'",
                    i, response.qnaId(), response.question(), response.answer(), response.tip());
        }

        log.debug("가짜 질문 응답 목록 생성 완료: 총 {}개", qnaList.size());
        return qnaList;
    }

    private CustomQuestionRequest createMockCustomQuestionRequest() {
        log.debug("가짜 커스텀 질문 요청 생성");
        return new CustomQuestionRequest("면접에서 가장 중요하게 생각하는 가치는 무엇인가요?");
    }

    private CustomAnswerResponse createMockCustomAnswerResponse() {
        log.debug("가짜 커스텀 답변 응답 생성");
        return new CustomAnswerResponse(
                "저는 팀워크와 지속적인 학습을 가장 중요하게 생각합니다. 자소서에서 언급한 팀 프로젝트 경험을 통해...",
                "구체적인 경험 사례와 함께 개인의 가치관을 명확히 표현하세요."
        );
    }
}
