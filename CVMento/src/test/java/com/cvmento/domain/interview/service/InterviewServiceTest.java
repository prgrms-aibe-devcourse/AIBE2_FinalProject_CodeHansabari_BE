package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.dto.request.ContentItem;
import com.cvmento.domain.coverLetter.dto.request.InputItem;
import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.interview.dto.response.CustomAnswerResponse;
import com.cvmento.domain.interview.dto.response.InterviewLlmResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaDto;
import com.cvmento.domain.interview.dto.response.InterviewQnaListResponse;
import com.cvmento.domain.interview.entity.CoverLetterQna;
import com.cvmento.domain.interview.enums.QuestionSourceType;
import com.cvmento.domain.interview.repository.CoverLetterQnaRepository;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.common.services.MetricsService;
import com.cvmento.global.exception.customException.CoverLetterException;
import com.cvmento.global.exception.customException.InterviewException;
import com.cvmento.global.exception.customException.InterviewLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * InterviewService의 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewService 단위 테스트")
@Slf4j
class InterviewServiceTest {

    @Mock
    private CoverLetterRepository coverLetterRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CoverLetterQnaRepository coverLetterQnaRepository;
    @Mock
    private InterviewLlmPromptService promptService;
    @Mock
    private InterviewLlmClientService llmClientService;
    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private InterviewService interviewService;

    private Member testMember;
    private CoverLetter testCoverLetter;
    private Long coverLetterId = 1L;
    private String memberEmail = "test@example.com";

    @BeforeEach
    void setUp() throws Exception {
        log.info("=== 테스트 데이터 설정 시작 ===");

        testMember = new Member("google123", memberEmail, "테스트 사용자", "profile.jpg");
        setField(testMember, "memberId", 1L);
        log.info("테스트 Member 생성 완료: email={}, name={}, memberId={}",
                testMember.getEmail(), testMember.getName(), 1L);

        testCoverLetter = new CoverLetter(
                "백엔드 개발자 자소서",
                "Spring Boot를 활용한 백엔드 개발 경험...",
                "백엔드 개발",
                0,
                testMember
        );
        setField(testCoverLetter, "coverLetterId", coverLetterId);
        log.info("테스트 CoverLetter 생성 완료: title={}, jobField={}, experienceYears={}, coverLetterId={}",
                testCoverLetter.getTitle(), testCoverLetter.getJobField(),
                testCoverLetter.getExperienceYears(), coverLetterId);

        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("getExistingInterviewQna 메서드")
    class GetExistingInterviewQnaTest {

        @Test
        @DisplayName("기존 질문이 있을 때 정상 반환")
        void shouldReturnExistingQuestions() {
            log.info("=== 테스트 시작: 기존 질문이 있을 때 정상 반환 ===");

            // given
            List<CoverLetterQna> existingQnas = createMockQnaList(5);
            log.info("생성된 가짜 질문 개수: {}", existingQnas.size());

            given(coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                    any(Long.class), any(String.class), any(CoverLetterStatus.class)))
                    .willReturn(Optional.of(testCoverLetter));
            log.info("Mock 설정: coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(any, any, any) -> testCoverLetter 반환");

            given(coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(any(CoverLetter.class)))
                    .willReturn(existingQnas);
            log.info("Mock 설정: coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(any) -> {}개 질문 반환",
                    existingQnas.size());

            // when
            log.info("=== 메서드 실행 ===");
            InterviewQnaListResponse result = interviewService.getExistingInterviewQna(coverLetterId, memberEmail);
            log.info("메서드 실행 결과: qnaList.size()={}, totalCount={}, generatedCount={}",
                    result.qnaList().size(), result.totalCount(), result.generatedCount());

            // then
            log.info("=== 결과 검증 ===");
            assertThat(result.qnaList()).hasSize(5);
            log.info("✅ qnaList 크기 검증 통과: {}", result.qnaList().size());

            assertThat(result.totalCount()).isEqualTo(5);
            log.info("✅ totalCount 검증 통과: {}", result.totalCount());

            assertThat(result.generatedCount()).isEqualTo(5);
            log.info("✅ generatedCount 검증 통과: {}", result.generatedCount());

            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("기존 질문이 없을 때 빈 배열 반환")
        void shouldReturnEmptyListWhenNoQuestions() {
            log.info("=== 테스트 시작: 기존 질문이 없을 때 빈 배열 반환 ===");

            // given
            given(coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                    any(Long.class), any(String.class), any(CoverLetterStatus.class)))
                    .willReturn(Optional.of(testCoverLetter));
            given(coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(any(CoverLetter.class)))
                    .willReturn(new ArrayList<>());
            log.info("Mock 설정: 빈 배열 반환하도록 설정");

            // when
            InterviewQnaListResponse result = interviewService.getExistingInterviewQna(coverLetterId, memberEmail);
            log.info("메서드 실행 결과: qnaList.size()={}, totalCount={}, generatedCount={}",
                    result.qnaList().size(), result.totalCount(), result.generatedCount());

            // then
            assertThat(result.qnaList()).isEmpty();
            assertThat(result.totalCount()).isEqualTo(0);
            assertThat(result.generatedCount()).isEqualTo(0);
            log.info("✅ 빈 배열 반환 검증 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 자소서일 때 예외 발생")
        void shouldThrowExceptionWhenCoverLetterNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 자소서일 때 예외 발생 ===");

            // given
            given(coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                    any(Long.class), any(String.class), any(CoverLetterStatus.class)))
                    .willReturn(Optional.empty());
            log.info("Mock 설정: coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(any, any, any) -> Optional.empty() 반환");

            // when & then
            log.info("예외 발생 예상 - CoverLetterException");
            assertThatThrownBy(() ->
                    interviewService.getExistingInterviewQna(coverLetterId, memberEmail))
                    .isInstanceOf(CoverLetterException.class)
                    .hasMessage("자소서를 찾을 수 없습니다.");
            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("createInterviewQuestions 메서드")
    class CreateInterviewQuestionsTest {

        @BeforeEach
        void setUp() {
            log.info("--- CreateInterviewQuestionsTest 공통 Mock 설정 ---");
            given(coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                    any(Long.class), any(String.class), any(CoverLetterStatus.class)))
                    .willReturn(Optional.of(testCoverLetter));
            log.info("CoverLetter 조회 Mock 설정 완료 (any 매처 사용)\n");
        }

        @Test
        @DisplayName("질문이 0개일 때 초기 5개 질문 생성")
        void shouldCreateInitialQuestionsWhenNoneExist() {
            log.info("=== 테스트 시작: 질문이 0개일 때 초기 5개 질문 생성 ===");

            // given
            given(coverLetterQnaRepository.countByCoverLetterAndSourceType(any(CoverLetter.class), any(QuestionSourceType.class)))
                    .willReturn(0L);
            log.info("Mock 설정: 기존 질문 개수 = 0개");

            // InputItem 목록 생성
            List<InputItem> mockInputItems = createMockInputItems();
            given(promptService.buildQnaGenerationInputItems(any(CoverLetter.class)))
                    .willReturn(mockInputItems);
            log.info("Mock 설정: 생성된 InputItems 개수 = {}", mockInputItems.size());

            InterviewLlmResponse mockResponse = createMockLlmResponse();
            given(llmClientService.generateQnaList(anyList()))
                    .willReturn(mockResponse);
            log.info("Mock 설정: LLM 응답 질문 개수 = {}개", mockResponse.qnaList().size());

            // ArgumentCaptor를 사용하여 저장되는 객체들 캡처
            ArgumentCaptor<CoverLetterQna> qnaCaptor = ArgumentCaptor.forClass(CoverLetterQna.class);
            given(coverLetterQnaRepository.save(qnaCaptor.capture()))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            log.info("=== 메서드 실행 ===");
            InterviewQnaListResponse result = interviewService.createInterviewQuestions(coverLetterId, memberEmail);

            // then
            log.info("=== 결과 검증 ===");
            log.info("반환된 응답: qnaList.size()={}, totalCount={}, generatedCount={}",
                    result.qnaList().size(), result.totalCount(), result.generatedCount());

            // 저장된 객체들 상세 로깅
            List<CoverLetterQna> savedQnas = qnaCaptor.getAllValues();
            log.info("실제로 저장된 질문 개수: {}", savedQnas.size());
            for (int i = 0; i < savedQnas.size(); i++) {
                CoverLetterQna qna = savedQnas.get(i);
                log.info("저장된 질문 {}: question='{}', answer='{}', tip='{}'",
                        i + 1, qna.getQuestion(), qna.getAnswer(), qna.getTip());
            }

            assertThat(result.qnaList()).hasSize(5);
            assertThat(result.totalCount()).isEqualTo(5);
            assertThat(result.generatedCount()).isEqualTo(5);

            // 메서드 호출 검증
            log.info("=== 메서드 호출 검증 ===");
            verify(promptService).buildQnaGenerationInputItems(any(CoverLetter.class));
            log.info("✅ promptService.buildQnaGenerationInputItems() 호출 확인");

            verify(llmClientService).generateQnaList(anyList());
            log.info("✅ llmClientService.generateQnaList(inputItems) 호출 확인");

            verify(coverLetterQnaRepository, times(5)).save(any(CoverLetterQna.class));
            log.info("✅ coverLetterQnaRepository.save() 5번 호출 확인");

            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("질문이 5개일 때 추가 5개 질문 생성")
        void shouldCreateAdditionalQuestionsWhenSomeExist() {
            log.info("=== 테스트 시작: 질문이 5개일 때 추가 5개 질문 생성 ===");

            // given
            List<String> existingQuestions = List.of("질문1", "질문2", "질문3", "질문4", "질문5");
            log.info("기존 질문 목록: {}", existingQuestions);

            given(coverLetterQnaRepository.countByCoverLetterAndSourceType(any(CoverLetter.class), any(QuestionSourceType.class)))
                    .willReturn(5L);
            log.info("Mock 설정: 기존 질문 개수 = 5개");

            given(coverLetterQnaRepository.findQuestionsByCoverLetterAndSourceType(any(CoverLetter.class), any(QuestionSourceType.class)))
                    .willReturn(existingQuestions);
            log.info("Mock 설정: 기존 질문 목록 반환");

            List<InputItem> additionalInputItems = createMockInputItems();
            given(promptService.buildAdditionalQnaInputItems(any(CoverLetter.class), anyList()))
                    .willReturn(additionalInputItems);
            log.info("Mock 설정: 추가 질문용 InputItems 개수 = {}", additionalInputItems.size());

            InterviewLlmResponse mockResponse = createMockLlmResponse();
            given(llmClientService.generateQnaList(anyList()))
                    .willReturn(mockResponse);
            log.info("Mock 설정: LLM 추가 응답 질문 개수 = {}개", mockResponse.qnaList().size());

            given(coverLetterQnaRepository.save(any(CoverLetterQna.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            log.info("=== 메서드 실행 ===");
            InterviewQnaListResponse result = interviewService.createInterviewQuestions(coverLetterId, memberEmail);

            // then
            log.info("=== 결과 검증 ===");
            log.info("반환된 응답: qnaList.size()={}, totalCount={}, generatedCount={}",
                    result.qnaList().size(), result.totalCount(), result.generatedCount());

            assertThat(result.qnaList()).hasSize(5);
            assertThat(result.totalCount()).isEqualTo(5);
            assertThat(result.generatedCount()).isEqualTo(5);

            verify(promptService).buildAdditionalQnaInputItems(any(CoverLetter.class), anyList());
            log.info("✅ buildAdditionalQnaInputItems() 호출 확인");

            verify(llmClientService).generateQnaList(anyList());
            log.info("✅ generateQnaList(inputItems) 호출 확인");

            verify(coverLetterQnaRepository, times(5)).save(any(CoverLetterQna.class));
            log.info("✅ save() 5번 호출 확인");

            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("질문이 15개일 때 제한 초과 예외 발생")
        void shouldThrowExceptionWhenLimitExceeded() {
            log.info("=== 테스트 시작: 질문이 15개일 때 제한 초과 예외 발생 ===");

            // given
            given(coverLetterQnaRepository.countByCoverLetterAndSourceType(any(CoverLetter.class), any(QuestionSourceType.class)))
                    .willReturn(15L);
            log.info("Mock 설정: 기존 질문 개수 = 15개 (최대 제한)");

            // when & then
            log.info("예외 발생 예상 - InterviewLimitExceededException");
            assertThatThrownBy(() ->
                    interviewService.createInterviewQuestions(coverLetterId, memberEmail))
                    .isInstanceOf(InterviewLimitExceededException.class)
                    .hasMessage("더 이상 질문을 생성할 수 없습니다. (최대 15개)");
            log.info("✅ 예상된 제한 초과 예외 발생 확인");

            // 호출되지 않아야 하는 메서드들 검증
            verify(llmClientService, never()).generateQnaList(any());
            verify(coverLetterQnaRepository, never()).save(any());
            log.info("✅ LLM 서비스와 저장 메서드가 호출되지 않음을 확인");

            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("LLM 서비스 실패시 예외 발생")
        void shouldThrowExceptionWhenLlmServiceFails() {
            log.info("=== 테스트 시작: LLM 서비스 실패시 예외 발생 ===");

            // given
            given(coverLetterQnaRepository.countByCoverLetterAndSourceType(any(CoverLetter.class), any(QuestionSourceType.class)))
                    .willReturn(0L);
            log.info("Mock 설정: 기존 질문 개수 = 0개");

            List<InputItem> inputItems = createMockInputItems();
            given(promptService.buildQnaGenerationInputItems(any(CoverLetter.class)))
                    .willReturn(inputItems);
            log.info("Mock 설정: 생성된 InputItems");

            RuntimeException llmException = new RuntimeException("LLM API 호출 실패");
            given(llmClientService.generateQnaList(anyList()))
                    .willThrow(llmException);
            log.info("Mock 설정: LLM 서비스에서 예외 발생 - '{}'", llmException.getMessage());

            // when & then
            log.info("예외 발생 예상 - InterviewException");
            assertThatThrownBy(() ->
                    interviewService.createInterviewQuestions(coverLetterId, memberEmail))
                    .isInstanceOf(InterviewException.class)
                    .hasMessage("질문/답변 생성에 실패했습니다.");
            log.info("✅ 예상된 InterviewException 발생 확인");

            verify(coverLetterQnaRepository, never()).save(any());
            log.info("✅ 저장 메서드가 호출되지 않음을 확인 (LLM 실패로 인해)");

            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("createCustomAnswer 메서드")
    class CreateCustomAnswerTest {

        @BeforeEach
        void setUp() {
            log.info("--- CreateCustomAnswerTest 공통 Mock 설정 ---");
            given(coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                    any(Long.class), any(String.class), any(CoverLetterStatus.class)))
                    .willReturn(Optional.of(testCoverLetter));
            log.info("CoverLetter 조회 Mock 설정 완료 (any 매처 사용)\n");
        }

        @Test
        @DisplayName("커스텀 질문 답변 생성 및 저장 성공")
        void shouldCreateCustomAnswerSuccessfully() {
            log.info("=== 테스트 시작: 커스텀 질문 답변 생성 및 저장 성공 ===");

            // given
            String customQuestion = "면접에서 가장 중요하게 생각하는 가치는 무엇인가요?";
            log.info("커스텀 질문: '{}'", customQuestion);

            List<InputItem> mockInputItems = createMockInputItems();
            given(promptService.buildCustomAnswerInputItems(any(CoverLetter.class), anyString()))
                    .willReturn(mockInputItems);
            log.info("Mock 설정: 생성된 InputItems 개수 = {}", mockInputItems.size());

            CustomAnswerResponse mockResponse = createMockCustomAnswerResponse();
            given(llmClientService.generateCustomAnswer(anyList()))
                    .willReturn(mockResponse);
            log.info("Mock 설정: LLM 응답 answer='{}', tip='{}'",
                    mockResponse.answer(), mockResponse.tip());

            // ArgumentCaptor를 사용하여 저장되는 객체 캡처
            ArgumentCaptor<CoverLetterQna> qnaCaptor = ArgumentCaptor.forClass(CoverLetterQna.class);
            given(coverLetterQnaRepository.save(qnaCaptor.capture()))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            log.info("=== 메서드 실행 ===");
            CustomAnswerResponse result = interviewService.createCustomAnswer(coverLetterId, memberEmail, customQuestion);

            // then
            log.info("=== 결과 검증 ===");
            log.info("반환된 응답: answer='{}', tip='{}'", result.answer(), result.tip());

            assertThat(result.answer()).isEqualTo(mockResponse.answer());
            log.info("✅ answer 필드 검증 통과: '{}'", result.answer());

            assertThat(result.tip()).isEqualTo(mockResponse.tip());
            log.info("✅ tip 필드 검증 통과: '{}'", result.tip());

            // 저장된 객체 상세 검증
            CoverLetterQna savedQna = qnaCaptor.getValue();
            log.info("실제로 저장된 질문: question='{}', answer='{}', tip='{}', sourceType='{}'",
                    savedQna.getQuestion(), savedQna.getAnswer(), savedQna.getTip(), savedQna.getSourceType());

            assertThat(savedQna.getQuestion()).isEqualTo(customQuestion);
            log.info("✅ 저장된 질문 검증 통과");

            assertThat(savedQna.getAnswer()).isEqualTo(mockResponse.answer());
            log.info("✅ 저장된 답변 검증 통과");

            assertThat(savedQna.getTip()).isEqualTo(mockResponse.tip());
            log.info("✅ 저장된 팁 검증 통과");

            assertThat(savedQna.getSourceType()).isEqualTo(QuestionSourceType.CUSTOM);
            log.info("✅ sourceType이 CUSTOM으로 저장됨 검증 통과");

            // 메서드 호출 검증
            log.info("=== 메서드 호출 검증 ===");
            verify(promptService).buildCustomAnswerInputItems(any(CoverLetter.class), anyString());
            log.info("✅ promptService.buildCustomAnswerInputItems() 호출 확인");

            verify(llmClientService).generateCustomAnswer(anyList());
            log.info("✅ llmClientService.generateCustomAnswer(inputItems) 호출 확인");

            verify(coverLetterQnaRepository).save(any(CoverLetterQna.class));
            log.info("✅ coverLetterQnaRepository.save() 호출 확인");

            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("LLM 서비스 실패시 예외 발생")
        void shouldThrowExceptionWhenLlmServiceFails() {
            log.info("=== 테스트 시작: LLM 서비스 실패시 예외 발생 ===");

            // given
            String customQuestion = "테스트 질문";
            List<InputItem> mockInputItems = createMockInputItems();

            given(promptService.buildCustomAnswerInputItems(any(CoverLetter.class), anyString()))
                    .willReturn(mockInputItems);
            log.info("Mock 설정: InputItems 생성");

            RuntimeException llmException = new RuntimeException("LLM 서비스 실패");
            given(llmClientService.generateCustomAnswer(anyList()))
                    .willThrow(llmException);
            log.info("Mock 설정: LLM 서비스에서 예외 발생 - '{}'", llmException.getMessage());

            // when & then
            log.info("예외 발생 예상 - InterviewException");
            assertThatThrownBy(() ->
                    interviewService.createCustomAnswer(coverLetterId, memberEmail, customQuestion))
                    .isInstanceOf(InterviewException.class)
                    .hasMessage("커스텀 질문 답변 생성에 실패했습니다.");
            log.info("✅ 예상된 InterviewException 발생 확인");

            verify(coverLetterQnaRepository, never()).save(any());
            log.info("✅ 저장 메서드가 호출되지 않음을 확인 (LLM 실패로 인해)");

            log.info("=== 테스트 완료 ===\n");
        }
    }

    // ================ 테스트 헬퍼 메서드들 ================

    private List<CoverLetterQna> createMockQnaList(int count) {
        log.debug("가짜 질문 목록 생성 시작: {}개", count);
        List<CoverLetterQna> qnaList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            CoverLetterQna qna = new CoverLetterQna("질문 " + i, testCoverLetter, QuestionSourceType.GENERATED);
            qna.updateAnswerAndTip("답변 " + i, "팁 " + i);
            qnaList.add(qna);
            log.debug("가짜 질문 {}번 생성: question='질문 {}', answer='답변 {}', tip='팁 {}'", i, i, i, i);
        }
        log.debug("가짜 질문 목록 생성 완료: 총 {}개", qnaList.size());
        return qnaList;
    }

    private InterviewLlmResponse createMockLlmResponse() {
        log.debug("가짜 LLM 응답 생성 시작");
        List<InterviewQnaDto> qnaList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            InterviewQnaDto dto = new InterviewQnaDto(
                    "AI 생성 질문 " + i,
                    "AI 생성 답변 " + i,
                    "AI 생성 팁 " + i
            );
            qnaList.add(dto);
            log.debug("가짜 LLM 질문 {}번 생성: question='AI 생성 질문 {}', answer='AI 생성 답변 {}', tip='AI 생성 팁 {}'", i, i, i, i);
        }
        InterviewLlmResponse response = new InterviewLlmResponse(qnaList);
        log.debug("가짜 LLM 응답 생성 완료: 총 {}개 질문", qnaList.size());
        return response;
    }

    private List<InputItem> createMockInputItems() {
        log.debug("가짜 InputItem 목록 생성");
        List<InputItem> inputItems = new ArrayList<>();

        // 시스템 메시지
        inputItems.add(new InputItem("system",
                List.of(ContentItem.text("면접 질문을 생성하는 AI 어시스턴트입니다."))));

        // 사용자 메시지 (자소서 내용 포함)
        inputItems.add(new InputItem("user",
                List.of(ContentItem.text("다음 자소서를 바탕으로 면접 질문을 생성해주세요: " + testCoverLetter.getContent()))));

        log.debug("가짜 InputItem 목록 생성 완료: {}개", inputItems.size());
        return inputItems;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
        log.debug("리플렉션으로 필드 설정: {}.{} = {}", target.getClass().getSimpleName(), fieldName, value);
    }

    private CustomAnswerResponse createMockCustomAnswerResponse() {
        log.debug("가짜 커스텀 답변 응답 생성");
        return new CustomAnswerResponse(
                "저는 팀워크와 지속적인 학습을 가장 중요하게 생각합니다. 자소서에서 언급한 팀 프로젝트 경험을 통해...",
                "구체적인 경험 사례와 함께 개인의 가치관을 명확히 표현하세요."
        );
    }
}