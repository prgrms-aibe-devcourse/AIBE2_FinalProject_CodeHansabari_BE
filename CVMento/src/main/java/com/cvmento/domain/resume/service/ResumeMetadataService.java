package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.response.EnumOptionResponse;
import com.cvmento.domain.resume.dto.response.ResumeMetadataResponse;
import com.cvmento.domain.resume.dto.response.TechStackResponse;
import com.cvmento.domain.resume.entity.TechStack;
import com.cvmento.domain.resume.enums.*;
import com.cvmento.domain.resume.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ResumeMetadataService {

    private final TechStackRepository techStackRepository;

    /**
     * 이력서 작성에 필요한 모든 메타데이터 조회
     */
    public ResumeMetadataResponse getResumeMetadata() {
        MDC.put("spanId", "metadata-service");

        log.info("이력서 메타데이터 조회 시작");

        // 기술 스택 목록
        MDC.put("spanId", "techstack-repository");
        List<TechStack> techStacks = techStackRepository.findAllByOrderByNameAsc();

        MDC.put("spanId", "metadata-service");
        List<TechStackResponse> techStackResponses = techStacks.stream()
                .map(TechStackResponse::from)
                .toList();

        // Enum 값들을 EnumOptionResponse로 변환
        List<EnumOptionResponse> resumeTypes = Arrays.stream(ResumeType.values())
                .map(type -> new EnumOptionResponse(type.name(), type.getDescription()))
                .toList();

        List<EnumOptionResponse> careerTypes = Arrays.stream(CareerType.values())
                .map(type -> new EnumOptionResponse(type.name(), type.getDescription()))
                .toList();

        List<EnumOptionResponse> degreeLevels = Arrays.stream(DegreeLevel.values())
                .map(level -> new EnumOptionResponse(level.name(), level.getDescription()))
                .toList();

        List<EnumOptionResponse> proficiencyLevels = Arrays.stream(ProficiencyLevel.values())
                .map(level -> new EnumOptionResponse(level.name(), level.getDescription()))
                .toList();

        List<EnumOptionResponse> projectTypes = Arrays.stream(ProjectType.values())
                .map(type -> new EnumOptionResponse(type.name(), type.getDescription()))
                .toList();

        List<EnumOptionResponse> additionalInfoCategories = Arrays.stream(AdditionalInfoCategory.values())
                .map(category -> new EnumOptionResponse(category.name(), category.getDescription()))
                .toList();

        return new ResumeMetadataResponse(
                techStackResponses,
                resumeTypes,
                careerTypes,
                degreeLevels,
                proficiencyLevels,
                projectTypes,
                additionalInfoCategories
        );
    }
}