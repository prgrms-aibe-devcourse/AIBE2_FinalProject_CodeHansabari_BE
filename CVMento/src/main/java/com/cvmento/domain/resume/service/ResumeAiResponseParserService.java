package com.cvmento.domain.resume.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.resume.dto.request.UserExperienceRequest;
import com.cvmento.domain.resume.dto.response.*;
import com.cvmento.domain.resume.dto.response.LlmSuggestedResume.*;
import com.cvmento.domain.resume.enums.AdditionalInfoCategory;
import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.resume.enums.DegreeLevel;
import com.cvmento.domain.resume.enums.ProficiencyLevel;
import com.cvmento.domain.resume.enums.ProjectType;
import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.ResumeAiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LLM 응답 파싱 및 DTO 변환 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAiResponseParserService {

    private final ObjectMapper objectMapper;
    private final ResumeAiValidationService validationService;
    private final OpenAiResponseParser openAiResponseParser;

    /**
     * LLM 이력서 제안 응답을 ResumeAiSuggestionResponse로 변환
     */
    public ResumeAiSuggestionResponse parseResumeSuggestionResponse(
            String llmResponse, 
            UserExperienceRequest request, 
            Member member) {
        
        try {
            log.debug("LLM 응답 파싱 시작. 응답 길이: {} 문자", llmResponse.length());
            
            // 1단계: 기본 응답 검증
            if (!validationService.isValidLlmResponse(llmResponse)) {
                throw new ResumeAiException("유효하지 않은 LLM 응답입니다.");
            }
            
            // 2단계: OpenAI 응답에서 실제 텍스트 컨텐츠 추출 (인터뷰 AI와 동일한 방식)
            String textContent = openAiResponseParser.extractTextContent(llmResponse);
            log.debug("OpenAI에서 추출된 텍스트: {}", textContent.substring(0, Math.min(300, textContent.length())));
            
            // 3단계: 코드 블록 제거 등 정제
            String cleanedJson = cleanJsonContent(textContent);
            log.debug("정제된 JSON: {}", cleanedJson.substring(0, Math.min(300, cleanedJson.length())));
            
            // 4단계: 응답 내용 검증 (디버깅을 위해 임시로 완화)
            if (!validationService.isValidResumeSuggestionContent(cleanedJson)) {
                log.warn("응답 검증 실패 - 하지만 파싱을 시도합니다. cleanedJson 첫 500자: {}", 
                    cleanedJson.substring(0, Math.min(500, cleanedJson.length())));
            }
            
            // 4단계: 보안 검증
            if (validationService.containsSensitiveInfo(cleanedJson)) {
                log.warn("민감한 정보가 포함된 응답이 감지되었습니다.");
                throw new ResumeAiException("응답에 부적절한 내용이 포함되어 있습니다.");
            }
            
            // 5단계: 응답 품질 점수 계산
            int qualityScore = validationService.calculateResponseQuality(cleanedJson);
            log.info("LLM 응답 품질 점수: {}/100", qualityScore);
            
            if (qualityScore < 30) {
                log.warn("LLM 응답 품질이 낮습니다. 점수: {}", qualityScore);
            }
            
            // 6단계: LLM 응답을 중간 DTO로 파싱
            LlmResumeResponse llmResumeResponse = objectMapper.readValue(cleanedJson, LlmResumeResponse.class);
            
            // 7단계: 중간 DTO를 최종 응답 DTO로 변환 (null 안전성 확보)
            if (llmResumeResponse == null) {
                log.error("llmResumeResponse가 null입니다.");
                throw new ResumeAiException("AI 응답 파싱에 실패했습니다.");
            }
            
            if (llmResumeResponse.suggestedResume() == null) {
                log.error("llmResumeResponse.suggestedResume()이 null입니다.");
                throw new ResumeAiException("AI가 제안한 이력서 내용이 없습니다.");
            }
            
            SuggestedResume suggestedResume = convertToSuggestedResume(llmResumeResponse.suggestedResume());
            
            log.info("LLM 응답 파싱 완료");
            
            return new ResumeAiSuggestionResponse(
                    suggestedResume,
                    llmResumeResponse.improvementTips() != null ? llmResumeResponse.improvementTips() : List.of(),
                    llmResumeResponse.missingElements() != null ? llmResumeResponse.missingElements() : List.of()
            );
            
        } catch (JsonProcessingException e) {
            log.error("LLM 응답 JSON 파싱 실패: {}", e.getMessage(), e);
            log.debug("파싱 실패한 응답: {}", llmResponse);
            throw new ResumeAiException("AI 응답 파싱 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("LLM 응답 처리 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new ResumeAiException("AI 응답 처리 중 오류가 발생했습니다.", e);
        }
    }


    /**
     * 텍스트 컨텐츠에서 JSON 정제 (코드 블록 제거 등)
     */
    private String cleanJsonContent(String textContent) {
        String trimmed = textContent.trim();
        
        // ```json으로 시작하는 경우
        if (trimmed.startsWith("```json")) {
            int startIndex = trimmed.indexOf("```json") + 7;
            int endIndex = trimmed.lastIndexOf("```");
            if (endIndex > startIndex) {
                trimmed = trimmed.substring(startIndex, endIndex).trim();
            }
        }
        
        // ```로 시작하는 경우
        if (trimmed.startsWith("```")) {
            int startIndex = trimmed.indexOf("```") + 3;
            int endIndex = trimmed.lastIndexOf("```");
            if (endIndex > startIndex) {
                trimmed = trimmed.substring(startIndex, endIndex).trim();
            }
        }
        
        // 코드 블록 제거 완료, 정제된 JSON 반환
        return trimmed;
    }

    /**
     * LlmSuggestedResume을 SuggestedResume으로 변환
     */
    private SuggestedResume convertToSuggestedResume(LlmSuggestedResume llmResume) {
        return new SuggestedResume(
                llmResume.title(),
                safeParseResumeType(llmResume.type()),
                llmResume.name(),
                llmResume.email(),
                llmResume.birthYear(),
                llmResume.phone(),
                safeParseCareerType(llmResume.careerType()),
                llmResume.fieldName(),
                llmResume.introduction(),
                llmResume.githubUrl(),
                llmResume.blogUrl(),
                llmResume.notionUrl(),
                convertEducations(llmResume.educations()),
                convertTechStacks(llmResume.techStacks()),
                convertCareers(llmResume.careers()),
                convertProjects(llmResume.projects()),
                convertTrainings(llmResume.trainings()),
                convertAdditionalInfos(llmResume.additionalInfos())
        );
    }

    private List<SuggestedEducation> convertEducations(List<LlmEducation> llmEducations) {
        if (llmEducations == null) return List.of();
        
        return llmEducations.stream()
                .map(edu -> new SuggestedEducation(
                        safeGetString(edu.schoolName()),
                        safeGetString(edu.major()),
                        safeParseDegreeLevel(edu.degreeLevel()),
                        edu.personalGpa(),
                        edu.totalGpa(),
                        safeParseDate(edu.graduationDate())
                ))
                .collect(Collectors.toList());
    }

    private List<SuggestedTechStack> convertTechStacks(List<LlmTechStack> llmTechStacks) {
        if (llmTechStacks == null) return List.of();
        
        return llmTechStacks.stream()
                .map(tech -> new SuggestedTechStack(
                        tech.techStackId(),
                        tech.techStackName(),
                        tech.category(),
                        safeParseProficiencyLevel(tech.proficiencyLevel()),
                        tech.experienceDescription()
                ))
                .collect(Collectors.toList());
    }

    private List<SuggestedCareer> convertCareers(List<LlmCareer> llmCareers) {
        if (llmCareers == null) return List.of();
        
        return llmCareers.stream()
                .map(career -> new SuggestedCareer(
                        safeParseDate(career.startDate()),
                        safeParseDate(career.endDate()),
                        career.companyName(),
                        career.companyDescription(),
                        career.departmentPosition(),
                        career.mainTasks(),
                        convertCareerTechStacks(career.techStacks())
                ))
                .collect(Collectors.toList());
    }

    private List<SuggestedCareerTechStack> convertCareerTechStacks(List<LlmCareerTechStack> llmTechStacks) {
        if (llmTechStacks == null) return List.of();
        
        return llmTechStacks.stream()
                .map(tech -> new SuggestedCareerTechStack(
                        tech.techStackId(),
                        tech.techStackName(),
                        safeParseProficiencyLevel(tech.proficiencyLevel())
                ))
                .collect(Collectors.toList());
    }

    private List<SuggestedProject> convertProjects(List<LlmProject> llmProjects) {
        if (llmProjects == null) return List.of();
        
        return llmProjects.stream()
                .map(project -> new SuggestedProject(
                        safeParseDate(project.startDate()),
                        safeParseDate(project.endDate()),
                        project.name(),
                        project.description(),
                        project.detailedDescription(),
                        project.repositoryUrl(),
                        project.deployUrl(),
                        safeParseProjectType(project.projectType()),
                        convertProjectTechStacks(project.techStacks())
                ))
                .collect(Collectors.toList());
    }

    private List<SuggestedProjectTechStack> convertProjectTechStacks(List<LlmProjectTechStack> llmTechStacks) {
        if (llmTechStacks == null) return List.of();
        
        return llmTechStacks.stream()
                .map(tech -> new SuggestedProjectTechStack(
                        tech.techStackId(),
                        tech.techStackName(),
                        safeParseProficiencyLevel(tech.proficiencyLevel())
                ))
                .collect(Collectors.toList());
    }

    private List<SuggestedTraining> convertTrainings(List<LlmTraining> llmTrainings) {
        if (llmTrainings == null) return List.of();
        
        return llmTrainings.stream()
                .map(training -> new SuggestedTraining(
                        safeParseDate(training.startDate()),
                        safeParseDate(training.endDate()),
                        training.name(),
                        training.institution(),
                        training.description(),
                        List.of() // 현재는 기술스택 없음
                ))
                .collect(Collectors.toList());
    }

    private List<SuggestedAdditionalInfo> convertAdditionalInfos(List<LlmAdditionalInfo> llmAdditionalInfos) {
        if (llmAdditionalInfos == null) return List.of();
        
        return llmAdditionalInfos.stream()
                .map(info -> new SuggestedAdditionalInfo(
                        safeParseAdditionalInfoCategory(info.category()),
                        info.title(),
                        info.content(),
                        safeParseDate(info.achievementDate()),
                        info.description()
                ))
                .collect(Collectors.toList());
    }

    // Safe parsing methods
    private String safeGetString(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : null;
    }

    private ResumeType safeParseResumeType(String resumeType) {
        try {
            return ResumeType.valueOf(resumeType);
        } catch (Exception e) {
            log.warn("이력서 타입 파싱 실패: {}. DEFAULT로 기본값 설정", resumeType);
            return ResumeType.DEFAULT;
        }
    }

    private CareerType safeParseCareerType(String careerType) {
        try {
            return CareerType.valueOf(careerType);
        } catch (Exception e) {
            log.warn("경력 타입 파싱 실패: {}. FRESHMAN로 기본값 설정", careerType);
            return CareerType.FRESHMAN;
        }
    }

    private DegreeLevel safeParseDegreeLevel(String degreeLevel) {
        try {
            return DegreeLevel.valueOf(degreeLevel);
        } catch (Exception e) {
            log.warn("학위 수준 파싱 실패: {}. BACHELOR로 기본값 설정", degreeLevel);
            return DegreeLevel.BACHELOR;
        }
    }

    private ProficiencyLevel safeParseProficiencyLevel(String proficiencyLevel) {
        try {
            return ProficiencyLevel.valueOf(proficiencyLevel);
        } catch (Exception e) {
            log.warn("숙련도 수준 파싱 실패: {}. INTERMEDIATE로 기본값 설정", proficiencyLevel);
            return ProficiencyLevel.INTERMEDIATE;
        }
    }

    private ProjectType safeParseProjectType(String projectType) {
        try {
            return ProjectType.valueOf(projectType);
        } catch (Exception e) {
            log.warn("프로젝트 타입 파싱 실패: {}. PERSONAL로 기본값 설정", projectType);
            return ProjectType.PERSONAL;
        }
    }

    private AdditionalInfoCategory safeParseAdditionalInfoCategory(String category) {
        try {
            return AdditionalInfoCategory.valueOf(category);
        } catch (Exception e) {
            log.warn("기타사항 카테고리 파싱 실패: {}. CERTIFICATE로 기본값 설정", category);
            return AdditionalInfoCategory.CERTIFICATE;
        }
    }

    private LocalDate safeParseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: {}. null로 설정", dateString);
            return null;
        }
    }
}