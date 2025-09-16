package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.client.CoverLetterLlmFeignClient;
import com.cvmento.domain.coverLetter.dto.request.ContentItem;
import com.cvmento.domain.coverLetter.dto.request.InputItem;
import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import com.cvmento.domain.coverLetter.dto.response.LlmAnalysisResponse;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.AiInvalidRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Slf4j
@DisplayName("CoverLetterLlmClientService 단위 테스트 (raw 응답 래핑 버전)")
class CoverLetterLlmClientServiceTest {

    @Mock
    private CoverLetterLlmFeignClient coverLetterLlmFeignClient;

    @Mock
    private OpenAiResponseParser openAiResponseParser;

    private ObjectMapper objectMapper;

    private CoverLetterLlmClientService service;

    private List<InputItem> inputItems;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new CoverLetterLlmClientService(coverLetterLlmFeignClient, objectMapper, openAiResponseParser);

        inputItems = List.of(
                new InputItem("system", List.of(ContentItem.text("자소서 첨삭 도우미입니다."))),
                new InputItem("user", List.of(ContentItem.text("이 자소서를 개선해줘.")))
        );
    }

    @Nested
    @DisplayName("analyze - 정상/예외 흐름")
    class Analyze {

        @Test
        @DisplayName("정상 JSON → 파싱 성공 & 검증 통과")
        void shouldPass_whenValidJson() throws Exception {
            // given (improvedContent는 50자 이상이어야 통과)
            String contentJson = """
                {
                  "feedback": {
                    "strengths": [{"description":"명확한 동기","suggestion":"구체 예시 유지"}],
                    "improvements": [{"description":"성과 수치 부족","suggestion":"수치화하여 보완"}],
                    "summary": "좋은 구조이나 사례의 구체성이 부족합니다."
                  },
                  "improvedContent": "저는 직무 관련 프로젝트에서 팀 생산성을 25% 향상시킨 경험이 있습니다. 문제 정의부터 실행과 회고까지..."
                }
            """;
            String raw = wrapAsOpenAiRaw(contentJson);
            given(coverLetterLlmFeignClient.analyzeRaw(any(LlmRequest.class))).willReturn(raw);
            given(openAiResponseParser.extractTextContent(raw)).willReturn(contentJson);

            // when
            LlmAnalysisResponse resp = service.analyze(inputItems);

            // then
            assertThat(resp.feedback()).isNotBlank();
            assertThat(resp.improvedContent()).hasSizeGreaterThanOrEqualTo(50);
        }

        @Test
        @DisplayName("거절 키워드 포함(summary) → AiInvalidRequestException (422)")
        void shouldThrowAiInvalidRequest_whenRejectionKeywordsInSummary() throws Exception {
            // given
            String contentJson = """
                {
                  "feedback": {
                    "strengths": [],
                    "improvements": [],
                    "summary": "자소서와 관련되지 않은 요청입니다. 자소서 첨삭 서비스만 제공 가능합니다."
                  },
                  "improvedContent": "길이 검증 통과를 위해 충분히 긴 텍스트를 포함합니다. 이 문장은 50자를 넘겨야 하므로 내용을 조금 더 추가합니다."
                }
            """;
            String raw = wrapAsOpenAiRaw(contentJson);
            given(coverLetterLlmFeignClient.analyzeRaw(any(LlmRequest.class))).willReturn(raw);
            given(openAiResponseParser.extractTextContent(raw)).willReturn(contentJson);

            // when / then
            assertThatThrownBy(() -> service.analyze(inputItems))
                    .isInstanceOf(AiInvalidRequestException.class)
                    .hasMessageContaining("자소서 내용과 관련된 요청만 가능");
        }

        @Test
        @DisplayName("품질 미달(개선본문 너무 짧음) → AiInvalidRequestException (422)")
        void shouldThrowAiInvalidRequest_whenImprovedContentTooShort() throws Exception {
            // given
            String contentJson = """
                {
                  "feedback": { "strengths": [], "improvements": [], "summary": "간단 요약" },
                  "improvedContent": "짧음"
                }
            """;
            String raw = wrapAsOpenAiRaw(contentJson);
            given(coverLetterLlmFeignClient.analyzeRaw(any(LlmRequest.class))).willReturn(raw);
            given(openAiResponseParser.extractTextContent(raw)).willReturn(contentJson);

            // when / then
            assertThatThrownBy(() -> service.analyze(inputItems))
                    .isInstanceOf(AiInvalidRequestException.class);
        }
    }

    /** OpenAI Responses 스타일 raw를 흉내내어 content에 주어진 문자열을 삽입 */
    private String wrapAsOpenAiRaw(String content) {
        return """
            {
              "choices": [
                {
                  "message": {
                    "content": %s
                  }
                }
              ]
            }
            """.formatted(escapeAsJsonString(content));
    }

    /** content가 JSON이면 그대로, 아니면 JSON 문자열로 이스케이프 */
    private String escapeAsJsonString(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) return trimmed;
        String escaped = trimmed
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }
}
