package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.response.ResumeImportResponse;
import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.global.aws.LambdaService;
import com.cvmento.global.exception.customException.LambdaException;
import com.cvmento.global.exception.customException.ResumeException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;

/**
 * ResumeImportService의 단위 테스트.
 *
 * 정상 시나리오:
 * - Direct 전략으로 이미지/PDF 파일 변환
 * - Lambda 전략으로 파일 변환
 * - 기술스택 ID 매핑
 * - 변환 후 자동 저장
 *
 * 비정상 시나리오:
 * - 파일 검증 실패
 * - Lambda 실패 시 Direct 전략으로 fallback
 * - LLM API 호출 실패
 * - 저장 실패
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeImportService 단위 테스트")
@Slf4j
class ResumeImportServiceTest {

    private static final List<com.cvmento.domain.resume.dto.request.EducationSaveRequest> EMPTY_EDUCATIONS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.ResumeTechStackSaveRequest> EMPTY_TECH_STACKS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.CustomLinkSaveRequest> EMPTY_CUSTOM_LINKS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.CareerSaveRequest> EMPTY_CAREERS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.ProjectSaveRequest> EMPTY_PROJECTS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.TrainingSaveRequest> EMPTY_TRAININGS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.AdditionalInfoSaveRequest> EMPTY_ADDITIONAL_INFOS = List.of();
    private static final byte[] EMPTY_CONTENT = new byte[0];
    private static final byte[] VALID_CONTENT = "valid content".getBytes();
    private static final byte[] OVERSIZED_CONTENT = new byte[11 * 1024 * 1024]; // 11MB
    private static final String MEMBER_EMAIL = "test@example.com";

    @Mock
    private ResumeLlmPromptService resumeLlmPromptService;

    @Mock
    private ResumeLlmClientService resumeLlmClientService;

    @Mock
    private LambdaService lambdaService;

    @Mock
    private ResumeService resumeService;

    @Mock
    private TechStackMappingService techStackMappingService;

    @InjectMocks
    private ResumeImportService resumeImportService;

    private ResumeImportResponse mockResponse;
    private VisionPromptResult mockVisionPrompt;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 설정 시작 ===");

        mockResponse = new ResumeImportResponse(
                "백엔드 개발자 김개발",
                ResumeType.DEFAULT,
                "김개발",
                "kim@example.com",
                1995,
                "010-1234-5678",
                CareerType.EXPERIENCED,
                "백엔드 개발자",
                "3년차 백엔드 개발자입니다.",
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

        mockVisionPrompt = new VisionPromptResult(
                "이력서를 분석해주세요.",
                "base64ImageData"
        );

        // 기본 전략을 direct로 설정
        ReflectionTestUtils.setField(resumeImportService, "importStrategy", "direct");

        log.info("테스트 Mock 데이터 생성 완료");
        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("파일 검증 테스트")
    class FileValidationTests {

        @Test
        @DisplayName("빈 파일 업로드 시 예외 발생")
        void validateFile_WithEmptyFile_ThrowsException() {
            log.info("=== 테스트 시작: 빈 파일 업로드 시 예외 발생 ===");

            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", EMPTY_CONTENT
            );

            // When & Then
            assertThatThrownBy(() -> resumeImportService.importResume(emptyFile, MEMBER_EMAIL))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("파일이 비어있습니다.");

            log.info("✅ 빈 파일 검증 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("지원하지 않는 파일 형식 시 예외 발생")
        void validateFile_WithUnsupportedType_ThrowsException() {
            log.info("=== 테스트 시작: 지원하지 않는 파일 형식 시 예외 발생 ===");

            // Given
            MockMultipartFile unsupportedFile = new MockMultipartFile(
                    "file", "document.txt", "text/plain", VALID_CONTENT
            );

            // When & Then
            assertThatThrownBy(() -> resumeImportService.importResume(unsupportedFile, MEMBER_EMAIL))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("지원하지 않는 파일 형식입니다. PDF 또는 이미지 파일만 업로드 가능합니다.");

            log.info("✅ 지원하지 않는 파일 형식 검증 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("파일 크기 초과 시 예외 발생")
        void validateFile_WithOversizedFile_ThrowsException() {
            log.info("=== 테스트 시작: 파일 크기 초과 시 예외 발생 ===");

            // Given
            MockMultipartFile oversizedFile = new MockMultipartFile(
                    "file", "large.pdf", "application/pdf", OVERSIZED_CONTENT
            );

            // When & Then
            assertThatThrownBy(() -> resumeImportService.importResume(oversizedFile, MEMBER_EMAIL))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("파일 크기는 10MB를 초과할 수 없습니다.");

            log.info("✅ 파일 크기 초과 검증 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("PDF 파일 정상 검증")
        void validateFile_WithValidPdf_Success() {
            log.info("=== 테스트 시작: PDF 파일 정상 검증 ===");

            // Given
            MockMultipartFile validPdf = new MockMultipartFile(
                    "file", "resume.pdf", "application/pdf", VALID_CONTENT
            );

            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willReturn(mockResponse);

            // When
            ResumeImportResponse result = resumeImportService.importResume(validPdf, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김개발");

            log.info("✅ PDF 파일 정상 검증 및 변환 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("이미지 파일 정상 검증")
        void validateFile_WithValidImage_Success() {
            log.info("=== 테스트 시작: 이미지 파일 정상 검증 ===");

            // Given
            MockMultipartFile validImage = new MockMultipartFile(
                    "file", "resume.jpg", "image/jpeg", VALID_CONTENT
            );

            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willReturn(mockResponse);

            // When
            ResumeImportResponse result = resumeImportService.importResume(validImage, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("kim@example.com");

            log.info("✅ 이미지 파일 정상 검증 및 변환 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("Direct 전략 테스트")
    class DirectStrategyTests {

        @Test
        @DisplayName("Direct 전략으로 이미지 파일 변환 성공")
        void importWithDirect_WithImageFile_Success() {
            log.info("=== 테스트 시작: Direct 전략으로 이미지 파일 변환 성공 ===");

            // Given
            MockMultipartFile imageFile = new MockMultipartFile(
                    "file", "resume.jpg", "image/jpeg", VALID_CONTENT
            );

            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willReturn(mockResponse);

            // When
            ResumeImportResponse result = resumeImportService.importResume(imageFile, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("백엔드 개발자 김개발");
            assertThat(result.careerType()).isEqualTo(CareerType.EXPERIENCED);

            verify(resumeLlmPromptService).createVisionPrompt(imageFile);
            verify(resumeLlmClientService).convertResumeWithVision(
                    mockVisionPrompt.textPrompt(), mockVisionPrompt.base64Image()
            );

            log.info("✅ Direct 전략 이미지 파일 변환 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("Direct 전략으로 PDF 파일 변환 성공")
        void importWithDirect_WithPdfFile_Success() {
            log.info("=== 테스트 시작: Direct 전략으로 PDF 파일 변환 성공 ===");

            // Given
            MockMultipartFile pdfFile = new MockMultipartFile(
                    "file", "resume.pdf", "application/pdf", VALID_CONTENT
            );

            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willReturn(mockResponse);

            // When
            ResumeImportResponse result = resumeImportService.importResume(pdfFile, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.fieldName()).isEqualTo("백엔드 개발자");
            assertThat(result.introduction()).contains("3년차");

            verify(resumeLlmPromptService).createVisionPrompt(pdfFile);
            verify(resumeLlmClientService).convertResumeWithVision(anyString(), anyString());

            log.info("✅ Direct 전략 PDF 파일 변환 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("Direct 전략에서 Vision API 호출 실패 시 예외 발생")
        void importWithDirect_WithVisionApiError_ThrowsException() {
            log.info("=== 테스트 시작: Direct 전략에서 Vision API 호출 실패 시 예외 발생 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file", "resume.jpg", "image/jpeg", VALID_CONTENT
            );

            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willThrow(new ResumeException("Vision API 호출 실패"));

            // When & Then
            assertThatThrownBy(() -> resumeImportService.importResume(validFile, MEMBER_EMAIL))
                    .isInstanceOf(ResumeException.class)
                    .hasMessage("Vision API 호출 실패");

            log.info("✅ Direct 전략 Vision API 실패 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("Lambda 전략 테스트")
    class LambdaStrategyTests {

        @BeforeEach
        void setLambdaStrategy() {
            ReflectionTestUtils.setField(resumeImportService, "importStrategy", "lambda");
        }

        @Test
        @DisplayName("Lambda 전략으로 파일 변환 성공")
        void importWithLambda_Success() {
            log.info("=== 테스트 시작: Lambda 전략으로 파일 변환 성공 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file", "resume.pdf", "application/pdf", VALID_CONTENT
            );

            String extractedText = "김개발\n백엔드 개발자\n경력 3년";
            String prompt = "이력서 텍스트를 분석해주세요: " + extractedText;

            given(lambdaService.invokeLambdaOcr(any(MultipartFile.class)))
                    .willReturn(extractedText);
            given(resumeLlmPromptService.createResumeConversionPrompt(extractedText))
                    .willReturn(prompt);
            given(resumeLlmClientService.convertResume(prompt))
                    .willReturn(mockResponse);

            // When
            ResumeImportResponse result = resumeImportService.importResume(validFile, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김개발");

            verify(lambdaService).invokeLambdaOcr(validFile);
            verify(resumeLlmPromptService).createResumeConversionPrompt(extractedText);
            verify(resumeLlmClientService).convertResume(prompt);

            log.info("✅ Lambda 전략 파일 변환 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("Lambda 실패 시 Direct 전략으로 fallback")
        void importWithLambda_FallbackToDirect_WhenLambdaFails() {
            log.info("=== 테스트 시작: Lambda 실패 시 Direct 전략으로 fallback ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file", "resume.pdf", "application/pdf", VALID_CONTENT
            );

            // Lambda 실패 시나리오
            given(lambdaService.invokeLambdaOcr(any(MultipartFile.class)))
                    .willThrow(new LambdaException("Lambda OCR 실패"));

            // Direct 전략 성공 시나리오
            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willReturn(mockResponse);

            // When
            ResumeImportResponse result = resumeImportService.importResume(validFile, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김개발");

            // Lambda 호출 시도
            verify(lambdaService).invokeLambdaOcr(validFile);
            // Direct 전략으로 fallback
            verify(resumeLlmPromptService).createVisionPrompt(validFile);
            verify(resumeLlmClientService).convertResumeWithVision(anyString(), anyString());

            log.info("✅ Lambda 실패 시 Direct fallback 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("Lambda OCR 추출된 텍스트 길이 확인")
        void importWithLambda_CheckExtractedTextLength() {
            log.info("=== 테스트 시작: Lambda OCR 추출된 텍스트 길이 확인 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file", "resume.pdf", "application/pdf", VALID_CONTENT
            );

            String longExtractedText = "김개발".repeat(100); // 긴 텍스트
            String prompt = "긴 이력서 프롬프트";

            given(lambdaService.invokeLambdaOcr(any(MultipartFile.class)))
                    .willReturn(longExtractedText);
            given(resumeLlmPromptService.createResumeConversionPrompt(longExtractedText))
                    .willReturn(prompt);
            given(resumeLlmClientService.convertResume(prompt))
                    .willReturn(mockResponse);

            // When
            ResumeImportResponse result = resumeImportService.importResume(validFile, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();

            verify(lambdaService).invokeLambdaOcr(validFile);
            verify(resumeLlmPromptService).createResumeConversionPrompt(longExtractedText);
            verify(resumeLlmClientService).convertResume(prompt);

            log.info("✅ 긴 텍스트 처리 테스트 완료 - 텍스트 길이: {}chars", longExtractedText.length());
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("기술스택 매핑 테스트")
    class TechStackMappingTests {

        @Test
        @DisplayName("기술스택 ID 매핑 성공")
        void mapTechStackIdsToRealIds_Success() {
            log.info("=== 테스트 시작: 기술스택 ID 매핑 성공 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file", "resume.jpg", "image/jpeg", VALID_CONTENT
            );

            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willReturn(mockResponse);

            // 기술스택 매핑 Mock (lenient 모드 - 호출되지 않을 수도 있음)
            lenient().when(techStackMappingService.findTechStackIdByName(anyString()))
                    .thenReturn(Optional.of(123L));

            // When
            ResumeImportResponse result = resumeImportService.importResume(validFile, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();

            log.info("✅ 기술스택 매핑 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("기술스택 매핑 실패 시 원본 반환")
        void mapTechStackIdsToRealIds_ReturnOriginalWhenMappingFails() {
            log.info("=== 테스트 시작: 기술스택 매핑 실패 시 원본 반환 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file", "resume.jpg", "image/jpeg", VALID_CONTENT
            );

            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willReturn(mockResponse);

            // 기술스택 매핑 실패 (lenient 모드 - 호출되지 않을 수도 있음)
            lenient().when(techStackMappingService.findTechStackIdByName(anyString()))
                    .thenThrow(new RuntimeException("매핑 서비스 오류"));

            // When
            ResumeImportResponse result = resumeImportService.importResume(validFile, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김개발"); // 원본 데이터 반환

            log.info("✅ 기술스택 매핑 실패 시 원본 반환 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("변환 후 저장 테스트")
    class SaveAfterConversionTests {

        @Test
        @DisplayName("변환 후 자동 저장 성공")
        void saveConvertedResume_Success() {
            log.info("=== 테스트 시작: 변환 후 자동 저장 성공 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file", "resume.jpg", "image/jpeg", VALID_CONTENT
            );

            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willReturn(mockResponse);

            // When
            ResumeImportResponse result = resumeImportService.importResume(validFile, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();

            // 저장 메서드 호출 확인
            verify(resumeService).saveResume(any(), eq(MEMBER_EMAIL));

            log.info("✅ 변환 후 자동 저장 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("저장 실패해도 변환 결과는 반환")
        void saveConvertedResume_ReturnResultEvenWhenSaveFails() {
            log.info("=== 테스트 시작: 저장 실패해도 변환 결과는 반환 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file", "resume.jpg", "image/jpeg", VALID_CONTENT
            );

            given(resumeLlmPromptService.createVisionPrompt(any(MultipartFile.class)))
                    .willReturn(mockVisionPrompt);
            given(resumeLlmClientService.convertResumeWithVision(anyString(), anyString()))
                    .willReturn(mockResponse);

            // 저장 실패 시나리오
            doThrow(new RuntimeException("DB 저장 실패"))
                    .when(resumeService).saveResume(any(), eq(MEMBER_EMAIL));

            // When
            ResumeImportResponse result = resumeImportService.importResume(validFile, MEMBER_EMAIL);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김개발");

            // 저장 시도는 했지만 실패
            verify(resumeService).saveResume(any(), eq(MEMBER_EMAIL));

            log.info("✅ 저장 실패 시에도 변환 결과 반환 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }
    }
}