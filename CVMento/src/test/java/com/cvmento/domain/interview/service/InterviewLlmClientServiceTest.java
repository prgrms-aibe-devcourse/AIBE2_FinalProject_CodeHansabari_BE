package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.dto.request.ContentItem;
import com.cvmento.domain.coverLetter.dto.request.InputItem;
import com.cvmento.domain.interview.client.InterviewLlmFeignClient;
import com.cvmento.domain.interview.dto.response.CustomAnswerResponse;
import com.cvmento.domain.interview.dto.response.InterviewLlmResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaDto;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.AiInvalidRequestException;
import com.cvmento.global.exception.customException.InterviewException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * InterviewLlmClientService의 단위 테스트
 *
 * 정상 시나리오:
 * - Q&A 리스트 생성 성공
 * - 커스텀 답변 생성 성공
 *
 * 예외 처리 시나리오:
 * - 부적절한 커스텀 응답 검증 → AiInvalidRequestException (422)
 * - 품질 미달 응답 검증 → AiInvalidRequestException (422)
 * - LLM API 호출 실패 → InterviewException
 * - 응답 파싱 실패 → InterviewException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InterviewLlmClientService 단위 테스트")
@Slf4j
class InterviewLlmClientServiceTest {

    @Mock
    private InterviewLlmFeignClient interviewLlmFeignClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private OpenAiResponseParser openAiResponseParser;

    @InjectMocks
    private InterviewLlmClientService interviewLlmClientService;

    private List<InputItem> testInputItems;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 설정 시작 ===");

        objectMapper = new ObjectMapper();
        interviewLlmClientService =
                new InterviewLlmClientService(interviewLlmFeignClient, objectMapper, openAiResponseParser);

        testInputItems = List.of(
                new InputItem("system", List.of(ContentItem.text("면접 질문 생성 AI입니다."))),
                new InputItem("user", List.of(ContentItem.text("자소서를 바탕으로 질문을 생성해주세요.")))
        );

        log.info("테스트 InputItems 생성 완료: {}개", testInputItems.size());
        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("generateCustomAnswer 메서드")
    class GenerateCustomAnswerTest {

        @Test
        @DisplayName("거절 키워드 포함 응답으로 AiInvalidRequestException 발생 (422)")
        void shouldThrowAiInvalidRequestExceptionForRejectionKeywords() throws Exception {
            // given
            String rawResponse = createMockOpenAiRawResponse();
            given(interviewLlmFeignClient.analyzeRaw(any())).willReturn(rawResponse);

            String rejectionJson = "{\"answer\":\"\",\"tip\":\"면접 준비 서비스만 제공 가능합니다. 면접과 관련된 요청을 해주세요.\"}";
            given(openAiResponseParser.extractTextContent(rawResponse)).willReturn(rejectionJson);


            // when & then
            assertThatThrownBy(() -> interviewLlmClientService.generateCustomAnswer(testInputItems))
                    .isInstanceOf(AiInvalidRequestException.class)
                    .hasMessage("면접과 관련되지 않은 요청입니다. 면접 준비와 관련된 요청만 가능합니다.");
        }

        @Test
        @DisplayName("품질 미달 응답(너무 짧음)으로 AiInvalidRequestException 발생 (422)")
        void shouldThrowAiInvalidRequestExceptionForLowQualityResponse() throws Exception {
            // given
            String rawResponse = createMockOpenAiRawResponse();
            given(interviewLlmFeignClient.analyzeRaw(any())).willReturn(rawResponse);

            String lowQualityJson = "{\"answer\":\"좋습니다.\",\"tip\":\"더 구체적으로 답변하세요.\"}";
            given(openAiResponseParser.extractTextContent(rawResponse)).willReturn(lowQualityJson);

            // when & then
            assertThatThrownBy(() -> interviewLlmClientService.generateCustomAnswer(testInputItems))
                    .isInstanceOf(AiInvalidRequestException.class)
                    .hasMessage("면접과 관련되지 않은 요청입니다. 면접 준비와 관련된 요청만 가능합니다.");
        }
    }

    private String createMockOpenAiRawResponse() {
        return """
                {
                    "choices": [
                        {
                            "message": {
                                "content": "실제 응답 내용"
                            }
                        }
                    ]
                }
                """;
    }

    private String createMockQnaJsonContent() {
        return """
                {
                    "qnaList": [
                        {
                            "question": "이 회사에 지원한 동기는 무엇인가요?",
                            "answer": "저는 이 회사의 혁신적인 기술과 비전에 깊이 공감했습니다...",
                            "tip": "구체적인 회사 정보를 바탕으로 답변하세요."
                        },
                        {
                            "question": "당신의 가장 큰 강점은 무엇인가요?",
                            "answer": "저의 가장 큰 강점은 문제 해결 능력입니다...",
                            "tip": "실제 경험 사례와 함께 설명하세요."
                        },
                        {
                            "question": "팀 프로젝트에서 어려움을 겪은 경험이 있나요?",
                            "answer": "이전 프로젝트에서 의견 충돌이 있었지만...",
                            "tip": "갈등 해결 과정과 결과를 구체적으로 설명하세요."
                        }
                    ]
                }
                """;
    }

    private String createValidCustomAnswerJson() {
        return """
                {
                    "answer": "저의 가장 큰 강점은 팀워크와 소통능력입니다. 이전 프로젝트에서 서로 다른 배경을 가진 팀원들과 협업하며 목표를 달성한 경험이 있습니다. 특히 의견이 충돌할 때도 적극적으로 소통하여 해결책을 찾았습니다.",
                    "tip": "구체적인 사례와 함께 성과를 수치로 설명하면 더욱 효과적입니다."
                }
                """;
    }
}