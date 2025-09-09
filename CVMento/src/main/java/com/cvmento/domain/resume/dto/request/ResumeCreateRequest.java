package com.cvmento.domain.resume.dto.request;

/**
 * 이력서 생성 요청 DTO
 * - ResumeRequest를 그대로 사용 (타입 안전성을 위한 별칭)
 */
public record ResumeCreateRequest(
    String title,
    ResumeRequest.MemberInfoRequest memberInfo,
    java.util.List<ResumeRequest.ResumeSectionRequest> sections
) {
    
    /**
     * ResumeRequest로부터 생성
     */
    public static ResumeCreateRequest from(ResumeRequest request) {
        return new ResumeCreateRequest(
            request.title(),
            request.memberInfo(), 
            request.sections()
        );
    }
    
    /**
     * ResumeRequest로 변환
     */
    public ResumeRequest toResumeRequest() {
        return new ResumeRequest(title, memberInfo, sections);
    }
}
