package com.cvmento.domain.resume.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 기술스택 저장/조회 변환기
 * - 일관되게 JSON 형식으로 저장하고 조회
 * - 하위 호환성을 위해 CSV 형식도 읽기 가능
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TechStackConverter {

    private final ObjectMapper objectMapper;

    /**
     * List<String>을 JSON 문자열로 변환
     */
    public String toJson(List<String> techStack) {
        if (techStack == null || techStack.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(techStack);
        } catch (JsonProcessingException e) {
            log.error("기술스택을 JSON으로 변환 실패: {}", techStack, e);
            // fallback: CSV 형식으로 저장
            return String.join(",", techStack);
        }
    }

    /**
     * JSON 문자열을 List<String>으로 변환
     * - 하위 호환성을 위해 CSV 형식도 지원
     */
    public List<String> fromJson(String techStackJson) {
        if (techStackJson == null || techStackJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // JSON 형식으로 파싱 시도
            return objectMapper.readValue(techStackJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.debug("JSON 파싱 실패, CSV 형식으로 파싱 시도: {}", techStackJson);
            
            // CSV 형식 fallback (하위 호환성)
            return Arrays.asList(techStackJson.split(","))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }
}