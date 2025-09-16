package com.cvmento.domain.coverLetter.client;

import com.cvmento.domain.coverLetter.dto.request.GeminiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Gemini API를 사용한 자소서 특징 추출용 Feign 클라이언트
 * - Gemini 2.5 Flash 모델 사용
 * - JSON 구조화된 응답 지원
 */
@FeignClient(
        name = "cover-letter-feature-llm-client",
        url = "${gemini.api.feature-extraction.url}",
        configuration = CoverLetterFeatureLlmFeignConfig.class
)
public interface CoverLetterFeatureLlmFeignClient {

    /**
     * Gemini 2.5 Flash API를 통한 특징 추출 요청
     *
     * @param model   사용할 Gemini 모델명 (예: "gemini-2.5-flash")
     * @param request Gemini 요청 페이로드
     * @return Gemini API의 원본 JSON 응답
     */
    @PostMapping("/models/{model}:generateContent")
    String analyzeRaw(@PathVariable("model") String model,
                      @RequestBody GeminiRequest request);
}
