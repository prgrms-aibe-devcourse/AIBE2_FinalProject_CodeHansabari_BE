package com.cvmento.domain.interview.client;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 인터뷰 LLM 호출용 Feign 클라이언트.
 * 원시 문자열 응답을 그대로 반환한다.
 */
@FeignClient(
        name = "interview-llm-client",
        url = "${llm.api.url}",
        configuration = InterviewLlmFeignConfig.class
)
public interface InterviewLlmFeignClient {

    /**
     * LLM 분석 요청(원문 문자열 응답).
     */
    @PostMapping("/responses")
    String analyzeRaw(@RequestBody LlmRequest request);
}
