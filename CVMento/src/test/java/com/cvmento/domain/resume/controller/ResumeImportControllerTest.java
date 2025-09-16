package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.response.ResumeImportResponse;
import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.domain.resume.service.ResumeImportService;
import com.cvmento.global.common.dto.CommonResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * ResumeImportController의 단위 테스트.
 *
 * 정상 시나리오:
 * - 이미지 파일 업로드 및 변환 성공
 * - PDF 파일 업로드 및 변환 성공
 * - Vision API를 통한 이력서 변환
 *
 * 비정상 시나리오:
 * - 빈 파일 업로드
 * - 지원하지 않는 파일 형식
 * - 파일 크기 초과
 * - LLM API 오류
 * - Lambda OCR 오류
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeImportController 단위 테스트")
@Slf4j
class ResumeImportControllerTest {

    private static final List<com.cvmento.domain.resume.dto.request.EducationSaveRequest> EMPTY_EDUCATIONS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.ResumeTechStackSaveRequest> EMPTY_TECH_STACKS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.CustomLinkSaveRequest> EMPTY_CUSTOM_LINKS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.CareerSaveRequest> EMPTY_CAREERS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.ProjectSaveRequest> EMPTY_PROJECTS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.TrainingSaveRequest> EMPTY_TRAININGS = List.of();
    private static final List<com.cvmento.domain.resume.dto.request.AdditionalInfoSaveRequest> EMPTY_ADDITIONAL_INFOS = List.of();
    private static final String[] USER_ROLES = {"USER"};

    @Mock
    private ResumeImportService resumeImportService;

    @InjectMocks
    private ResumeImportController resumeImportController;

    private String userEmail = "test@example.com";
    private UserDetails userDetails;
    private ResumeImportResponse mockResponse;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 설정 시작 ===");

        userDetails = User.withUsername(userEmail)
                .password("")
                .authorities("ROLE_USER")
                .build();

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

        log.info("테스트 Mock UserDetails 생성 완료: username={}, authorities={}",
                userEmail, userDetails.getAuthorities());
        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("이력서 파일 변환 성공 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("이미지 파일 업로드 및 변환 성공")
        void importResume_WithImageFile_Success() {
            log.info("=== 테스트 시작: 이미지 파일 업로드 및 변환 성공 ===");

            // Given
            MockMultipartFile imageFile = new MockMultipartFile(
                    "file",
                    "resume.jpg",
                    "image/jpeg",
                    "image content".getBytes()
            );

            given(resumeImportService.importResume(any(MultipartFile.class), eq(userEmail)))
                    .willReturn(mockResponse);
            log.info("Mock 설정: resumeImportService.importResume -> mockResponse 반환");

            // When
            log.info("=== 컨트롤러 메서드 실행 ===");
            ResponseEntity<CommonResponse<ResumeImportResponse>> result =
                    resumeImportController.importResume(imageFile, userDetails);

            // Then
            log.info("=== 결과 검증 ===");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("이력서가 성공적으로 변환되었습니다.");
            assertThat(result.getBody().getData()).isEqualTo(mockResponse);

            verify(resumeImportService).importResume(imageFile, userEmail);
            log.info("✅ 이미지 파일 변환 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("PDF 파일 업로드 및 변환 성공")
        void importResume_WithPdfFile_Success() {
            log.info("=== 테스트 시작: PDF 파일 업로드 및 변환 성공 ===");

            // Given
            MockMultipartFile pdfFile = new MockMultipartFile(
                    "file",
                    "resume.pdf",
                    "application/pdf",
                    "pdf content".getBytes()
            );

            given(resumeImportService.importResume(any(MultipartFile.class), eq(userEmail)))
                    .willReturn(mockResponse);

            // When
            ResponseEntity<CommonResponse<ResumeImportResponse>> result =
                    resumeImportController.importResume(pdfFile, userDetails);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getData().name()).isEqualTo("김개발");
            assertThat(result.getBody().getData().careerType()).isEqualTo(CareerType.EXPERIENCED);

            verify(resumeImportService).importResume(pdfFile, userEmail);
            log.info("✅ PDF 파일 변환 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("이력서 파일 변환 실패 시나리오")
    class FailureScenarios {

        @Test
        @DisplayName("빈 파일 업로드 시 BAD_REQUEST 반환")
        void importResume_WithEmptyFile_BadRequest() {
            log.info("=== 테스트 시작: 빈 파일 업로드 시 BAD_REQUEST 반환 ===");

            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            given(resumeImportService.importResume(any(MultipartFile.class), eq(userEmail)))
                    .willThrow(new IllegalArgumentException("파일이 비어있습니다."));

            // When
            ResponseEntity<CommonResponse<ResumeImportResponse>> result =
                    resumeImportController.importResume(emptyFile, userDetails);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody().isSuccess()).isFalse();
            assertThat(result.getBody().getMessage()).isEqualTo("파일이 비어있습니다.");
            log.info("✅ 빈 파일 업로드 예외 처리 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("지원하지 않는 파일 형식 업로드 시 BAD_REQUEST 반환")
        void importResume_WithUnsupportedFileType_BadRequest() {
            log.info("=== 테스트 시작: 지원하지 않는 파일 형식 업로드 시 BAD_REQUEST 반환 ===");

            // Given
            MockMultipartFile unsupportedFile = new MockMultipartFile(
                    "file",
                    "resume.txt",
                    "text/plain",
                    "text content".getBytes()
            );

            given(resumeImportService.importResume(any(MultipartFile.class), eq(userEmail)))
                    .willThrow(new IllegalArgumentException("지원하지 않는 파일 형식입니다."));

            // When
            ResponseEntity<CommonResponse<ResumeImportResponse>> result =
                    resumeImportController.importResume(unsupportedFile, userDetails);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody().isSuccess()).isFalse();
            assertThat(result.getBody().getMessage()).contains("지원하지 않는 파일 형식");
            log.info("✅ 지원하지 않는 파일 형식 예외 처리 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("파일 크기 초과 시 BAD_REQUEST 반환")
        void importResume_WithOversizedFile_BadRequest() {
            log.info("=== 테스트 시작: 파일 크기 초과 시 BAD_REQUEST 반환 ===");

            // Given
            MockMultipartFile oversizedFile = new MockMultipartFile(
                    "file",
                    "large-resume.pdf",
                    "application/pdf",
                    new byte[11 * 1024 * 1024] // 11MB
            );

            given(resumeImportService.importResume(any(MultipartFile.class), eq(userEmail)))
                    .willThrow(new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다."));

            // When
            ResponseEntity<CommonResponse<ResumeImportResponse>> result =
                    resumeImportController.importResume(oversizedFile, userDetails);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(result.getBody().isSuccess()).isFalse();
            assertThat(result.getBody().getMessage()).contains("10MB를 초과할 수 없습니다");
            log.info("✅ 파일 크기 초과 예외 처리 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("LLM API 오류 시 INTERNAL_SERVER_ERROR 반환")
        void importResume_WithLlmError_InternalServerError() {
            log.info("=== 테스트 시작: LLM API 오류 시 INTERNAL_SERVER_ERROR 반환 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file",
                    "resume.jpg",
                    "image/jpeg",
                    "image content".getBytes()
            );

            given(resumeImportService.importResume(any(MultipartFile.class), eq(userEmail)))
                    .willThrow(new ResumeException("이력서 변환에 실패했습니다."));

            // When
            ResponseEntity<CommonResponse<ResumeImportResponse>> result =
                    resumeImportController.importResume(validFile, userDetails);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(result.getBody().isSuccess()).isFalse();
            assertThat(result.getBody().getMessage()).contains("이력서 변환 중 오류가 발생했습니다");
            log.info("✅ LLM API 오류 예외 처리 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("Lambda OCR 오류 시 INTERNAL_SERVER_ERROR 반환")
        void importResume_WithLambdaError_InternalServerError() {
            log.info("=== 테스트 시작: Lambda OCR 오류 시 INTERNAL_SERVER_ERROR 반환 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file",
                    "resume.pdf",
                    "application/pdf",
                    "pdf content".getBytes()
            );

            given(resumeImportService.importResume(any(MultipartFile.class), eq(userEmail)))
                    .willThrow(new LambdaException("OCR 처리 중 오류가 발생했습니다."));

            // When
            ResponseEntity<CommonResponse<ResumeImportResponse>> result =
                    resumeImportController.importResume(validFile, userDetails);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(result.getBody().isSuccess()).isFalse();
            assertThat(result.getBody().getMessage()).contains("OCR 처리 중 오류가 발생했습니다");
            log.info("✅ Lambda OCR 오류 예외 처리 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("인증 및 권한 관련 테스트")
    class AuthenticationTests {

        @Test
        @DisplayName("올바른 사용자 인증 정보로 변환 요청 성공")
        void importResume_WithValidAuthentication_Success() {
            log.info("=== 테스트 시작: 올바른 사용자 인증 정보로 변환 요청 성공 ===");

            // Given
            MockMultipartFile validFile = new MockMultipartFile(
                    "file",
                    "resume.jpg",
                    "image/jpeg",
                    "image content".getBytes()
            );

            given(resumeImportService.importResume(any(MultipartFile.class), eq(userEmail)))
                    .willReturn(mockResponse);

            // When
            ResponseEntity<CommonResponse<ResumeImportResponse>> result =
                    resumeImportController.importResume(validFile, userDetails);

            // Then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(resumeImportService).importResume(validFile, userEmail);
            log.info("✅ 인증 정보 검증 테스트 완료");
            log.info("=== 테스트 완료 ===\n");
        }
    }
}