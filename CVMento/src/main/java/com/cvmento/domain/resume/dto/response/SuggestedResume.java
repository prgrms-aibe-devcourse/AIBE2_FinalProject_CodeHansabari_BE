package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.resume.enums.ResumeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI가 제안하는 이력서 내용")
public record SuggestedResume(

        @Schema(description = "이력서 제목", example = "네이버 백엔드 개발자 지원용 이력서")
        String title,

        @Schema(description = "이력서 타입", example = "DEFAULT")
        ResumeType type,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "이메일", example = "hong@example.com")
        String email,

        @Schema(description = "출생년도", example = "1995")
        Integer birthYear,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "경력 구분", example = "EXPERIENCED")
        CareerType careerType,

        @Schema(description = "지원분야", example = "백엔드 개발자")
        String fieldName,

        @Schema(description = "자기소개", 
                example = "2년간의 백엔드 개발 경험을 바탕으로 효율적인 API 설계와 데이터베이스 최적화에 전문성을 가지고 있습니다.")
        String introduction,

        @Schema(description = "깃허브 URL", example = "https://github.com/username")
        String githubUrl,

        @Schema(description = "블로그 URL", example = "https://blog.example.com")
        String blogUrl,

        @Schema(description = "노션 URL", example = "https://notion.so/username")
        String notionUrl,

        @Schema(description = "제안된 학력 정보")
        List<SuggestedEducation> educations,

        @Schema(description = "제안된 기술스택")
        List<SuggestedTechStack> techStacks,

        @Schema(description = "제안된 경력 정보")
        List<SuggestedCareer> careers,

        @Schema(description = "제안된 프로젝트")
        List<SuggestedProject> projects,

        @Schema(description = "제안된 교육이력")
        List<SuggestedTraining> trainings,

        @Schema(description = "제안된 기타사항")
        List<SuggestedAdditionalInfo> additionalInfos

) {
}