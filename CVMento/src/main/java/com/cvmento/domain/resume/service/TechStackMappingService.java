package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.entity.TechStack;
import com.cvmento.domain.resume.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechStackMappingService {

    private final TechStackRepository techStackRepository;
    private Map<String, Long> techStackNameToIdCache;

    /**
     * 기술스택 이름으로 ID 조회 (캐시 사용)
     */
    public Optional<Long> findTechStackIdByName(String techStackName) {
        if (techStackName == null || techStackName.trim().isEmpty()) {
            return Optional.empty();
        }

        // 캐시가 없으면 초기화
        if (techStackNameToIdCache == null) {
            initializeCache();
        }

        Long techStackId = techStackNameToIdCache.get(techStackName.trim());
        if (techStackId != null) {
            log.debug("기술스택 매핑 성공: {} -> {}", techStackName, techStackId);
            return Optional.of(techStackId);
        } else {
            log.warn("기술스택 매핑 실패 - 존재하지 않는 기술명: {}", techStackName);
            return Optional.empty();
        }
    }

    /**
     * 기술스택 이름으로 TechStack 엔티티 조회
     */
    public Optional<TechStack> findTechStackByName(String techStackName) {
        MDC.put("spanId", "techStack-mapping-service");
        
        if (techStackName == null || techStackName.trim().isEmpty()) {
            log.warn("빈 기술스택 이름으로 조회 시도");
            return Optional.empty();
        }

        Optional<TechStack> techStack = techStackRepository.findByName(techStackName.trim());
        
        if (techStack.isPresent()) {
            log.info("기술스택 조회 성공: {} (ID: {})", techStackName, techStack.get().getId());
        } else {
            log.warn("존재하지 않는 기술스택: {}", techStackName);
        }
        
        return techStack;
    }

    /**
     * 캐시 초기화 - 모든 기술스택을 메모리에 로드
     */
    private void initializeCache() {
        MDC.put("spanId", "techStack-cache-init");
        
        try {
            List<TechStack> allTechStacks = techStackRepository.findAll();
            techStackNameToIdCache = new HashMap<>();
            
            for (TechStack techStack : allTechStacks) {
                techStackNameToIdCache.put(techStack.getName(), techStack.getId());
            }
            
        } catch (Exception e) {
            log.error("기술스택 캐시 초기화 실패: {}", e.getMessage(), e);
            techStackNameToIdCache = new HashMap<>(); // 빈 캐시라도 생성
        }
    }

    /**
     * 캐시 갱신 (관리자용)
     */
    public void refreshCache() {
        MDC.put("spanId", "techStack-cache-refresh");
        
        log.info("기술스택 캐시 수동 갱신 시작");
        techStackNameToIdCache = null;
        initializeCache();
    }

    /**
     * 사용 가능한 기술스택 이름 목록 조회
     */
    public List<String> getAllTechStackNames() {
        MDC.put("spanId", "techStack-names-query");

        return techStackRepository.findAll()
                .stream()
                .map(TechStack::getName)
                .sorted()
                .toList();
    }
}