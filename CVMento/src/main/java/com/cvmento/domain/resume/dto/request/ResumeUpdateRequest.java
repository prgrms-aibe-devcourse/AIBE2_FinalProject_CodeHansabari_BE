package com.cvmento.domain.resume.dto.request;


public record ResumeUpdateRequest(
    String title,
    ResumeRequest.MemberInfoRequest memberInfo,
    java.util.List<ResumeRequest.ResumeSectionRequest> sections
) {
    
    /**
     * ResumeRequest로부터 생성
     */
    public static ResumeUpdateRequest from(ResumeRequest request) {
        return new ResumeUpdateRequest(
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
