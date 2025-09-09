package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.request.ResumeSaveRequest;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.repository.ResumeRepository;
import com.cvmento.domain.resume.repository.ResumeRepositoryImpl;
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
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeRepositoryImpl resumeRepositoryImpl;
    private final MemberRepository memberRepository;

    /**
     * 이력서 전체 정보 저장
     */
    @Transactional
    public void saveResume(ResumeSaveRequest request, String userEmail) {
        Member member = findMemberByEmail(userEmail);

        // 1. 이력서 기본 정보 저장
        Resume resume = createAndSaveResume(request, member);

        // 2. 상세 정보들 저장 (QueryDSL 구현체 사용)
        resumeRepositoryImpl.saveResumeDetails(request, resume);

        log.info("이력서 전체 정보 저장 완료 - ID: {}, 제목: {}, 타입: {}, 사용자: {}",
                resume.getId(), request.title(), request.type(), userEmail);
    }

    // ======================== Private 메서드 ========================

    private Resume createAndSaveResume(ResumeSaveRequest request, Member member) {
        Resume resume = Resume.createResume(
                request.title(),
                request.type(),
                request.name(),
                request.email(),
                request.birthYear(),
                request.phone(),
                request.careerType(),
                request.fieldName(),
                member
        );

        // 선택적 정보 업데이트
        if (request.introduction() != null && !request.introduction().trim().isEmpty()) {
            resume.updateIntroduction(request.introduction());
        }

        if (hasAnyUrl(request)) {
            resume.updateUrls(request.githubUrl(), request.blogUrl(), request.notionUrl());
        }

        return resumeRepository.save(resume);
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private boolean hasAnyUrl(ResumeSaveRequest request) {
        return (request.githubUrl() != null && !request.githubUrl().trim().isEmpty()) ||
                (request.blogUrl() != null && !request.blogUrl().trim().isEmpty()) ||
                (request.notionUrl() != null && !request.notionUrl().trim().isEmpty());
    }
}