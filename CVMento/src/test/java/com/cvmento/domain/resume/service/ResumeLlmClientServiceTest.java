package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.client.ResumeLlmFeignClient;
import com.cvmento.domain.resume.dto.request.ResumeLlmRequest;
import com.cvmento.domain.resume.dto.request.ResumeVisionRequest;
import com.cvmento.domain.resume.dto.response.ResumeImportResponse;
import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.ResumeException;
import com.cvmento.global.exception.customException.ResumeValidationException;
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

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * ResumeLlmClientService의 단위 테스트.
 *
 * 정상 시나리오:
 * - LLM API 호출 및 응답 파싱
 * - Vision API 호출 및 응답 파싱
 * - JSON 응답 정상 처리
 * - 기본값 fallback 처리
 *
 * 비정상 시나리오:
 * - 빈 프롬프트 검증
 * - API 호출 실패
 * - JSON 파싱 실패
 * - Vision API 오류
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeLlmClientService 단위 테스트")
@Slf4j
class ResumeLlmClientServiceTest {

    private static final List<com.cvmento.domain.resume.dto.request.EducationSaveRequest> EMPTY_EDUCATIONS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.ResumeTechStackSaveRequest> EMPTY_TECH_STACKS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.CustomLinkSaveRequest> EMPTY_CUSTOM_LINKS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.CareerSaveRequest> EMPTY_CAREERS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.ProjectSaveRequest> EMPTY_PROJECTS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.TrainingSaveRequest> EMPTY_TRAININGS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.AdditionalInfoSaveRequest> EMPTY_ADDITIONAL_INFOS = List.of();
    private static final String VALID_PROMPT = "이력서를 분석해주세요.";
    private static final String VALID_BASE64_IMAGE = "data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    private static final String VALID_JSON_RESPONSE = "{\"title\":\"백엔드 개발자\",\"type\":\"DEFAULT\",\"name\":\"김개발\",\"email\":\"kim@example.com\",\"birthYear\":1995,\"phone\":\"010-1234-5678\",\"careerType\":\"EXPERIENCED\",\"fieldName\":\"백엔드 개발자\",\"introduction\":\"3년차 개발자입니다.\",\"githubUrl\":\"https://github.com/kimdev\",\"blogUrl\":null,\"notionUrl\":null,\"educations\":[],\"techStacks\":[],\"customLinks\":[],\"careers\":[],\"projects\":[],\"trainings\":[],\"additionalInfos\":[]}";

    @Mock
    private ResumeLlmFeignClient resumeLlmFeignClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OpenAiResponseParser openAiResponseParser;

    @InjectMocks
    private ResumeLlmClientService resumeLlmClientService;

    private ResumeImportResponse mockResponse;
    private String mockOpenAiResponse;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 설정 시작 ===");

        mockResponse = new ResumeImportResponse(
                "백엔드 개발자",
                ResumeType.DEFAULT,
                "김개발",
                "kim@example.com",
                1995,
                "010-1234-5678",
                CareerType.EXPERIENCED,
                "백엔드 개발자",
                "3년차 개발자입니다.",
                "https://github.com/kimdev",
                null,
                null,
                EMPTY_EDUCATIONS,
                EMPTY_TECH_STACKS,
                EMPTY_CUSTOM_LINKS,
                EMPTY_CAREERS,
                EMPTY_PROJECTS,
                EMPTY_TRAININGS,
                EMPTY_ADDITIONAL_INFOS
        );

        mockOpenAiResponse = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "```json\\n%s\\n```"
                      }
                    }
                  ]
                }
                """.formatted(VALID_JSON_RESPONSE);

        log.info("테스트 Mock 데이터 생성 완료");
        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("입력 검증 테스트")
    class InputValidationTests {

        @Test
        @DisplayName("빈 프롬프트로 LLM 호출 시 예외 발생")
        void convertResume_WithEmptyPrompt_ThrowsException() {
            log.info("=== 테스트 시작: 빈 프롬프트로 LLM 호출 시 예외 발생 ===");

            // When & Then
            assertThatThrownBy(() -> resumeLlmClientService.convertResume(""))
                    .isInstanceOf(ResumeValidationException.class)
                    .hasMessage("프롬프트가 비어있습니다.");

            assertThatThrownBy(() -> resumeLlmClientService.convertResume(null))
                    .isInstanceOf(ResumeValidationException.class)
                    .hasMessage("프롬프트가 비어있습니다.");

            log.info("✅ 빈 프롬프트 검증 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("올바르지 않은 Base64 이미지로 Vision 호출 시 예외 발생")
        void convertResumeWithVision_WithInvalidBase64_ThrowsException() {
            log.info("=== 테스트 시작: 올바르지 않은 Base64 이미지로 Vision 호출 시 예외 발생 ===");

            // When & Then
            assertThatThrownBy(() -> resumeLlmClientService.convertResumeWithVision(VALID_PROMPT, "invalid_base64"))
                    .isInstanceOf(ResumeValidationException.class)
                    .hasMessage("올바르지 않은 Base64 이미지 형식입니다.");

            assertThatThrownBy(() -> resumeLlmClientService.convertResumeWithVision(VALID_PROMPT, ""))
                    .isInstanceOf(ResumeValidationException.class)
                    .hasMessage("Base64 이미지 데이터가 비어있습니다.");

            log.info("✅ Base64 이미지 검증 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("올바른 입력값 검증 통과")
        void validateInputs_WithValidValues_Success() {
            log.info("=== 테스트 시작: 올바른 입력값 검증 통과 ===");

            // Given
            given(resumeLlmFeignClient.analyzeRaw(any(ResumeLlmRequest.class)))
                    .willReturn(mockOpenAiResponse);
            try {
                given(openAiResponseParser.extractTextContent(anyString()))
                        .willReturn(VALID_JSON_RESPONSE);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }
            try {
                given(objectMapper.readValue(anyString(), eq(ResumeImportResponse.class)))
                        .willReturn(mockResponse);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When
            ResumeImportResponse result = resumeLlmClientService.convertResume(VALID_PROMPT);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김개발");

            log.info("✅ 올바른 입력값으로 정상 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("LLM API 호출 테스트")
    class LlmApiCallTests {

        @Test
        @DisplayName("LLM API 정상 호출 및 응답 파싱")
        void convertResume_WithValidPrompt_Success() {
            log.info("=== 테스트 시작: LLM API 정상 호출 및 응답 파싱 ===");

            // Given
            given(resumeLlmFeignClient.analyzeRaw(any(ResumeLlmRequest.class)))
                    .willReturn(mockOpenAiResponse);
            try {
                given(openAiResponseParser.extractTextContent(anyString()))
                        .willReturn(VALID_JSON_RESPONSE);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }
            try {
                given(objectMapper.readValue(anyString(), eq(ResumeImportResponse.class)))
                        .willReturn(mockResponse);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When
            ResumeImportResponse result = resumeLlmClientService.convertResume(VALID_PROMPT);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김개발");
            assertThat(result.title()).isEqualTo("백엔드 개발자");
            assertThat(result.careerType()).isEqualTo(CareerType.EXPERIENCED);

            verify(resumeLlmFeignClient).analyzeRaw(any(ResumeLlmRequest.class));
            try {
                verify(openAiResponseParser).extractTextContent(mockOpenAiResponse);
            } catch (Exception e) {
                // Verify이므로 실제로는 발생하지 않음
            }

            log.info("✅ LLM API 정상 호출 및 파싱 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("LLM API 호출 실패 시 예외 발생")
        void convertResume_WithApiFailure_ThrowsException() {
            log.info("=== 테스트 시작: LLM API 호출 실패 시 예외 발생 ===");

            // Given
            given(resumeLlmFeignClient.analyzeRaw(any(ResumeLlmRequest.class)))
                    .willThrow(new RuntimeException("API 호출 실패"));

            // When & Then
            assertThatThrownBy(() -> resumeLlmClientService.convertResume(VALID_PROMPT))
                    .isInstanceOf(ResumeException.class)
                    .hasMessage("LLM API 호출에 실패했습니다.");

            log.info("✅ LLM API 호출 실패 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("OpenAI 응답 파싱 실패 시 예외 발생")
        void convertResume_WithParsingFailure_ThrowsException() {
            log.info("=== 테스트 시작: OpenAI 응답 파싱 실패 시 예외 발생 ===");

            // Given
            given(resumeLlmFeignClient.analyzeRaw(any(ResumeLlmRequest.class)))
                    .willReturn(mockOpenAiResponse);
            try {
                given(openAiResponseParser.extractTextContent(anyString()))
                        .willThrow(new RuntimeException("파싱 실패"));
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When & Then
            assertThatThrownBy(() -> resumeLlmClientService.convertResume(VALID_PROMPT))
                    .isInstanceOf(ResumeException.class)
                    .hasMessage("LLM 응답 파싱에 실패했습니다.");

            log.info("✅ OpenAI 응답 파싱 실패 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("Vision API 호출 테스트")
    class VisionApiCallTests {

        @Test
        @DisplayName("Vision API 정상 호출 및 응답 파싱")
        void convertResumeWithVision_WithValidInputs_Success() {
            log.info("=== 테스트 시작: Vision API 정상 호출 및 응답 파싱 ===");

            // Given
            try {
                ObjectMapper realMapper = new ObjectMapper();
                com.fasterxml.jackson.databind.node.ObjectNode visionNode = realMapper.createObjectNode();
                com.fasterxml.jackson.databind.node.ArrayNode choicesArray = realMapper.createArrayNode();
                com.fasterxml.jackson.databind.node.ObjectNode choiceNode = realMapper.createObjectNode();
                com.fasterxml.jackson.databind.node.ObjectNode messageNode = realMapper.createObjectNode();

                messageNode.put("content", VALID_JSON_RESPONSE);
                choiceNode.set("message", messageNode);
                choicesArray.add(choiceNode);
                visionNode.set("choices", choicesArray);

                String visionResponse = realMapper.writeValueAsString(visionNode);

                given(resumeLlmFeignClient.analyzeVision(any(ResumeVisionRequest.class)))
                        .willReturn(visionResponse);

                // Vision API 응답 JSON을 실제 ObjectMapper로 파싱하도록 Mock
                com.fasterxml.jackson.databind.JsonNode actualVisionNode = realMapper.readTree(visionResponse);
                given(objectMapper.readTree(anyString()))
                        .willReturn(actualVisionNode);

                // 추출된 content를 ResumeImportResponse로 파싱
                given(objectMapper.readValue(anyString(), eq(ResumeImportResponse.class)))
                        .willReturn(mockResponse);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When
            ResumeImportResponse result = resumeLlmClientService.convertResumeWithVision(
                    VALID_PROMPT, VALID_BASE64_IMAGE
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김개발");
            assertThat(result.title()).isEqualTo("백엔드 개발자");

            verify(resumeLlmFeignClient).analyzeVision(any(ResumeVisionRequest.class));

            log.info("✅ Vision API 정상 호출 및 파싱 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("Vision API 호출 실패 시 예외 발생")
        void convertResumeWithVision_WithApiFailure_ThrowsException() {
            log.info("=== 테스트 시작: Vision API 호출 실패 시 예외 발생 ===");

            // Given
            given(resumeLlmFeignClient.analyzeVision(any(ResumeVisionRequest.class)))
                    .willThrow(new RuntimeException("Vision API 호출 실패"));

            // When & Then
            assertThatThrownBy(() -> resumeLlmClientService.convertResumeWithVision(
                    VALID_PROMPT, VALID_BASE64_IMAGE))
                    .isInstanceOf(ResumeException.class)
                    .hasMessage("Vision API 호출에 실패했습니다.");

            log.info("✅ Vision API 호출 실패 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("Vision API 응답에서 content 추출 실패 시 fallback")
        void convertResumeWithVision_WithContentExtractionFailure_Fallback() {
            log.info("=== 테스트 시작: Vision API 응답에서 content 추출 실패 시 fallback ===");

            // Given
            String malformedResponse = "{ \"invalid\": \"response\" }";
            given(resumeLlmFeignClient.analyzeVision(any(ResumeVisionRequest.class)))
                    .willReturn(malformedResponse);
            try {
                given(objectMapper.readTree(anyString()))
                        .willReturn(createEmptyJsonNode());
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When
            ResumeImportResponse result = resumeLlmClientService.convertResumeWithVision(
                    VALID_PROMPT, VALID_BASE64_IMAGE
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("홍길동"); // 기본값
            assertThat(result.title()).isEqualTo("변환된 이력서 (자동생성)");

            log.info("✅ Vision content 추출 실패 시 fallback 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("JSON 응답 파싱 테스트")
    class JsonParsingTests {

        @Test
        @DisplayName("마크다운 코드 블록이 포함된 응답 정상 파싱")
        void parseActualContent_WithMarkdownCodeBlock_Success() {
            log.info("=== 테스트 시작: 마크다운 코드 블록이 포함된 응답 정상 파싱 ===");

            // Given
            String markdownResponse = "```json\n" + VALID_JSON_RESPONSE + "\n```";
            given(resumeLlmFeignClient.analyzeRaw(any(ResumeLlmRequest.class)))
                    .willReturn(mockOpenAiResponse);
            try {
                given(openAiResponseParser.extractTextContent(anyString()))
                        .willReturn(markdownResponse);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }
            try {
                given(objectMapper.readValue(anyString(), eq(ResumeImportResponse.class)))
                        .willReturn(mockResponse);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When
            ResumeImportResponse result = resumeLlmClientService.convertResume(VALID_PROMPT);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김개발");

            log.info("✅ 마크다운 코드 블록 포함 응답 파싱 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("JSON 파싱 실패 시 기본값 반환")
        void parseActualContent_WithInvalidJson_ReturnDefault() {
            log.info("=== 테스트 시작: JSON 파싱 실패 시 기본값 반환 ===");

            // Given
            String invalidJson = "{ 잘못된 JSON }";
            given(resumeLlmFeignClient.analyzeRaw(any(ResumeLlmRequest.class)))
                    .willReturn(mockOpenAiResponse);
            try {
                given(openAiResponseParser.extractTextContent(anyString()))
                        .willReturn(invalidJson);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }
            try {
                given(objectMapper.readValue(anyString(), eq(ResumeImportResponse.class)))
                        .willThrow(new RuntimeException("JSON 파싱 오류"));
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When
            ResumeImportResponse result = resumeLlmClientService.convertResume(VALID_PROMPT);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("홍길동"); // 기본값
            assertThat(result.title()).isEqualTo("변환된 이력서 (자동생성)");
            assertThat(result.careerType()).isEqualTo(CareerType.FRESHMAN);

            log.info("✅ JSON 파싱 실패 시 기본값 반환 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("JSON이 없는 응답 시 기본값 반환")
        void parseActualContent_WithoutJson_ReturnDefault() {
            log.info("=== 테스트 시작: JSON이 없는 응답 시 기본값 반환 ===");

            // Given
            String textOnlyResponse = "이력서 변환이 완료되었습니다.";
            given(resumeLlmFeignClient.analyzeRaw(any(ResumeLlmRequest.class)))
                    .willReturn(mockOpenAiResponse);
            try {
                given(openAiResponseParser.extractTextContent(anyString()))
                        .willReturn(textOnlyResponse);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When
            ResumeImportResponse result = resumeLlmClientService.convertResume(VALID_PROMPT);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("홍길동");
            assertThat(result.introduction()).contains("LLM 응답을 파싱할 수 없어");

            log.info("✅ JSON이 없는 응답 시 기본값 반환 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("기본값 생성 테스트")
    class DefaultResponseTests {

        @Test
        @DisplayName("기본 응답 생성 시 원본 텍스트 포함")
        void createDefaultResponse_IncludeOriginalText() {
            log.info("=== 테스트 시작: 기본 응답 생성 시 원본 텍스트 포함 ===");

            // Given
            String originalText = "파싱할 수 없는 LLM 응답";
            given(resumeLlmFeignClient.analyzeRaw(any(ResumeLlmRequest.class)))
                    .willReturn(mockOpenAiResponse);
            try {
                given(openAiResponseParser.extractTextContent(anyString()))
                        .willReturn(originalText);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When
            ResumeImportResponse result = resumeLlmClientService.convertResume(VALID_PROMPT);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.introduction()).contains(originalText);
            assertThat(result.careerType()).isEqualTo(CareerType.FRESHMAN);
            assertThat(result.type()).isEqualTo(ResumeType.DEFAULT);

            log.info("✅ 기본 응답에 원본 텍스트 포함 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("긴 원본 텍스트 시 200자로 제한")
        void createDefaultResponse_LimitLongText() {
            log.info("=== 테스트 시작: 긴 원본 텍스트 시 200자로 제한 ===");

            // Given
            String longText = "A".repeat(300); // 300자 텍스트
            given(resumeLlmFeignClient.analyzeRaw(any(ResumeLlmRequest.class)))
                    .willReturn(mockOpenAiResponse);
            try {
                given(openAiResponseParser.extractTextContent(anyString()))
                        .willReturn(longText);
            } catch (Exception e) {
                // Mock 설정이므로 실제로는 발생하지 않음
            }

            // When
            ResumeImportResponse result = resumeLlmClientService.convertResume(VALID_PROMPT);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.introduction()).contains("A".repeat(200) + "...");
            assertThat(result.introduction()).doesNotContain("A".repeat(250));

            log.info("✅ 긴 텍스트 200자 제한 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    // 테스트 헬퍼 메서드들
    private com.fasterxml.jackson.databind.JsonNode createMockJsonNode(String content) {
        try {
            return new ObjectMapper().readTree("""
                    {
                      "choices": [
                        {
                          "message": {
                            "content": "%s"
                          }
                        }
                      ]
                    }
                    """.formatted(content));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private com.fasterxml.jackson.databind.JsonNode createEmptyJsonNode() {
        try {
            return new ObjectMapper().readTree("{}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}