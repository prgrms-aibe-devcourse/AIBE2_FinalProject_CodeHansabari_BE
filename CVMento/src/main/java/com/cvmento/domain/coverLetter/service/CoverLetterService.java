package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterDetailResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterListResponse;
import com.cvmento.domain.coverLetter.entity.CoverLetter;
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
    public void saveCoverLetter(CoverLetterSaveRequest request, String userEmail) {
        Member member = findMemberByEmail(userEmail);

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
                logType, savedCoverLetter.getCoverLetterId(), userEmail);
    }

    /**
     * 자소서 목록 조회 (페이징 + view 옵션)
     */
    public Page<CoverLetterListResponse> getCoverLetters(String userEmail, Pageable pageable, String view) {
        Member member = findMemberByEmail(userEmail);
        Page<CoverLetter> coverLetters = coverLetterRepository.findByMemberOrderByUpdatedAtDesc(member, pageable);

        boolean isThumbnailView = "thumbnail".equals(view);

        List<CoverLetterListResponse> responses = coverLetters.getContent()
                .stream()
                .map(coverLetter -> isThumbnailView
                        ? CoverLetterListResponse.thumbnail(coverLetter)
                        : CoverLetterListResponse.full(coverLetter))
                .toList();

        log.info("자소서 목록 조회 - 사용자: {}, 페이지: {}, 크기: {}, 뷰: {}, 총 개수: {}",
                userEmail, pageable.getPageNumber(), pageable.getPageSize(),
                isThumbnailView ? "thumbnail" : "full", coverLetters.getTotalElements());

        return new PageImpl<>(responses, pageable, coverLetters.getTotalElements());
    }

    /**
     * 자소서 단일 조회
     */
    public CoverLetterDetailResponse getCoverLetter(Long coverLetterId, String userEmail) {
        CoverLetter coverLetter = findCoverLetterByIdAndUser(coverLetterId, userEmail);

        log.info("자소서 상세 조회 - ID: {}, 사용자: {}", coverLetterId, userEmail);

        return CoverLetterDetailResponse.from(coverLetter);
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

    private CoverLetter findCoverLetterByIdAndUser(Long coverLetterId, String userEmail) {
        Member member = findMemberByEmail(userEmail);
        return coverLetterRepository.findByCoverLetterIdAndMember(coverLetterId, member)
                .orElseThrow(() -> new CoverLetterException("자소서를 찾을 수 없습니다."));
    }
}