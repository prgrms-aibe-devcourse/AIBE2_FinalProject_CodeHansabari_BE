package com.cvmento.domain.coverLetter.client;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "cover-letter-llm-client",
        url = "${llm.api.url}",
        configuration = CoverLetterLlmFeignConfig.class
)
public interface CoverLetterLlmFeignClient {

    // Raw String 응답을 받는 메소드 추가
    @PostMapping("/responses")
    String analyzeRaw(@RequestBody LlmRequest request);
}