package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.service.ResumeImportService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * ResumeImportController의 단위 테스트
 *
 * 정상 시나리오:
 * - POST /resumes/import: 파일로부터 이력서 생성 (PDF, 이미지)
 *
 * 비정상 요청 시나리오:
 * - 지원하지 않는 파일 형식
 * - 파일 크기 초과
 * - 빈 파일
 * - AI 서비스 오류
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeImportController 단위 테스트")
@Slf4j
class ResumeImportControllerTest {

    @Mock
    private ResumeImportService resumeImportService;

    @InjectMocks
    private ResumeImportController resumeImportController;

    private String userEmail = "test@example.com";
    private UserDetails mockUserDetails;
    private MultipartFile pdfFile;
    private MultipartFile imageFile;
    private ResumeResponse resumeResponse;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 설정 시작 ===");

        mockUserDetails = User.withUsername(userEmail)
                .password("")
                .authorities("ROLE_USER")
                .build();

        // PDF 파일 Mock
        pdfFile = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        // 이미지 파일 Mock
        imageFile = new MockMultipartFile(
                "file",
                "resume.png",
                "image/png",
                "PNG content".getBytes()
        );

        // 이력서 응답 Mock (record 생성자 사용)
        ResumeResponse.MemberInfoResponse memberInfo = new ResumeResponse.MemberInfoResponse(
                "홍길동",
                "hong@example.com"
        );
        
        ResumeResponse.IntroResponse intro = new ResumeResponse.IntroResponse(
                "파일에서 추출된 자기소개입니다.",
                java.util.List.of("Java", "Spring", "React")
        );
        
        resumeResponse = new ResumeResponse(
                1L,
                "파일에서 생성된 이력서",
                memberInfo,
                intro,
                java.util.List.of(), // 빈 섹션 리스트
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString()
        );

        log.info("테스트 데이터 설정 완료");
        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("importResumeFromFile 메서드")
    class ImportResumeFromFileTest {

        @Test
        @DisplayName("PDF 파일로부터 정상적인 이력서 생성")
        void shouldCreateResumeFromPdfSuccessfully() {
            log.info("=== 테스트 시작: PDF 파일로부터 정상적인 이력서 생성 ===");

            // given
            given(resumeImportService.createResumeFromFile(pdfFile, userEmail))
                    .willReturn(resumeResponse);
            log.info("Mock 설정: PDF 파일 이력서 생성 서비스 호출 -> resumeResponse 반환");

            // when
            log.info("=== 컨트롤러 메서드 실행 ===");
            ResponseEntity<CommonResponse<ResumeResponse>> result =
                    resumeImportController.importResumeFromFile(pdfFile, mockUserDetails);

            // then
            log.info("=== 결과 검증 ===");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("이력서 파일로부터 생성 성공");
            assertThat(result.getBody().getData().id()).isEqualTo(1L);
            assertThat(result.getBody().getData().title()).isEqualTo("파일에서 생성된 이력서");

            verify(resumeImportService).createResumeFromFile(pdfFile, userEmail);
            log.info("✅ PDF 파일 이력서 생성 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("이미지 파일로부터 정상적인 이력서 생성")
        void shouldCreateResumeFromImageSuccessfully() {
            log.info("=== 테스트 시작: 이미지 파일로부터 정상적인 이력서 생성 ===");

            // given
            given(resumeImportService.createResumeFromFile(imageFile, userEmail))
                    .willReturn(resumeResponse);

            // when
            ResponseEntity<CommonResponse<ResumeResponse>> result =
                    resumeImportController.importResumeFromFile(imageFile, mockUserDetails);

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("이력서 파일로부터 생성 성공");
            assertThat(result.getBody().getData().id()).isEqualTo(1L);

            verify(resumeImportService).createResumeFromFile(imageFile, userEmail);
            log.info("✅ 이미지 파일 이력서 생성 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 때 예외 발생")
        void shouldThrowExceptionWhenMemberNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 사용자일 때 예외 발생 ===");

            // given
            given(resumeImportService.createResumeFromFile(pdfFile, userEmail))
                    .willThrow(new MemberNotFoundException("사용자를 찾을 수 없습니다."));

            // when & then
            assertThatThrownBy(() ->
                    resumeImportController.importResumeFromFile(pdfFile, mockUserDetails))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");

            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("파일에서 텍스트 추출 실패시 예외 발생")
        void shouldThrowExceptionWhenTextExtractionFailed() {
            log.info("=== 테스트 시작: 파일에서 텍스트 추출 실패시 예외 발생 ===");

            // given
            given(resumeImportService.createResumeFromFile(pdfFile, userEmail))
                    .willThrow(new ResumeAiException("파일에서 텍스트를 추출하지 못했습니다."));

            // when & then
            assertThatThrownBy(() ->
                    resumeImportController.importResumeFromFile(pdfFile, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("파일에서 텍스트를 추출하지 못했습니다.");

            log.info("✅ 텍스트 추출 실패 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("AI 이력서 내용 분석 실패시 예외 발생")
        void shouldThrowExceptionWhenAiAnalysisFailed() {
            log.info("=== 테스트 시작: AI 이력서 내용 분석 실패시 예외 발생 ===");

            // given
            given(resumeImportService.createResumeFromFile(pdfFile, userEmail))
                    .willThrow(new ResumeAiException("AI가 이력서 내용을 분석하지 못했습니다."));

            // when & then
            assertThatThrownBy(() ->
                    resumeImportController.importResumeFromFile(pdfFile, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("AI가 이력서 내용을 분석하지 못했습니다.");

            log.info("✅ AI 분석 실패 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("AI 응답 처리 중 오류 발생시 예외 발생")
        void shouldThrowExceptionWhenAiResponseProcessingError() {
            log.info("=== 테스트 시작: AI 응답 처리 중 오류 발생시 예외 발생 ===");

            // given
            given(resumeImportService.createResumeFromFile(imageFile, userEmail))
                    .willThrow(new ResumeAiException("AI 응답을 처리하는 중 오류가 발생했습니다. 응답 형식이 올바르지 않을 수 있습니다."));

            // when & then
            assertThatThrownBy(() ->
                    resumeImportController.importResumeFromFile(imageFile, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("AI 응답을 처리하는 중 오류가 발생했습니다. 응답 형식이 올바르지 않을 수 있습니다.");

            log.info("✅ AI 응답 처리 오류 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("파일 유효성 검사 테스트")
    class FileValidationTest {

        @Test
        @DisplayName("빈 파일 업로드시 예외 발생")
        void shouldThrowExceptionWhenEmptyFile() {
            log.info("=== 테스트 시작: 빈 파일 업로드시 예외 발생 ===");

            // given
            MultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.pdf", "application/pdf", new byte[0]);

            given(resumeImportService.createResumeFromFile(emptyFile, userEmail))
                    .willThrow(new IllegalArgumentException("Uploaded file is empty."));

            // when & then
            assertThatThrownBy(() ->
                    resumeImportController.importResumeFromFile(emptyFile, mockUserDetails))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Uploaded file is empty.");

            log.info("✅ 빈 파일에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("지원하지 않는 파일 형식 업로드시 예외 발생")
        void shouldThrowExceptionWhenUnsupportedFileType() {
            log.info("=== 테스트 시작: 지원하지 않는 파일 형식 업로드시 예외 발생 ===");

            // given
            MultipartFile unsupportedFile = new MockMultipartFile(
                    "file", "document.txt", "text/plain", "text content".getBytes());

            given(resumeImportService.createResumeFromFile(unsupportedFile, userEmail))
                    .willThrow(new IllegalArgumentException("Invalid file type. Only PDF, PNG, JPG files are allowed."));

            // when & then
            assertThatThrownBy(() ->
                    resumeImportController.importResumeFromFile(unsupportedFile, mockUserDetails))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid file type. Only PDF, PNG, JPG files are allowed.");

            log.info("✅ 지원하지 않는 파일 형식에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("파일 크기 초과시 예외 발생")
        void shouldThrowExceptionWhenFileSizeExceeded() {
            log.info("=== 테스트 시작: 파일 크기 초과시 예외 발생 ===");

            // given
            MultipartFile largeFile = new MockMultipartFile(
                    "file", "large.pdf", "application/pdf", new byte[6 * 1024 * 1024]); // 6MB

            given(resumeImportService.createResumeFromFile(largeFile, userEmail))
                    .willThrow(new IllegalArgumentException("File size exceeds the limit of 5MB."));

            // when & then
            assertThatThrownBy(() ->
                    resumeImportController.importResumeFromFile(largeFile, mockUserDetails))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("File size exceeds the limit of 5MB.");

            log.info("✅ 파일 크기 초과에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("AI 서비스 오류 처리 테스트")
    class AiServiceErrorTest {

        @Test
        @DisplayName("Vision LLM 서비스 연결 오류시 처리")
        void shouldHandleVisionLlmConnectionError() {
            log.info("=== 테스트 시작: Vision LLM 서비스 연결 오류시 처리 ===");

            // given
            given(resumeImportService.createResumeFromFile(imageFile, userEmail))
                    .willThrow(new ResumeAiException("AI 서비스에 연결할 수 없습니다. 네트워크 상태를 확인하거나 잠시 후 다시 시도해주세요."));

            // when & then
            assertThatThrownBy(() ->
                    resumeImportController.importResumeFromFile(imageFile, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("AI 서비스에 연결할 수 없습니다. 네트워크 상태를 확인하거나 잠시 후 다시 시도해주세요.");

            log.info("✅ Vision LLM 연결 오류에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("Lambda OCR 서비스 오류시 처리")
        void shouldHandleLambdaOcrError() {
            log.info("=== 테스트 시작: Lambda OCR 서비스 오류시 처리 ===");

            // given
            given(resumeImportService.createResumeFromFile(pdfFile, userEmail))
                    .willThrow(new ResumeAiException("이력서 생성 중 오류가 발생했습니다."));

            // when & then
            assertThatThrownBy(() ->
                    resumeImportController.importResumeFromFile(pdfFile, mockUserDetails))
                    .isInstanceOf(ResumeAiException.class)
                    .hasMessage("이력서 생성 중 오류가 발생했습니다.");

            log.info("✅ Lambda OCR 오류에 대한 예외 처리 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }
}