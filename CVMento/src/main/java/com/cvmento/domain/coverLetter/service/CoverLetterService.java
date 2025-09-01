package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterSaveResponse;
import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final MemberRepository memberRepository;

    /**
     * 자소서 저장 (원본/AI첨삭 구분)
     */
    @Transactional
    public CoverLetterSaveResponse saveCoverLetter(CoverLetterSaveRequest request, String userEmail) {
        Member member = findMemberByEmail(userEmail);

        // 제목에 접두사 추가
        String finalTitle = buildTitleWithPrefix(request.title(), request.isAiImproved());

        CoverLetter coverLetter = new CoverLetter(
                finalTitle,
                request.content(),
                member
        );

        CoverLetter savedCoverLetter = coverLetterRepository.save(coverLetter);

        String logType = request.isAiImproved() ? "AI첨삭" : "원본";
        log.info("{} 자소서 저장 완료 - ID: {}, 사용자: {}",
                logType, savedCoverLetter.getCoverLetterId(), userEmail);

        return CoverLetterSaveResponse.from(savedCoverLetter);
    }

    // ======================== 유틸리티 메서드 ========================

    private String buildTitleWithPrefix(String originalTitle, boolean isAiImproved) {
        String prefix = isAiImproved ? "[AI첨삭] " : "[원본] ";
        return prefix + originalTitle;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }
}