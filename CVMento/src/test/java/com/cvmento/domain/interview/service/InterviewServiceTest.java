package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
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
 * InterviewService의 단위 테스트 (상세 로깅 버전)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewService 단위 테스트")
@Slf4j  // 로깅을 위한 어노테이션
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

    @InjectMocks
    private InterviewService interviewService;

    private Member testMember;
    private CoverLetter testCoverLetter;
    private Long coverLetterId = 1L;
    private String userEmail = "test@example.com";

    @BeforeEach
    void setUp() throws Exception {
        log.info("=== 테스트 데이터 설정 시작 ===");

        testMember = new Member("google123", userEmail, "테스트 사용자", "profile.jpg");
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

            // Mock 설정 로깅
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            log.info("Mock 설정: memberRepository.findByEmail({}) -> testMember 반환", userEmail);

            given(coverLetterRepository.findByCoverLetterIdAndMember(coverLetterId, testMember))
                    .willReturn(Optional.of(testCoverLetter));
            log.info("Mock 설정: coverLetterRepository.findByCoverLetterIdAndMember({}, testMember) -> testCoverLetter 반환", coverLetterId);

            given(coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(testCoverLetter))
                    .willReturn(existingQnas);
            log.info("Mock 설정: coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(testCoverLetter) -> {}개 질문 반환", existingQnas.size());

            // when
            log.info("=== 메서드 실행 ===");
            InterviewQnaListResponse result = interviewService.getExistingInterviewQna(coverLetterId, userEmail);
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
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            given(coverLetterRepository.findByCoverLetterIdAndMember(coverLetterId, testMember))
                    .willReturn(Optional.of(testCoverLetter));
            given(coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(testCoverLetter))
                    .willReturn(new ArrayList<>());
            log.info("Mock 설정: 빈 배열 반환하도록 설정");

            // when
            InterviewQnaListResponse result = interviewService.getExistingInterviewQna(coverLetterId, userEmail);
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
        @DisplayName("존재하지 않는 사용자일 때 예외 발생")
        void shouldThrowExceptionWhenMemberNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 사용자일 때 예외 발생 ===");

            // given
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.empty());
            log.info("Mock 설정: memberRepository.findByEmail({}) -> Optional.empty() 반환", userEmail);

            // when & then
            log.info("예외 발생 예상 - MemberNotFoundException");
            assertThatThrownBy(() ->
                    interviewService.getExistingInterviewQna(coverLetterId, userEmail))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");
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
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            given(coverLetterRepository.findByCoverLetterIdAndMember(coverLetterId, testMember))
                    .willReturn(Optional.of(testCoverLetter));
            log.info("Member와 CoverLetter 조회 Mock 설정 완료\n");
        }

        @Test
        @DisplayName("질문이 0개일 때 초기 5개 질문 생성")
        void shouldCreateInitialQuestionsWhenNoneExist() {
            log.info("=== 테스트 시작: 질문이 0개일 때 초기 5개 질문 생성 ===");

            // given
            given(coverLetterQnaRepository.countByCoverLetterAndSourceType(testCoverLetter, QuestionSourceType.GENERATED))
                    .willReturn(0L);
            log.info("Mock 설정: 기존 질문 개수 = 0개");

            String mockPrompt = "초기 프롬프트";
            given(promptService.buildQnaGenerationPrompt(testCoverLetter))
                    .willReturn(mockPrompt);
            log.info("Mock 설정: 생성된 프롬프트 = '{}'", mockPrompt);

            InterviewLlmResponse mockResponse = createMockLlmResponse();
            given(llmClientService.generateQnaList(mockPrompt))
                    .willReturn(mockResponse);
            log.info("Mock 설정: LLM 응답 질문 개수 = {}개", mockResponse.qnaList().size());

            // ArgumentCaptor를 사용하여 저장되는 객체들 캡처
            ArgumentCaptor<CoverLetterQna> qnaCaptor = ArgumentCaptor.forClass(CoverLetterQna.class);
            given(coverLetterQnaRepository.save(qnaCaptor.capture()))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            log.info("=== 메서드 실행 ===");
            InterviewQnaListResponse result = interviewService.createInterviewQuestions(coverLetterId, userEmail);

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
            verify(promptService).buildQnaGenerationPrompt(testCoverLetter);
            log.info("✅ promptService.buildQnaGenerationPrompt() 호출 확인");

            verify(llmClientService).generateQnaList(mockPrompt);
            log.info("✅ llmClientService.generateQnaList('{}') 호출 확인", mockPrompt);

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

            given(coverLetterQnaRepository.countByCoverLetterAndSourceType(testCoverLetter, QuestionSourceType.GENERATED))
                    .willReturn(5L);
            log.info("Mock 설정: 기존 질문 개수 = 5개");

            given(coverLetterQnaRepository.findQuestionsByCoverLetterAndSourceType(testCoverLetter, QuestionSourceType.GENERATED))
                    .willReturn(existingQuestions);
            log.info("Mock 설정: 기존 질문 목록 반환");

            String additionalPrompt = "추가 프롬프트";
            given(promptService.buildAdditionalQnaPrompt(testCoverLetter, existingQuestions))
                    .willReturn(additionalPrompt);
            log.info("Mock 설정: 추가 질문용 프롬프트 = '{}'", additionalPrompt);

            InterviewLlmResponse mockResponse = createMockLlmResponse();
            given(llmClientService.generateQnaList(additionalPrompt))
                    .willReturn(mockResponse);
            log.info("Mock 설정: LLM 추가 응답 질문 개수 = {}개", mockResponse.qnaList().size());

            given(coverLetterQnaRepository.save(any(CoverLetterQna.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            log.info("=== 메서드 실행 ===");
            InterviewQnaListResponse result = interviewService.createInterviewQuestions(coverLetterId, userEmail);

            // then
            log.info("=== 결과 검증 ===");
            log.info("반환된 응답: qnaList.size()={}, totalCount={}, generatedCount={}",
                    result.qnaList().size(), result.totalCount(), result.generatedCount());

            assertThat(result.qnaList()).hasSize(5);
            assertThat(result.totalCount()).isEqualTo(5);
            assertThat(result.generatedCount()).isEqualTo(5);

            verify(promptService).buildAdditionalQnaPrompt(testCoverLetter, existingQuestions);
            log.info("✅ buildAdditionalQnaPrompt() 호출 확인");

            verify(llmClientService).generateQnaList(additionalPrompt);
            log.info("✅ generateQnaList('{}') 호출 확인", additionalPrompt);

            verify(coverLetterQnaRepository, times(5)).save(any(CoverLetterQna.class));
            log.info("✅ save() 5번 호출 확인");

            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("질문이 15개일 때 제한 초과 예외 발생")
        void shouldThrowExceptionWhenLimitExceeded() {
            log.info("=== 테스트 시작: 질문이 15개일 때 제한 초과 예외 발생 ===");

            // given
            given(coverLetterQnaRepository.countByCoverLetterAndSourceType(testCoverLetter, QuestionSourceType.GENERATED))
                    .willReturn(15L);
            log.info("Mock 설정: 기존 질문 개수 = 15개 (최대 제한)");

            // when & then
            log.info("예외 발생 예상 - InterviewLimitExceededException");
            assertThatThrownBy(() ->
                    interviewService.createInterviewQuestions(coverLetterId, userEmail))
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
            given(coverLetterQnaRepository.countByCoverLetterAndSourceType(testCoverLetter, QuestionSourceType.GENERATED))
                    .willReturn(0L);
            log.info("Mock 설정: 기존 질문 개수 = 0개");

            String prompt = "프롬프트";
            given(promptService.buildQnaGenerationPrompt(testCoverLetter))
                    .willReturn(prompt);
            log.info("Mock 설정: 생성된 프롬프트 = '{}'", prompt);

            RuntimeException llmException = new RuntimeException("LLM API 호출 실패");
            given(llmClientService.generateQnaList(prompt))
                    .willThrow(llmException);
            log.info("Mock 설정: LLM 서비스에서 예외 발생 - '{}'", llmException.getMessage());

            // when & then
            log.info("예외 발생 예상 - InterviewException");
            assertThatThrownBy(() ->
                    interviewService.createInterviewQuestions(coverLetterId, userEmail))
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
            given(coverLetterRepository.findByCoverLetterIdAndMemberEmail(coverLetterId, userEmail))
                    .willReturn(Optional.of(testCoverLetter));
            log.info("CoverLetter 조회 Mock 설정 완료\n");
        }

        @Test
        @DisplayName("커스텀 질문 답변 생성 및 저장 성공")
        void shouldCreateCustomAnswerSuccessfully() {
            log.info("=== 테스트 시작: 커스텀 질문 답변 생성 및 저장 성공 ===");

            // given
            String customQuestion = "면접에서 가장 중요하게 생각하는 가치는 무엇인가요?";
            log.info("커스텀 질문: '{}'", customQuestion);

            String mockPrompt = "커스텀 답변 프롬프트";
            given(promptService.buildCustomAnswerPrompt(testCoverLetter, customQuestion))
                    .willReturn(mockPrompt);
            log.info("Mock 설정: 생성된 프롬프트 = '{}'", mockPrompt);

            CustomAnswerResponse mockResponse = createMockCustomAnswerResponse();
            given(llmClientService.generateCustomAnswer(mockPrompt))
                    .willReturn(mockResponse);
            log.info("Mock 설정: LLM 응답 answer='{}', tip='{}'",
                    mockResponse.answer(), mockResponse.tip());

            // ArgumentCaptor를 사용하여 저장되는 객체 캡처
            ArgumentCaptor<CoverLetterQna> qnaCaptor = ArgumentCaptor.forClass(CoverLetterQna.class);
            given(coverLetterQnaRepository.save(qnaCaptor.capture()))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            log.info("=== 메서드 실행 ===");
            CustomAnswerResponse result = interviewService.createCustomAnswer(coverLetterId, userEmail, customQuestion);

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
            verify(promptService).buildCustomAnswerPrompt(testCoverLetter, customQuestion);
            log.info("✅ promptService.buildCustomAnswerPrompt() 호출 확인");

            verify(llmClientService).generateCustomAnswer(mockPrompt);
            log.info("✅ llmClientService.generateCustomAnswer('{}') 호출 확인", mockPrompt);

            verify(coverLetterQnaRepository).save(any(CoverLetterQna.class));
            log.info("✅ coverLetterQnaRepository.save() 호출 확인");

            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("LLM이 JSON이 아닌 응답을 보낼 때")
        void shouldThrowExceptionWhenLlmReturnsInvalidJson() {
            log.info("=== 테스트 시작: LLM이 JSON이 아닌 응답을 보낼 때 ===");

            // given
            String customQuestion = "테스트 질문";
            String mockPrompt = "프롬프트";

            given(promptService.buildCustomAnswerPrompt(testCoverLetter, customQuestion))
                    .willReturn(mockPrompt);
            log.info("Mock 설정: 프롬프트 생성");

            // LLM이 일반 텍스트 응답을 보내는 상황
            given(llmClientService.generateCustomAnswer(mockPrompt))
                    .willThrow(new InterviewException("커스텀 답변 데이터 파싱에 실패했습니다."));
            log.info("Mock 설정: LLM이 파싱 불가능한 응답 반환");

            // when & then
            log.info("예외 발생 예상 - InterviewException");
            assertThatThrownBy(() ->
                    interviewService.createCustomAnswer(coverLetterId, userEmail, customQuestion))
                    .isInstanceOf(InterviewException.class)
                    .hasMessage("커스텀 질문 답변 생성에 실패했습니다.");
            log.info("✅ 예상된 JSON 파싱 예외 발생 확인");

            verify(coverLetterQnaRepository, never()).save(any());
            log.info("✅ 저장 메서드가 호출되지 않음을 확인 (파싱 실패로 인해)");

            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("LLM 응답에 answer 필드가 없을 때")
        void shouldThrowExceptionWhenAnswerFieldMissing() {
            log.info("=== 테스트 시작: LLM 응답에 answer 필드가 없을 때 ===");

            // given
            String customQuestion = "테스트 질문";
            String mockPrompt = "프롬프트";

            given(promptService.buildCustomAnswerPrompt(testCoverLetter, customQuestion))
                    .willReturn(mockPrompt);
            log.info("Mock 설정: 프롬프트 생성");

            // LLM이 answer 필드가 누락된 응답을 보내는 상황
            given(llmClientService.generateCustomAnswer(mockPrompt))
                    .willThrow(new InterviewException("커스텀 답변 데이터 파싱에 실패했습니다."));
            log.info("Mock 설정: LLM이 answer 필드 누락된 응답 반환");

            // when & then
            log.info("예외 발생 예상 - InterviewException");
            assertThatThrownBy(() ->
                    interviewService.createCustomAnswer(coverLetterId, userEmail, customQuestion))
                    .isInstanceOf(InterviewException.class)
                    .hasMessage("커스텀 질문 답변 생성에 실패했습니다.");
            log.info("✅ 예상된 필드 누락 예외 발생 확인");

            verify(coverLetterQnaRepository, never()).save(any());
            log.info("✅ 저장 메서드가 호출되지 않음을 확인 (필드 누락으로 인해)");

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