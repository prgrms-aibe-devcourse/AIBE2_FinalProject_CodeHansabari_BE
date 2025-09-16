package com.cvmento.domain.resume.repository;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.resume.dto.request.ResumeSaveRequest;
import com.cvmento.domain.resume.dto.response.ResumeDetailResponse;
import com.cvmento.domain.resume.dto.response.ResumeStatusListResponse;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.enums.ResumeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResumeRepositoryCustom {
    void saveResumeDetails(ResumeSaveRequest request, Resume resume);
    void deleteAllResumeDetails(Resume resume);
    ResumeDetailResponse findResumeDetailByIdAndMember(Long resumeId, Member member, ResumeStatus status);

    /**
     * 상태별 이력서 목록을 필터링과 페이징으로 조회
     *
     * @param status 이력서 상태 (ACTIVE, DELETED)
     * @param email 작성자 이메일 (부분 검색, null 가능)
     * @param title 이력서 제목 (부분 검색, null 가능)
     * @param pageable 페이징 정보
     * @return 이력서 목록 (페이징)
     */
    Page<ResumeStatusListResponse> findResumesWithFilters(
            ResumeStatus status,
            String email,
            String title,
            Pageable pageable
    );
}