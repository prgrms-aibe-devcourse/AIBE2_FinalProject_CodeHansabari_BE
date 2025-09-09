package com.cvmento.domain.coverLetter.client;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "cover-letter-feature-llm-client",
    url = "${llm.api.url}",
    configuration = CoverLetterFeatureLlmFeignConfig.class
)
public interface CoverLetterFeatureLlmFeignClient {

    @PostMapping("/responses")
    String analyzeRaw(@RequestBody LlmRequest request);
}
