package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterUpdateRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterDetailResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterListResponse;
import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.exception.customException.CoverLetterException;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final MemberRepository memberRepository;

    /**
     * 자소서 저장 (원본/AI첨삭 구분)
     */
    @Transactional
    public void saveCoverLetter(CoverLetterSaveRequest request, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);

        // 제목에 접두사 추가
        String finalTitle = buildTitleWithPrefix(request.title(), request.isAiImproved());

        CoverLetter coverLetter = new CoverLetter(
                finalTitle,
                request.content(),
                request.jobField(),      // 지원분야
                request.experienceYears(), // 경력 년수
                member
        );

        CoverLetter savedCoverLetter = coverLetterRepository.save(coverLetter);

        String logType = request.isAiImproved() ? "AI첨삭" : "원본";
        log.info("{} 자소서 저장 완료 - ID: {}, 사용자: {}",
                logType, savedCoverLetter.getCoverLetterId(), memberEmail);
    }

    /**
     * 자소서 수정
     */
    @Transactional
    public void updateCoverLetter(Long coverLetterId, CoverLetterUpdateRequest request, String memberEmail) {
        CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

        // [수정본] 접두사 추가
        String finalTitle = "[수정본] " + request.title();

        // 엔티티 업데이트 (updatedAt은 자동으로 갱신됨)
        coverLetter.updateCoverLetter(
                finalTitle,
                request.content(),
                request.jobField(),
                request.experienceYears()
        );
        coverLetterRepository.save(coverLetter);

        log.info("자소서 수정 완료 - ID: {}, 사용자: {}", coverLetterId, memberEmail);
    }

    /**
     * 자소서 삭제 (소프트 삭제)
     */
    @Transactional
    public void deleteCoverLetter(Long coverLetterId, String memberEmail) {
        CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

        // 소프트 삭제 (상태를 DELETED로 변경)
        coverLetter.delete();

        log.info("자소서 삭제 완료 - ID: {}, 사용자: {}", coverLetterId, memberEmail);
    }

    /**
     * 자소서 목록 조회 (페이징 + view 옵션) - 활성 상태만
     */
    public Page<CoverLetterListResponse> getCoverLetters(String memberEmail, Pageable pageable, String view) {
        Member member = findMemberByEmail(memberEmail);

        // 활성 상태의 자소서만 조회
        Page<CoverLetter> coverLetters = coverLetterRepository
                .findByMemberAndStatusOrderByUpdatedAtDesc(member, CoverLetterStatus.ACTIVE, pageable);

        boolean isThumbnailView = "thumbnail".equals(view);

        List<CoverLetterListResponse> responses = coverLetters.getContent()
                .stream()
                .map(coverLetter -> isThumbnailView
                        ? CoverLetterListResponse.thumbnail(coverLetter)
                        : CoverLetterListResponse.full(coverLetter))
                .toList();

        log.info("자소서 목록 조회 - 사용자: {}, 페이지: {}, 크기: {}, 뷰: {}, 총 개수: {}",
                memberEmail, pageable.getPageNumber(), pageable.getPageSize(),
                isThumbnailView ? "thumbnail" : "full", coverLetters.getTotalElements());

        return new PageImpl<>(responses, pageable, coverLetters.getTotalElements());
    }

    /**
     * 자소서 단일 조회 - 활성 상태만
     */
    public CoverLetterDetailResponse getCoverLetter(Long coverLetterId, String memberEmail) {
        CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

        log.info("자소서 상세 조회 - ID: {}, 사용자: {}", coverLetterId, memberEmail);

        return CoverLetterDetailResponse.from(coverLetter);
    }

    /**
     * 자소서 복구 (관리자 전용)
     * 삭제된 자소서 ID만으로 복구
     */
    @Transactional
    public void restoreCoverLetter(Long coverLetterId, String adminEmail) {
        CoverLetter coverLetter = findDeletedCoverLetterById(coverLetterId);

        // 복구 실행
        coverLetter.restore();

        log.info("관리자 권한으로 자소서 복구 완료 - ID: {}, 관리자: {}, 원 소유자: {}",
                coverLetterId, adminEmail, coverLetter.getMember().getEmail());
    }

    // ======================== 유틸리티 메서드 ========================

    private String buildTitleWithPrefix(String originalTitle, boolean isAiImproved) {
        String prefix = isAiImproved ? "[AI첨삭] " : "[원본] ";
        return prefix + originalTitle;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 활성 상태의 자소서만 조회하는 헬퍼 메서드 (이메일 기반)
     */
    private CoverLetter findActiveCoverLetterByIdAndMember(Long coverLetterId, String memberEmail) {
        return coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                        coverLetterId, memberEmail, CoverLetterStatus.ACTIVE)
                .orElseThrow(() -> new CoverLetterException("자소서를 찾을 수 없습니다."));
    }

    /**
     * 삭제된 상태의 자소서를 찾는 헬퍼 메서드 (관리자용 - ID만으로)
     */
    private CoverLetter findDeletedCoverLetterById(Long coverLetterId) {
        return coverLetterRepository.findByCoverLetterIdAndStatus(coverLetterId, CoverLetterStatus.DELETED)
                .orElseThrow(() -> new CoverLetterException("복구할 수 있는 자소서를 찾을 수 없습니다."));
    }
}