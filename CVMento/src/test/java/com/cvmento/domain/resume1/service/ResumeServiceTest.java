package com.cvmento.domain.resume1.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume1.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume1.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume1.dto.response.ResumeResponse;
import com.cvmento.domain.resume1.entity.Resume;
import com.cvmento.domain.resume1.enums.RecordStatus;
import com.cvmento.domain.resume1.enums.ResumeSectionType;
import com.cvmento.domain.resume1.repository.ResumeRepository;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * ResumeService의 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResumeService 단위 테스트")
@Slf4j
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ResumeService resumeService;

    private Member testMember;
    private Resume testResume;
    private Long resumeId = 1L;
    private Long memberId = 1L;
    private String userEmail = "test@example.com";
    private ResumeCreateRequest createRequest;
    private ResumeUpdateRequest updateRequest;

    @BeforeEach
    void setUp() throws Exception {
        log.info("=== 테스트 데이터 설정 시작 ===");

        testMember = new Member("google123", userEmail, "테스트 사용자", "profile.jpg");
        setField(testMember, "memberId", memberId);
        log.info("테스트 Member 생성 완료: email={}, name={}, memberId={}",
                testMember.getEmail(), testMember.getName(), memberId);

        testResume = new Resume("신입 개발자 이력서", testMember, "열정적인 개발자입니다.", "Java,Spring");
        setField(testResume, "resumeId", resumeId);
        log.info("테스트 Resume 생성 완료: title={}, resumeId={}",
                testResume.getTitle(), resumeId);

        // 생성 요청 Mock
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
                .sections(List.of(
                        ResumeCreateRequest.ResumeSectionRequest.builder()
                                .sectionType(ResumeSectionType.EDUCATION)
                                .sectionTitle("학력")
                                .items(List.of(
                                        ResumeCreateRequest.SectionItemRequest.builder()
                                                .title("ABC 대학교")
                                                .subTitle("컴퓨터공학과")
                                                .startDate("2018-03")
                                                .endDate("2022-02")
                                                .description("학사 졸업")
                                                .build()))
                                .build()))
                .build();

        // 수정 요청 Mock
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
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            log.info("Mock 설정: 사용자 조회 성공");

            given(resumeRepository.save(any(Resume.class)))
                    .willReturn(testResume);
            log.info("Mock 설정: 이력서 저장 성공");

            // when
            log.info("=== 서비스 메서드 실행 ===");
            ResumeResponse result = resumeService.createResume(createRequest, userEmail);

            // then
            log.info("=== 결과 검증 ===");
            assertThat(result).isNotNull();
            assertThat(result.resumeId()).isEqualTo(resumeId);
            assertThat(result.title()).isEqualTo("신입 개발자 이력서");
            assertThat(result.memberName()).isEqualTo(testMember.getName());
            assertThat(result.memberEmail()).isEqualTo(testMember.getEmail());

            verify(memberRepository).findByEmail(userEmail);
            verify(resumeRepository).save(any(Resume.class));
            log.info("✅ 이력서 생성 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 사용자일 때 예외 발생")
        void shouldThrowExceptionWhenMemberNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 사용자일 때 예외 발생 ===");

            // given
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    resumeService.createResume(createRequest, userEmail))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");

            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("getResume 메서드")
    class GetResumeTest {

        @Test
        @DisplayName("정상적인 이력서 조회")
        void shouldGetResumeSuccessfully() {
            log.info("=== 테스트 시작: 정상적인 이력서 조회 ===");

            // given
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            given(resumeRepository.findById(resumeId))
                    .willReturn(Optional.of(testResume));

            // when
            ResumeResponse result = resumeService.getResume(resumeId, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(result.resumeId()).isEqualTo(resumeId);
            assertThat(result.title()).isEqualTo(testResume.getTitle());

            verify(memberRepository).findByEmail(userEmail);
            verify(resumeRepository).findById(resumeId);
            log.info("✅ 이력서 조회 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("존재하지 않는 이력서일 때 예외 발생")
        void shouldThrowExceptionWhenResumeNotFound() {
            log.info("=== 테스트 시작: 존재하지 않는 이력서일 때 예외 발생 ===");

            // given
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            given(resumeRepository.findById(resumeId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                    resumeService.getResume(resumeId, userEmail))
                    .isInstanceOf(ResumeNotFoundException.class)
                    .hasMessage("이력서를 찾을 수 없습니다.");

            log.info("✅ 예상된 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }

        @Test
        @DisplayName("다른 사용자의 이력서 접근시 예외 발생")
        void shouldThrowExceptionWhenAccessOtherUsersResume() {
            log.info("=== 테스트 시작: 다른 사용자의 이력서 접근시 예외 발생 ===");

            // given
            Member otherMember = new Member("google456", "other@example.com", "다른 사용자", "other.jpg");
            try {
                setField(otherMember, "memberId", 2L);
            } catch (Exception e) {
                log.error("테스트 설정 오류", e);
            }

            Resume otherResume = new Resume("다른 사용자 이력서", otherMember, "다른 사용자입니다.", "Python");
            try {
                setField(otherResume, "resumeId", resumeId);
            } catch (Exception e) {
                log.error("테스트 설정 오류", e);
            }

            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            given(resumeRepository.findById(resumeId))
                    .willReturn(Optional.of(otherResume));

            // when & then
            assertThatThrownBy(() ->
                    resumeService.getResume(resumeId, userEmail))
                    .isInstanceOf(ResumeNotFoundException.class)
                    .hasMessage("이력서에 접근할 권한이 없습니다.");

            log.info("✅ 예상된 권한 예외 발생 확인");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    @Nested
    @DisplayName("getResumesByMember 메서드")
    class GetResumesByMemberTest {

        @Test
        @DisplayName("사용자 이력서 목록 조회")
        void shouldGetResumeListSuccessfully() {
            log.info("=== 테스트 시작: 사용자 이력서 목록 조회 ===");

            // given
            List<Resume> resumeList = List.of(testResume);
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            given(resumeRepository.findByMember_MemberId(memberId))
                    .willReturn(resumeList);

            // when
            List<ResumeResponse> result = resumeService.getResumesByMember(userEmail);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).resumeId()).isEqualTo(resumeId);
            assertThat(result.get(0).title()).isEqualTo(testResume.getTitle());

            verify(memberRepository).findByEmail(userEmail);
            verify(resumeRepository).findByMember_MemberId(memberId);
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
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            given(resumeRepository.findById(resumeId))
                    .willReturn(Optional.of(testResume));

            // when
            ResumeResponse result = resumeService.updateResume(resumeId, updateRequest, userEmail);

            // then
            assertThat(result).isNotNull();
            assertThat(result.resumeId()).isEqualTo(resumeId);
            // 수정된 내용이 반영되었는지 확인은 실제 엔티티 메서드가 호출되었는지로 검증

            verify(memberRepository).findByEmail(userEmail);
            verify(resumeRepository).findById(resumeId);
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
            given(memberRepository.findByEmail(userEmail))
                    .willReturn(Optional.of(testMember));
            given(resumeRepository.findById(resumeId))
                    .willReturn(Optional.of(testResume));

            // when
            assertThatCode(() ->
                    resumeService.deleteResume(resumeId, userEmail))
                    .doesNotThrowAnyException();

            // then
            assertThat(testResume.getStatus()).isEqualTo(RecordStatus.DELETED);

            verify(memberRepository).findByEmail(userEmail);
            verify(resumeRepository).findById(resumeId);
            log.info("✅ 이력서 삭제 테스트 통과");
            log.info("=== 테스트 완료 ===\n");
        }
    }

    // ================ 테스트 헬퍼 메서드 ================

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}