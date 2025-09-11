package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.request.ResumeSaveRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeDetailResponse;
import com.cvmento.domain.resume.dto.response.ResumeThumbnailResponse;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.enums.ResumeStatus;
import com.cvmento.domain.resume.repository.ResumeRepository;
import com.cvmento.domain.resume.repository.ResumeRepositoryImpl;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import com.cvmento.global.exception.customException.ResumeNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    public void saveResume(ResumeSaveRequest request, String memberEmail) {
        MDC.put("spanId", "resume-save-service");

        Member member = findMemberByEmail(memberEmail);

        // 1. 이력서 기본 정보 저장
        Resume resume = createAndSaveResume(request, member);

        // 2. 상세 정보들 저장 (QueryDSL 구현체 사용)
        MDC.put("spanId", "resume-details-repository");
        resumeRepositoryImpl.saveResumeDetails(request, resume);

        MDC.put("spanId", "resume-save-service");
        log.info("이력서 저장 완료 - ID: {}, 제목: {}, 타입: {}",
                resume.getId(), request.title(), request.type());
    }

    /**
     * 이력서 전체 정보 수정 (덮어쓰기 방식)
     */
    @Transactional
    public void updateResume(Long resumeId, ResumeUpdateRequest request, String memberEmail) {
        MDC.put("spanId", "resume-update-service");

        Member member = findMemberByEmail(memberEmail);

        // 1. 기존 이력서 조회 및 권한 확인
        Resume existingResume = findActiveResumeByIdAndMember(resumeId, member);

        // 2. 기존 이력서의 모든 하위 데이터 삭제
        MDC.put("spanId", "resume-details-repository");
        resumeRepositoryImpl.deleteAllResumeDetails(existingResume);

        MDC.put("spanId", "resume-update-service");
        // 3. 기본 정보 업데이트
        updateResumeBasicInfo(existingResume, request);

        // 4. 새로운 상세 정보들 저장
        MDC.put("spanId", "resume-details-repository");
        resumeRepositoryImpl.saveResumeDetails(request.toSaveRequest(), existingResume);

        MDC.put("spanId", "resume-update-service");
        log.info("이력서 수정 완료 - ID: {}, 제목: {}", resumeId, request.title());
    }

    /**
     * 이력서 소프트 삭제
     */
    @Transactional
    public void deleteResume(Long resumeId, String memberEmail) {
        MDC.put("spanId", "resume-delete-service");

        Member member = findMemberByEmail(memberEmail);

        // 1. 이력서 조회 및 권한 확인
        Resume resume = findActiveResumeByIdAndMember(resumeId, member);

        // 2. 상태를 DELETED로 변경
        resume.updateStatus(ResumeStatus.DELETED);

        log.info("이력서 소프트 삭제 완료 - ID: {}, 제목: {}", resumeId, resume.getTitle());
    }

    /**
     * 이력서 목록 조회 (썸네일, 페이징)
     */
    public Page<ResumeThumbnailResponse> getResumeList(String memberEmail, Pageable pageable) {
        MDC.put("spanId", "resume-list-service");

        MDC.put("spanId", "resume-repository");
        Page<Resume> resumePage = resumeRepository.findByMemberEmailAndStatusOrderByUpdatedAtDesc(
                memberEmail, ResumeStatus.ACTIVE, pageable);

        MDC.put("spanId", "resume-list-service");
        log.info("이력서 목록 조회 완료 - 총 개수: {}, 현재페이지: {}",
                resumePage.getTotalElements(), resumePage.getNumber());

        return resumePage.map(this::convertToThumbnailResponse);
    }

    /**
     * 이력서 상세 조회 (QueryDSL Projection 활용)
     */
    public ResumeDetailResponse getResumeDetail(Long resumeId, String memberEmail) {
        MDC.put("spanId", "resume-detail-service");

        Member member = findMemberByEmail(memberEmail);

        // QueryDSL로 DTO 직접 조회
        MDC.put("spanId", "resume-details-repository");
        ResumeDetailResponse result = resumeRepositoryImpl.findResumeDetailByIdAndMember(
                resumeId, member, ResumeStatus.ACTIVE);

        MDC.put("spanId", "resume-detail-service");
        if (result == null) {
            throw new ResumeNotFoundException("이력서를 찾을 수 없거나 접근 권한이 없습니다.");
        }

        log.info("이력서 상세 조회 완료 - ID: {}, 제목: {}", resumeId, result.title());

        return result;
    }

    /**
     * 이력서 복구 (소프트 삭제된 이력서만) - 관리자 권한
     */
    @Transactional
    public void restoreResume(Long resumeId, String adminEmail) {
        MDC.put("spanId", "resume-restore-service");

        MDC.put("spanId", "resume-repository");
        Resume resume = resumeRepository.findByIdAndStatus(resumeId, ResumeStatus.DELETED)
                .orElseThrow(() -> new ResumeNotFoundException("복구할 수 있는 이력서를 찾을 수 없습니다."));

        MDC.put("spanId", "resume-restore-service");
        resume.restore();

        log.info("관리자 권한으로 이력서 복구 완료 - ID: {}, 관리자: {}, 원 소유자: {}",
                resumeId, adminEmail, resume.getMember().getEmail());
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

        updateOptionalInfo(resume, request.introduction(), request.githubUrl(),
                request.blogUrl(), request.notionUrl());

        MDC.put("spanId", "resume-repository");
        Resume saved = resumeRepository.save(resume);

        MDC.put("spanId", "resume-save-service");
        return saved;
    }

    private void updateResumeBasicInfo(Resume resume, ResumeUpdateRequest request) {
        resume.updateTitle(request.title());
        resume.updateType(request.type());
        resume.updateBasicInfo(request.name(), request.email(), request.birthYear(), request.phone());
        resume.updateFieldAndCareerType(request.fieldName(), request.careerType());

        updateOptionalInfo(resume, request.introduction(), request.githubUrl(),
                request.blogUrl(), request.notionUrl());
    }

    private void updateOptionalInfo(Resume resume, String introduction, String githubUrl,
                                    String blogUrl, String notionUrl) {
        if (introduction != null && !introduction.trim().isEmpty()) {
            resume.updateIntroduction(introduction);
        } else {
            resume.updateIntroduction(null);
        }

        resume.updateUrls(githubUrl, blogUrl, notionUrl);
    }

    /**
     * 활성 상태 이력서만 조회 - 커스텀 예외 사용
     */
    private Resume findActiveResumeByIdAndMember(Long resumeId, Member member) {
        MDC.put("spanId", "resume-repository");
        Resume resume = resumeRepository.findByIdAndMemberAndStatus(resumeId, member, ResumeStatus.ACTIVE)
                .orElseThrow(() -> new ResumeNotFoundException("이력서를 찾을 수 없거나 접근 권한이 없습니다."));

        MDC.put("spanId", "resume-update-service");
        return resume;
    }

    private Member findMemberByEmail(String email) {
        MDC.put("spanId", "member-repository");
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("멤버를 찾을 수 없습니다."));

        MDC.put("spanId", "resume-save-service");
        return member;
    }

    private ResumeThumbnailResponse convertToThumbnailResponse(Resume resume) {
        List<String> completedSections = getCompletedSections(resume);

        return ResumeThumbnailResponse.of(
                resume.getId(),
                resume.getTitle(),
                resume.getUpdatedAt(),
                completedSections
        );
    }
    private List<String> getCompletedSections(Resume resume) {
        List<String> sections = new ArrayList<>();

        if (hasEducations(resume)) {
            sections.add("educations");
        }

        if (hasTechStacks(resume)) {
            sections.add("techStacks");
        }

        if (hasCustomLinks(resume)) {
            sections.add("customLinks");
        }

        if (hasCareers(resume)) {
            sections.add("careers");
        }

        if (hasProjects(resume)) {
            sections.add("projects");
        }

        if (hasTrainings(resume)) {
            sections.add("trainings");
        }

        if (hasAdditionalInfos(resume)) {
            sections.add("additionalInfos");
        }

        return sections;
    }

    private boolean hasEducations(Resume resume) {
        return resume.getEducations() != null && !resume.getEducations().isEmpty();
    }

    private boolean hasTechStacks(Resume resume) {
        return resume.getResumeTechStacks() != null && !resume.getResumeTechStacks().isEmpty();
    }

    private boolean hasCustomLinks(Resume resume) {
        return resume.getCustomLinks() != null && !resume.getCustomLinks().isEmpty();
    }

    private boolean hasCareers(Resume resume) {
        return resume.getCareers() != null && !resume.getCareers().isEmpty();
    }

    private boolean hasProjects(Resume resume) {
        return resume.getProjects() != null && !resume.getProjects().isEmpty();
    }

    private boolean hasTrainings(Resume resume) {
        return resume.getTrainings() != null && !resume.getTrainings().isEmpty();
    }

    private boolean hasAdditionalInfos(Resume resume) {
        return resume.getAdditionalInfos() != null && !resume.getAdditionalInfos().isEmpty();
    }
}