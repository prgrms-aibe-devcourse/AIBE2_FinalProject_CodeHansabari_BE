package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterUpdateRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterDetailResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterListResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

    private static final Pattern TITLE_PREFIX =
            Pattern.compile("^\\[(원본|AI첨삭|수정본)]\\s*");

    /**
     * 자소서 저장(원본/AI 첨삭)
     */
    @Transactional
    public void saveCoverLetter(CoverLetterSaveRequest request, String memberEmail) {
        MDC.put("spanId", "coverletter-save-service");
        try {
            Member member = findMemberByEmail(memberEmail);

            // 접두사 단일화
            String prefix = request.isAiImproved() ? "[AI첨삭] " : "[원본] ";
            String finalTitle = withSinglePrefix(request.title(), prefix);

            CoverLetter coverLetter = new CoverLetter(
                    finalTitle,
                    request.content(),
                    request.jobField(),
                    request.experienceYears(),
                    member
            );

            CoverLetter saved = coverLetterRepository.save(coverLetter);

            String logType = request.isAiImproved() ? "AI첨삭" : "원본";
            log.info("{} 자소서 저장 완료 - coverLetterId: {}, memberId: {}, 지원분야: {}",
                    logType, saved.getCoverLetterId(), member.getMemberId(), request.jobField());
        } finally {
            MDC.remove("spanId");
        }
    }

    /**
     * 자소서 수정
     */
    @Transactional
    public void updateCoverLetter(Long coverLetterId, CoverLetterUpdateRequest request, String memberEmail) {
        MDC.put("spanId", "coverletter-update-service");
        try {
            CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

            // [수정본] 접두사 단일화
            String finalTitle = withSinglePrefix(request.title(), "[수정본] ");

            coverLetter.updateCoverLetter(
                    finalTitle,
                    request.content(),
                    request.jobField(),
                    request.experienceYears()
            );
            // 더티체킹으로 반영되므로 save() 불필요

            log.info("자소서 수정 완료 - coverLetterId: {}, memberId: {}, 지원분야: {}",
                    coverLetterId, coverLetter.getMember().getMemberId(), request.jobField());
        } finally {
            MDC.remove("spanId");
        }
    }

    /**
     * 자소서 삭제(소프트 삭제)
     */
    @Transactional
    public void deleteCoverLetter(Long coverLetterId, String memberEmail) {
        MDC.put("spanId", "coverletter-delete-service");
        try {
            CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);
            coverLetter.delete();
            log.info("자소서 삭제 완료 - coverLetterId: {}, memberId: {}",
                    coverLetterId, coverLetter.getMember().getMemberId());
        } finally {
            MDC.remove("spanId");
        }
    }

    /**
     * 자소서 목록 조회(활성만, 뷰 옵션 지원)
     */
    public Page<CoverLetterListResponse> getCoverLetters(String memberEmail, Pageable pageable, String view) {
        MDC.put("spanId", "coverletter-list-service");
        try {
            Member member = findMemberByEmail(memberEmail);

            // JPA 메서드로 Entity 조회
            Page<CoverLetter> coverLetters = coverLetterRepository
                    .findByMemberAndStatusOrderByUpdatedAtDesc(member, CoverLetterStatus.ACTIVE, pageable);

            // 뷰 타입에 따라 변환
            boolean isThumbnailView = "thumbnail".equals(view);
            List<CoverLetterListResponse> responses = coverLetters.getContent()
                    .stream()
                    .map(isThumbnailView
                            ? CoverLetterListResponse::thumbnail
                            : CoverLetterListResponse::full)
                    .toList();

            log.info("자소서 목록 조회 완료 - memberId: {}, 총 개수: {}, 현재 페이지 개수: {}, 뷰타입: {}",
                    member.getMemberId(), coverLetters.getTotalElements(), coverLetters.getNumberOfElements(), view);

            return new PageImpl<>(responses, pageable, coverLetters.getTotalElements());
        } finally {
            MDC.remove("spanId");
        }
    }

    /**
     * 자소서 단건 조회(활성만)
     */
    public CoverLetterDetailResponse getCoverLetter(Long coverLetterId, String memberEmail) {
        if (log.isInfoEnabled()) MDC.put("spanId", "coverletter-detail-service");
        try {
            CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);
            return CoverLetterDetailResponse.from(coverLetter);
        } finally {
            if (log.isInfoEnabled()) MDC.remove("spanId");
        }
    }

    /**
     * 자소서 복구(관리자용)
     */
    @Transactional
    public void restoreCoverLetter(Long coverLetterId, String adminEmail) {
        MDC.put("spanId", "coverletter-restore-service");
        try {
            CoverLetter coverLetter = findDeletedCoverLetterById(coverLetterId);
            coverLetter.restore();
            log.info("관리자 자소서 복구 완료 - coverLetterId: {}, 관리자: {}, 원소유자ID: {}",
                    coverLetterId, adminEmail, coverLetter.getMember().getMemberId());
        } finally {
            MDC.remove("spanId");
        }
    }

    /**
     * 상태별 자소서 목록 조회 (관리자용)
     */
    public Page<CoverLetterStatusListResponse> getCoverLettersByStatus(
            CoverLetterStatus status,
            String email,
            String title,
            Pageable pageable,
            String adminEmail
    ) {
        MDC.put("spanId", "coverletter-status-list-service");
        try {
            log.info("관리자 상태별 자소서 목록 조회 요청 - 관리자: {}, 상태: {}, 이메일필터: {}, 제목필터: {}, 페이지: {}",
                    adminEmail, status, email, title, pageable.getPageNumber());

            // 최적화된 커스텀 메서드 사용 (조건부 조인)
            Page<CoverLetterStatusListResponse> result =
                    coverLetterRepository.findCoverLettersWithFilters(status, email, title, pageable);

            log.info("상태별 자소서 목록 조회 완료 - 상태: {}, 총 개수: {}, 현재 페이지 개수: {}",
                    status, result.getTotalElements(), result.getNumberOfElements());

            return result;
        } finally {
            MDC.remove("spanId");
        }
    }

    private String withSinglePrefix(String originalTitle, String wantedPrefix) {
        // 접두사 단일화: 기존 접두사 제거 후 원하는 접두사만 1회 부여
        if (originalTitle == null) return wantedPrefix.trim();
        Matcher m = TITLE_PREFIX.matcher(originalTitle);
        String noPrefix = m.replaceFirst("").stripLeading();
        return wantedPrefix + noPrefix;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다."));
    }

    /** 활성 자소서 조회(소유자 검증) */
    private CoverLetter findActiveCoverLetterByIdAndMember(Long coverLetterId, String memberEmail) {
        return coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                        coverLetterId, memberEmail, CoverLetterStatus.ACTIVE)
                .orElseThrow(() -> new CoverLetterException("자소서를 찾을 수 없습니다."));
    }

    /** 삭제된 자소서 조회(관리자용) */
    private CoverLetter findDeletedCoverLetterById(Long coverLetterId) {
        return coverLetterRepository.findByCoverLetterIdAndStatus(
                        coverLetterId, CoverLetterStatus.DELETED)
                .orElseThrow(() -> new CoverLetterException("복구할 수 있는 자소서를 찾을 수 없습니다."));
    }
}