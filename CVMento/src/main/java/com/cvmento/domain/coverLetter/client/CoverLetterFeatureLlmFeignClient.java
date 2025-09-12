package com.cvmento.domain.coverLetter.client;

import com.cvmento.domain.coverLetter.dto.request.GeminiRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "cover-letter-feature-llm-client",
    url = "${llm.api.feature-extraction.url}",
    configuration = CoverLetterFeatureLlmFeignConfig.class
)
public interface CoverLetterFeatureLlmFeignClient {

    @PostMapping("/models/gemini-2.5-flash:generateContent")
    String analyzeRaw(@RequestBody GeminiRequest request);
}
