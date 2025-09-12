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
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 자소서 도메인 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final MemberRepository memberRepository;

    /**
     * 자소서 저장(원본/AI 첨삭)
     */
    @Transactional
    public void saveCoverLetter(CoverLetterSaveRequest request, String memberEmail) {
        MDC.put("spanId", "coverletter-save-service");

        Member member = findMemberByEmail(memberEmail);

        // 제목에 접두사 추가
        String finalTitle = buildTitleWithPrefix(request.title(), request.isAiImproved());

        CoverLetter coverLetter = new CoverLetter(
                finalTitle,
                request.content(),
                request.jobField(),
                request.experienceYears(),
                member
        );

        CoverLetter savedCoverLetter = coverLetterRepository.save(coverLetter);

        String logType = request.isAiImproved() ? "AI첨삭" : "원본";
        log.info("{} 자소서 저장 완료 - coverLetterId: {}, memberId: {}, 지원분야: {}",
                logType, savedCoverLetter.getCoverLetterId(), member.getMemberId(), request.jobField());
    }

    /**
     * 자소서 수정
     */
    @Transactional
    public void updateCoverLetter(Long coverLetterId, CoverLetterUpdateRequest request, String memberEmail) {
        MDC.put("spanId", "coverletter-update-service");
        CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

        String finalTitle = "[수정본] " + request.title();

        coverLetter.updateCoverLetter(
                finalTitle,
                request.content(),
                request.jobField(),
                request.experienceYears()
        );
        coverLetterRepository.save(coverLetter);

        log.info("자소서 수정 완료 - coverLetterId: {}, memberId: {}, 지원분야: {}",
                coverLetterId, coverLetter.getMember().getMemberId(), request.jobField());
    }

    /**
     * 자소서 삭제(소프트 삭제)
     */
    @Transactional
    public void deleteCoverLetter(Long coverLetterId, String memberEmail) {
        MDC.put("spanId", "coverletter-delete-service");
        CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

        coverLetter.delete();

        log.info("자소서 삭제 완료 - coverLetterId: {}, memberId: {}",
                coverLetterId, coverLetter.getMember().getMemberId());
    }

    /**
     * 자소서 목록 조회(활성만, 뷰 옵션 지원)
     */
    public Page<CoverLetterListResponse> getCoverLetters(String memberEmail, Pageable pageable, String view) {
        MDC.put("spanId", "coverletter-list-service");

        Member member = findMemberByEmail(memberEmail);

        MDC.put("spanId", "coverletter-repository");
        Page<CoverLetter> coverLetters = coverLetterRepository
                .findByMemberAndStatusOrderByUpdatedAtDesc(member, CoverLetterStatus.ACTIVE, pageable);

        MDC.put("spanId", "coverletter-list-service");
        boolean isThumbnailView = "thumbnail".equals(view);

        List<CoverLetterListResponse> responses = coverLetters.getContent()
                .stream()
                .map(coverLetter -> isThumbnailView
                        ? CoverLetterListResponse.thumbnail(coverLetter)
                        : CoverLetterListResponse.full(coverLetter))
                .toList();

        log.info("자소서 목록 조회 완료 - memberId: {}, 총 개수: {}, 뷰타입: {}",
                member.getMemberId(), coverLetters.getTotalElements(),
                isThumbnailView ? "thumbnail" : "full");

        return new PageImpl<>(responses, pageable, coverLetters.getTotalElements());
    }

    /**
     * 자소서 단건 조회(활성만)
     */
    public CoverLetterDetailResponse getCoverLetter(Long coverLetterId, String memberEmail) {
        MDC.put("spanId", "coverletter-detail-service");

        CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

        log.info("자소서 상세 조회 완료 - coverLetterId: {}, memberId: {}",
                coverLetterId, coverLetter.getMember().getMemberId());

        return CoverLetterDetailResponse.from(coverLetter);
    }

    /**
     * 자소서 복구(관리자용)
     */
    @Transactional
    public void restoreCoverLetter(Long coverLetterId, String adminEmail) {
        MDC.put("spanId", "coverletter-restore-service");
        CoverLetter coverLetter = findDeletedCoverLetterById(coverLetterId);

        coverLetter.restore();

        log.info("관리자 자소서 복구 완료 - coverLetterId: {}, 관리자: {}, 원소유자ID: {}",
                coverLetterId, adminEmail, coverLetter.getMember().getMemberId());
    }

    // ======================== 유틸리티 메서드 ========================

    private String buildTitleWithPrefix(String originalTitle, boolean isAiImproved) {
        String prefix = isAiImproved ? "[AI첨삭] " : "[원본] ";
        return prefix + originalTitle;
    }

    private Member findMemberByEmail(String email) {
        MDC.put("spanId", "member-repository");
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다."));

        MDC.put("spanId", "coverletter-list-service");
        return member;
    }

    /**
     * 활성 자소서 조회(소유자 검증)
     */
    private CoverLetter findActiveCoverLetterByIdAndMember(Long coverLetterId, String memberEmail) {
        MDC.put("spanId", "coverletter-repository");
        CoverLetter coverLetter = coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                        coverLetterId, memberEmail, CoverLetterStatus.ACTIVE)
                .orElseThrow(() -> new CoverLetterException("자소서를 찾을 수 없습니다."));

        MDC.put("spanId", "coverletter-update-service");
        return coverLetter;
    }

    /**
     * 삭제된 자소서 조회(관리자용)
     */
    private CoverLetter findDeletedCoverLetterById(Long coverLetterId) {
        MDC.put("spanId", "coverletter-repository");
        return coverLetterRepository.findByCoverLetterIdAndStatus(coverLetterId, CoverLetterStatus.DELETED)
                .orElseThrow(() -> new CoverLetterException("복구할 수 있는 자소서를 찾을 수 없습니다."));
    }
}
