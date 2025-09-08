package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.service.ResumeService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import com.cvmento.global.exception.customException.ResumeNotFoundException;
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
 * ResumeController의 단위 테스트
 *
 * 정상 시나리오:
 * - GET /resumes/{id}: 이력서 단건 조회
 * - GET /resumes: 사용자 이력서 목록 조회
 * - POST /resumes: 이력서 생성
 * - PUT /resumes/{id}: 이력서 수정
 * - DELETE /resumes/{id}: 이력서 삭제
 *
 * 비정상 요청 시나리오:
 * - 존재하지 않는 이력서 접근
 * - 다른 사용자의 이력서 접근
 * - 존재하지 않는 사용자
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeController 단위 테스트")
@Slf4j
class ResumeControllerTest {

    @Mock
    private ResumeService resumeService;

    @InjectMocks
    private ResumeController resumeController;

    private Long resumeId = 1L;
    private String userEmail = "test@example.com";
    private UserDetails mockUserDetails;
    private ResumeCreateRequest createRequest;
    private ResumeUpdateRequest updateRequest;
    private ResumeResponse resumeResponse;

    @BeforeEach
    void setUp() {
        log.info("=== 테스트 데이터 설정 시작 ===");

        mockUserDetails = User.withUsername(userEmail)
                .password("")
                .authorities("ROLE_USER")
                .build();

        // 이력서 생성 요청 Mock
        createRequest = ResumeCreateRequest.builder()
                .title("신입 개발자 이력서")
                .memberInfo(ResumeCreateRequest.MemberInfoRequest.builder()
                        .name("홍길동")
                        .email("hong@example.com")
                        .phoneNumber("010-1234-5678")
                        .build())
                .intro(ResumeCreateRequest.IntroRequest.builder()
                        .selfIntroduction("열정적인 신입 개발자입니다.")
                        .techStack(List.of("Java", "Spring"))
                        .build())
                .sections(new ArrayList<>())
                .build();

        // 이력서 수정 요청 Mock
        updateRequest = ResumeUpdateRequest.builder()
                .title("수정된 개발자 이력서")
                .memberInfo(ResumeUpdateRequest.MemberInfoRequest.builder()
                        .name("홍길동")
                        .email("hong@example.com")
                        .phoneNumber("010-1234-5678")
                        .build())
                .intro(ResumeUpdateRequest.IntroRequest.builder()
                        .selfIntroduction("경험 많은 개발자입니다.")
                        .techStack(List.of("Java", "Spring", "React"))
                        .build())
                .sections(new ArrayList<>())
                .build();

        // 이력서 응답 Mock
        resumeResponse = ResumeResponse.builder()
                .resumeId(resumeId)
                .title("신입 개발자 이력서")
                .memberName("홍길동")
                .memberEmail("hong@example.com")
                .memberPhoneNumber("010-1234-5678")
                .selfIntroduction("열정적인 신입 개발자입니다.")
                .techStack("Java,Spring")
                .sections(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("테스트 데이터 설정 완료");
        log.info("=== 테스트 데이터 설정 완료 ===\n");
    }

    @Nested
    @DisplayName("createResume 메서드")
    class CreateResumeTest {

        @Test
        @DisplayName("정상적인 이력서 생성")
        void shouldCreateResumeSuccessfully() {
            log.info("=== 테스트 시작: 정상적인 이력서 생성 ===");

            // given
            given(resumeService.createResume(createRequest, userEmail))
                    .willReturn(resumeResponse);
            log.info("Mock 설정: 이력서 생성 서비스 호출 -> resumeResponse 반환");

            // when
            log.info("=== 컨트롤러 메서드 실행 ===");
            ResponseEntity<CommonResponse<ResumeResponse>> result =
                    resumeController.createResume(createRequest, mockUserDetails);

            // then
            log.info("=== 결과 검증 ===");
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("이력서 생성 성공");
            assertThat(result.getBody().getData().resumeId()).isEqualTo(resumeId);

            verify(resumeService).createResume(createRequest, userEmail);
            log.info("✅ 이력서 생성 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 때 예외 발생")
        void shouldThrowExceptionWhenMemberNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 사용자일 때 예외 발생 ===");

            // given
            given(resumeService.createResume(createRequest, userEmail))
                    .willThrow(new MemberNotFoundException("사용자를 찾을 수 없습니다."));

            // when & then
            assertThatThrownBy(() ->
                    resumeController.createResume(createRequest, mockUserDetails))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");

            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("getResumeById 메서드")
    class GetResumeByIdTest {

        @Test
        @DisplayName("정상적인 이력서 조회")
        void shouldGetResumeSuccessfully() {
            log.info("=== 테스트 시작: 정상적인 이력서 조회 ===");

            // given
            given(resumeService.getResume(resumeId, userEmail))
                    .willReturn(resumeResponse);

            // when
            ResponseEntity<CommonResponse<ResumeResponse>> result =
                    resumeController.getResumeById(resumeId, mockUserDetails);

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("이력서 조회 성공");
            assertThat(result.getBody().getData().resumeId()).isEqualTo(resumeId);

            verify(resumeService).getResume(resumeId, userEmail);
            log.info("✅ 이력서 조회 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 이력서일 때 예외 발생")
        void shouldThrowExceptionWhenResumeNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 이력서일 때 예외 발생 ===");

            // given
            given(resumeService.getResume(resumeId, userEmail))
                    .willThrow(new ResumeNotFoundException("이력서를 찾을 수 없습니다."));

            // when & then
            assertThatThrownBy(() ->
                    resumeController.getResumeById(resumeId, mockUserDetails))
                    .isInstanceOf(ResumeNotFoundException.class)
                    .hasMessage("이력서를 찾을 수 없습니다.");

            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("getResumesByUser 메서드")
    class GetResumesByUserTest {

        @Test
        @DisplayName("사용자 이력서 목록 조회")
        void shouldGetResumeListSuccessfully() {
            log.info("=== 테스트 시작: 사용자 이력서 목록 조회 ===");

            // given
            List<ResumeResponse> resumeList = List.of(resumeResponse);
            given(resumeService.getResumesByMember(userEmail))
                    .willReturn(resumeList);

            // when
            ResponseEntity<CommonResponse<List<ResumeResponse>>> result =
                    resumeController.getResumesByUser(mockUserDetails);

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("이력서 목록 조회 성공");
            assertThat(result.getBody().getData()).hasSize(1);

            verify(resumeService).getResumesByMember(userEmail);
            log.info("✅ 이력서 목록 조회 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("updateResume 메서드")
    class UpdateResumeTest {

        @Test
        @DisplayName("정상적인 이력서 수정")
        void shouldUpdateResumeSuccessfully() {
            log.info("=== 테스트 시작: 정상적인 이력서 수정 ===");

            // given
            ResumeResponse updatedResponse = ResumeResponse.builder()
                    .resumeId(resumeId)
                    .title("수정된 개발자 이력서")
                    .selfIntroduction("경험 많은 개발자입니다.")
                    .build();

            given(resumeService.updateResume(resumeId, updateRequest, userEmail))
                    .willReturn(updatedResponse);

            // when
            ResponseEntity<CommonResponse<ResumeResponse>> result =
                    resumeController.updateResume(resumeId, updateRequest, mockUserDetails);

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("이력서 수정 성공");
            assertThat(result.getBody().getData().title()).isEqualTo("수정된 개발자 이력서");

            verify(resumeService).updateResume(resumeId, updateRequest, userEmail);
            log.info("✅ 이력서 수정 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("deleteResume 메서드")
    class DeleteResumeTest {

        @Test
        @DisplayName("정상적인 이력서 삭제")
        void shouldDeleteResumeSuccessfully() {
            log.info("=== 테스트 시작: 정상적인 이력서 삭제 ===");

            // given
            willDoNothing().given(resumeService).deleteResume(resumeId, userEmail);

            // when
            ResponseEntity<CommonResponse<Void>> result =
                    resumeController.deleteResume(resumeId, mockUserDetails);

            // then
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().isSuccess()).isTrue();
            assertThat(result.getBody().getMessage()).isEqualTo("이력서 삭제 성공");
            assertThat(result.getBody().getData()).isNull();

            verify(resumeService).deleteResume(resumeId, userEmail);
            log.info("✅ 이력서 삭제 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }
    }
}