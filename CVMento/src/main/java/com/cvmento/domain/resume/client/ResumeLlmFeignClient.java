package com.cvmento.domain.resume.client;

import com.cvmento.domain.resume.dto.request.LlmRequest;
import com.cvmento.domain.resume.dto.response.LlmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 이력서 LLM API 연동을 위한 Feign Client
 */
@FeignClient(
        name = "resume-llm-client",
        url = "${llm.api.url:https://api.openai.com/v1}",
        configuration = ResumeLlmFeignClientConfig.class
)
public interface ResumeLlmFeignClient {

    /**
     * LLM API 호출 - Raw String 응답
     */
    @PostMapping("/responses")
    String callLlm(
            @RequestHeader("Authorization") String authorization,
            @RequestBody LlmRequest request
    );
}