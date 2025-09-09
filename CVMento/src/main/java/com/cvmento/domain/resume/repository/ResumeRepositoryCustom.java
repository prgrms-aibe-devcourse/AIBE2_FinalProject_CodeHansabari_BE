package com.cvmento.domain.resume.repository;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.resume.dto.request.ResumeSaveRequest;
import com.cvmento.domain.resume.dto.response.ResumeDetailResponse;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.enums.ResumeStatus;

public interface ResumeRepositoryCustom {
    void saveResumeDetails(ResumeSaveRequest request, Resume resume);
    void deleteAllResumeDetails(Resume resume);
    ResumeDetailResponse findResumeDetailByIdAndMember(Long resumeId, Member member, ResumeStatus status);
}