package com.cvmento.domain.coverLetter.client;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 자소서 LLM 분석용 Feign 클라이언트
 */
@FeignClient(
        name = "cover-letter-llm-client",
        url = "${llm.api.url}",
        configuration = CoverLetterLlmFeignConfig.class
)
public interface CoverLetterLlmFeignClient {

    /**
     * LLM 분석 요청 (Raw String 응답)
     *
     * @param request LLM 요청 페이로드
     * @return 모델의 원본 문자열 응답
     */
    @PostMapping("/responses")
    String analyzeRaw(@RequestBody LlmRequest request);
}
