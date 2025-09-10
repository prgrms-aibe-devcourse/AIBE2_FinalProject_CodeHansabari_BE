// ResumeDetailResponse.java
package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.domain.resume.enums.CareerType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record ResumeDetailResponse(
        Long resumeId,
        String title,
        ResumeType type,
        String name,
        String email,
        Integer birthYear,
        String phone,
        CareerType careerType,
        String fieldName,
        String introduction,
        String githubUrl,
        String blogUrl,
        String notionUrl,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        List<EducationResponse> educations,
        List<ResumeTechStackResponse> techStacks,
        List<CustomLinkResponse> customLinks,
        List<CareerResponse> careers,
        List<ProjectResponse> projects,
        List<TrainingResponse> trainings,
        List<AdditionalInfoResponse> additionalInfos
) {
}