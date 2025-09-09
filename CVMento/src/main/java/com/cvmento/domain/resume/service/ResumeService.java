package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.request.ResumeSaveRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
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

    /**
     * 이력서 전체 정보 수정 (덮어쓰기 방식)
     */
    @Transactional
    public void updateResume(Long resumeId, ResumeUpdateRequest request, String userEmail) {
        Member member = findMemberByEmail(userEmail);

        // 1. 기존 이력서 조회 및 권한 확인
        Resume existingResume = findResumeByIdAndMember(resumeId, member);

        // 2. 기존 이력서의 모든 하위 데이터 삭제
        resumeRepositoryImpl.deleteAllResumeDetails(existingResume);

        // 3. 기본 정보 업데이트
        updateResumeBasicInfo(existingResume, request);

        // 4. 새로운 상세 정보들 저장
        resumeRepositoryImpl.saveResumeDetails(request.toSaveRequest(), existingResume);

        log.info("이력서 전체 정보 수정 완료 - ID: {}, 제목: {}, 사용자: {}",
                resumeId, request.title(), userEmail);
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
        updateOptionalInfo(resume, request.introduction(), request.githubUrl(),
                request.blogUrl(), request.notionUrl());

        return resumeRepository.save(resume);
    }

    private void updateResumeBasicInfo(Resume resume, ResumeUpdateRequest request) {
        // 기본 정보 업데이트
        resume.updateTitle(request.title());
        resume.updateType(request.type());
        resume.updateBasicInfo(request.name(), request.email(), request.birthYear(), request.phone());
        resume.updateFieldAndCareerType(request.fieldName(), request.careerType());

        // 선택적 정보 업데이트
        updateOptionalInfo(resume, request.introduction(), request.githubUrl(),
                request.blogUrl(), request.notionUrl());
    }

    private void updateOptionalInfo(Resume resume, String introduction, String githubUrl,
                                    String blogUrl, String notionUrl) {
        if (introduction != null && !introduction.trim().isEmpty()) {
            resume.updateIntroduction(introduction);
        } else {
            resume.updateIntroduction(null); // 기존 소개 삭제
        }

        resume.updateUrls(githubUrl, blogUrl, notionUrl);
    }

    private Resume findResumeByIdAndMember(Long resumeId, Member member) {
        return resumeRepository.findByIdAndMember(resumeId, member)
                .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없거나 접근 권한이 없습니다."));
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다."));
    }
}