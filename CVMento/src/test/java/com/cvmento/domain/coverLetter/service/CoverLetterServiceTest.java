package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterUpdateRequest;
import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.exception.customException.CoverLetterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoverLetterService 단위 테스트")
class CoverLetterServiceTest {

    @Mock
    private CoverLetterRepository coverLetterRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CoverLetterService coverLetterService;

    private Member testMember;
    private CoverLetter testCoverLetter;
    private CoverLetterUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testMember = new Member("google123", "test@example.com", "테스트유저", "profile.jpg");

        testCoverLetter = new CoverLetter(
                "[원본] 네이버 백엔드 개발자 지원",
                "기존 자소서 내용입니다.",
                "백엔드 개발자",
                1,
                testMember
        );

        updateRequest = new CoverLetterUpdateRequest(
                "카카오 백엔드 개발자 지원",
                "수정된 자소서 내용입니다.",
                "풀스택 개발자",
                2
        );
    }

    @Test
    @DisplayName("자소서 수정 성공 - 정상 케이스")
    void updateCoverLetter_Success() {
        // given
        String memberEmail = "test@example.com";
        Long coverLetterId = 1L;

        // 변경: 이메일과 상태로 직접 조회하는 방식으로 Mock 설정
        given(coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                coverLetterId, memberEmail, CoverLetterStatus.ACTIVE))
                .willReturn(Optional.of(testCoverLetter));

        // when
        coverLetterService.updateCoverLetter(coverLetterId, updateRequest, memberEmail);

        // then
        assertThat(testCoverLetter.getTitle()).isEqualTo("[수정본] 카카오 백엔드 개발자 지원");
        assertThat(testCoverLetter.getContent()).isEqualTo("수정된 자소서 내용입니다.");
        assertThat(testCoverLetter.getJobField()).isEqualTo("풀스택 개발자");
        assertThat(testCoverLetter.getExperienceYears()).isEqualTo(2);
    }

    @Test
    @DisplayName("자소서 수정 실패 - 존재하지 않는 자소서")
    void updateCoverLetter_Fail_CoverLetterNotFound() {
        // given
        String memberEmail = "test@example.com";
        Long coverLetterId = 999L;

        // 변경: 이메일과 상태로 직접 조회하는 방식으로 Mock 설정
        given(coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                coverLetterId, memberEmail, CoverLetterStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                coverLetterService.updateCoverLetter(coverLetterId, updateRequest, memberEmail))
                .isInstanceOf(CoverLetterException.class)
                .hasMessage("자소서를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("자소서 수정 실패 - 다른 사용자의 자소서 수정 시도")
    void updateCoverLetter_Fail_UnauthorizedAccess() {
        // given
        String hackerEmail = "hacker@example.com";
        Long coverLetterId = 1L;

        // 변경: Member 엔티티 생성 불필요, 이메일로 직접 조회
        // 해커가 다른 사람의 자소서를 조회하려 하지만 권한이 없어서 조회 안됨
        given(coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                coverLetterId, hackerEmail, CoverLetterStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                coverLetterService.updateCoverLetter(coverLetterId, updateRequest, hackerEmail))
                .isInstanceOf(CoverLetterException.class)
                .hasMessage("자소서를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("[수정본] 접두사가 올바르게 추가되는지 확인")
    void updateCoverLetter_TitlePrefix() {
        // given
        String memberEmail = "test@example.com";
        Long coverLetterId = 1L;

        // 변경: 이메일과 상태로 직접 조회하는 방식으로 Mock 설정
        given(coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                coverLetterId, memberEmail, CoverLetterStatus.ACTIVE))
                .willReturn(Optional.of(testCoverLetter));

        CoverLetterUpdateRequest requestWithShortTitle = new CoverLetterUpdateRequest(
                "단순제목", "내용", "직무", 1
        );

        // when
        coverLetterService.updateCoverLetter(coverLetterId, requestWithShortTitle, memberEmail);

        // then
        assertThat(testCoverLetter.getTitle()).isEqualTo("[수정본] 단순제목");
    }
}