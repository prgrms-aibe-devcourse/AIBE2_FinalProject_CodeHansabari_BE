package com.cvmento.domain.coverLetter.client;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "feature-extraction-llm-client",
    url = "${llm.api.url}",
    configuration = FeatureExtractionLlmFeignConfig.class
)
public interface FeatureExtractionLlmFeignClient {

    @PostMapping("/responses")
    String analyzeRaw(@RequestBody LlmRequest request);
}
