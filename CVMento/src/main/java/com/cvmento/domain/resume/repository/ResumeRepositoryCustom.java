package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.dto.request.ResumeSaveRequest;
import com.cvmento.domain.resume.entity.Resume;

public interface ResumeRepositoryCustom {
    void saveResumeDetails(ResumeSaveRequest request, Resume resume);
    void deleteAllResumeDetails(Resume resume);
}